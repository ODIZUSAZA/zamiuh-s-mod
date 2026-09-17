package com.zamiuh.smod.blockentity;

import com.zamiuh.smod.registry.ModBlockEntities;
import com.zamiuh.smod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * 晾晒架方块实体（docs/04）：
 * 白天 + 天空可见 → 晾晒进度 +1，满 24000 tick（1 游戏日）转换产物。
 */
public class DryingRackBlockEntity extends BlockEntity {
    public static final int DRYING_TIME = 24000;

    private ItemStack hangingStack = ItemStack.EMPTY;
    private int dryingProgress = 0;

    public DryingRackBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRYING_RACK.get(), pos, state);
    }

    public ItemStack getHangingStack() {
        return hangingStack;
    }

    /**
     * 手持物品右键：悬挂可晾晒物。
     */
    public ItemInteractionResult tryHang(ItemStack stack) {
        if (!hangingStack.isEmpty() || !dryingMap().containsKey(stack.getItem())) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }
        hangingStack = stack.split(1);
        dryingProgress = 0;
        setChanged();
        return ItemInteractionResult.CONSUME;
    }

    /**
     * 空手右键：取回悬挂物。
     */
    public InteractionResult takeHanging(Player player) {
        if (hangingStack.isEmpty()) {
            return InteractionResult.PASS;
        }
        player.getInventory().placeItemBackInInventory(hangingStack);
        hangingStack = ItemStack.EMPTY;
        dryingProgress = 0;
        setChanged();
        return InteractionResult.SUCCESS;
    }

    /** 转换表：生肉 → 肉干；湿陶坯 → 干陶坯；植物纤维 → 干燥纤维 */
    private static Map<Item, Item> dryingMap() {
        Map<Item, Item> map = new HashMap<>();
        map.put(Items.BEEF, ModItems.DRIED_MEAT.get());
        map.put(Items.PORKCHOP, ModItems.DRIED_MEAT.get());
        map.put(Items.CHICKEN, ModItems.DRIED_MEAT.get());
        map.put(Items.COD, ModItems.DRIED_MEAT.get());
        map.put(Items.SALMON, ModItems.DRIED_MEAT.get());
        map.put(Items.MUTTON, ModItems.DRIED_MEAT.get());
        map.put(ModItems.WET_CLAY_POT.get(), ModItems.DRIED_CLAY_POT.get());
        map.put(ModItems.PLANT_FIBER.get(), ModItems.DRIED_FIBER.get());
        return map;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, DryingRackBlockEntity be) {
        if (be.hangingStack.isEmpty()) {
            return;
        }
        if (!level.isDay() || !level.canSeeSky(pos.above())) {
            return;
        }
        Item dried = dryingMap().get(be.hangingStack.getItem());
        if (dried == null) {
            return;
        }
        if (++be.dryingProgress >= DRYING_TIME) {
            be.dryingProgress = 0;
            be.hangingStack = new ItemStack(dried, be.hangingStack.getCount());
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Hanging", hangingStack.save(registries, new CompoundTag()));
        tag.putInt("DryingProgress", dryingProgress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ItemStack.parse(registries, tag.getCompound("Hanging")).ifPresent(s -> this.hangingStack = s);
        dryingProgress = tag.getInt("DryingProgress");
    }
}
