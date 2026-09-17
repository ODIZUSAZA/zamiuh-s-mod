package com.zamiuh.smod.blockentity;

import com.zamiuh.smod.registry.ModBlockEntities;
import com.zamiuh.smod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.neoforged.neoforge.common.CommonHooks;

import java.util.HashMap;
import java.util.Map;

/**
 * 篝火坑方块实体（docs/14）：
 * 槽位 0 燃料 / 1 引火物 / 2 待加热物。
 * 点燃后每 400 tick 烹饪一次；燃料耗尽自动熄灭。
 */
public class FirePitBlockEntity extends BlockEntity {
    public static final int COOK_TIME = 400;

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int fuelTime = 0;
    private int cookProgress = 0;

    public FirePitBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FIRE_PIT.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    /**
     * 尝试点燃：需要引火物（槽 1），按概率成功。
     * 打火石传 1.0F，钻木取火器传 0.4F。
     */
    public boolean tryIgnite(float successChance) {
        if (level() == null || level().isClientSide) {
            return false;
        }
        if (items.get(1).isEmpty()) {
            return false;
        }
        if (level().random.nextFloat() > successChance) {
            return false;
        }
        fuelTime = 1200 + level().random.nextInt(600);
        cookProgress = 0;
        setChanged();
        level().setBlockAndUpdate(worldPosition, getBlockState().setValue(BlockStateProperties.LIT, true));
        level().playSound(null, worldPosition, SoundEvents.FIRE_IGNITE, SoundSource.BLOCKS, 1.0F, 1.0F);
        return true;
    }

    /**
     * 放入物品：燃料 → 槽 0；引火物 → 槽 1；可烹饪物 → 槽 2。
     */
    public ItemInteractionResult handleInteraction(ItemStack stack) {
        Item item = stack.getItem();
        if (items.get(0).isEmpty() && CommonHooks.getBurnTime(stack, null) > 0) {
            items.set(0, stack.split(1));
            setChanged();
            return ItemInteractionResult.CONSUME;
        }
        if (items.get(1).isEmpty() && isKindling(item)) {
            items.set(1, stack.split(1));
            setChanged();
            return ItemInteractionResult.CONSUME;
        }
        if (items.get(2).isEmpty() && cookingMap().containsKey(item)) {
            items.set(2, stack.split(1));
            setChanged();
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * 空手右键：取回最后放入的一组物品。
     */
    public InteractionResult takeLast(Player player) {
        for (int i = items.size() - 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                player.getInventory().placeItemBackInInventory(items.get(i));
                items.set(i, ItemStack.EMPTY);
                setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    private static boolean isKindling(Item item) {
        return item == ModItems.BARK.get() || item == ModItems.RESIN.get() || item == Items.STICK;
    }

    /** 烹饪映射表（TODO 正式版改为 RecipeType 数据包驱动） */
    private static Map<Item, Item> cookingMap() {
        Map<Item, Item> map = new HashMap<>();
        map.put(Items.BEEF, Items.COOKED_BEEF);
        map.put(Items.PORKCHOP, Items.COOKED_PORKCHOP);
        map.put(Items.CHICKEN, Items.COOKED_CHICKEN);
        map.put(Items.COD, Items.COOKED_COD);
        map.put(Items.SALMON, Items.COOKED_SALMON);
        map.put(Items.MUTTON, Items.COOKED_MUTTON);
        map.put(Items.POTATO, Items.BAKED_POTATO);
        return map;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, FirePitBlockEntity be) {
        if (!state.getValue(BlockStateProperties.LIT)) {
            return;
        }
        if (be.fuelTime > 0) {
            be.fuelTime--;
        } else {
            // 尝试续燃
            ItemStack fuel = be.items.get(0);
            if (!fuel.isEmpty()) {
                be.fuelTime = CommonHooks.getBurnTime(fuel, null);
                fuel.shrink(1);
                be.setChanged();
            } else {
                // 熄灭
                level.setBlockAndUpdate(pos, state.setValue(BlockStateProperties.LIT, false));
                be.cookProgress = 0;
                return;
            }
        }
        // 烹饪
        ItemStack heating = be.items.get(2);
        if (!heating.isEmpty() && be.cookingMap().containsKey(heating.getItem())) {
            if (++be.cookProgress >= COOK_TIME) {
                be.cookProgress = 0;
                be.items.set(2, new ItemStack(be.cookingMap().get(heating.getItem())));
                be.setChanged();
            }
        } else {
            be.cookProgress = 0;
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelperSaveHelper.save(tag, items, registries);
        tag.putInt("FuelTime", fuelTime);
        tag.putInt("CookProgress", cookProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelperSaveHelper.load(tag, items, registries);
        fuelTime = tag.getInt("FuelTime");
        cookProgress = tag.getInt("CookProgress");
    }
}
