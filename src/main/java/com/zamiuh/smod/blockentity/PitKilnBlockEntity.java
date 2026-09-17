package com.zamiuh.smod.blockentity;

import com.zamiuh.smod.registry.ModBlockEntities;
import com.zamiuh.smod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;

/**
 * 土窑方块实体（docs/14）：
 * 槽位 0 燃料 / 1 输入 / 2 输出。
 * 烧制映射：湿陶坯 → 陶罐；干陶坯 → 陶罐。
 * 骨架版烧制时长 1200t（正式版 12000t）；中途破坏按原状态掉落。
 */
public class PitKilnBlockEntity extends BlockEntity {
    public static final int KILN_TIME = 1200;

    private final NonNullList<ItemStack> items = NonNullList.withSize(3, ItemStack.EMPTY);
    private int burnTime = 0;
    private int progress = 0;

    public PitKilnBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.PIT_KILN.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    /** 燃料映射：树皮 800 / 树脂 1200 / 干燥纤维 400 / 其他复用通用燃料值 */
    public int getFuelTime(ItemStack stack) {
        Item item = stack.getItem();
        if (item == ModItems.BARK.get()) {
            return 800;
        }
        if (item == ModItems.RESIN.get()) {
            return 1200;
        }
        if (item == ModItems.DRIED_FIBER.get()) {
            return 400;
        }
        return stack.getBurnTime(null);
    }

    /** 烧制映射表 */
    private static Map<Item, ItemStack> kilnMap() {
        Map<Item, ItemStack> map = new HashMap<>();
        map.put(ModItems.WET_CLAY_POT.get(), new ItemStack(ModItems.CLAY_POT.get()));
        map.put(ModItems.DRIED_CLAY_POT.get(), new ItemStack(ModItems.CLAY_POT.get()));
        return map;
    }

    /**
     * 放入物品：燃料 → 槽 0；可烧制物 → 槽 1。
     */
    public ItemInteractionResult handleInteraction(ItemStack stack) {
        if (items.get(0).isEmpty() && getFuelTime(stack) > 0) {
            items.set(0, stack.split(1));
            setChanged();
            return ItemInteractionResult.CONSUME;
        }
        if (items.get(1).isEmpty() && kilnMap().containsKey(stack.getItem())) {
            items.set(1, stack.split(1));
            setChanged();
            return ItemInteractionResult.CONSUME;
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * 空手右键：优先取产出（槽 2），否则取最后放入的一组。
     */
    public InteractionResult takeLast(Player player) {
        if (!items.get(2).isEmpty()) {
            player.getInventory().placeItemBackInInventory(items.get(2));
            items.set(2, ItemStack.EMPTY);
            setChanged();
            return InteractionResult.SUCCESS;
        }
        for (int i = 1; i >= 0; i--) {
            if (!items.get(i).isEmpty()) {
                player.getInventory().placeItemBackInInventory(items.get(i));
                items.set(i, ItemStack.EMPTY);
                setChanged();
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }

    public static void serverTick(Level level, BlockPos pos, BlockState state, PitKilnBlockEntity be) {
        ItemStack input = be.items.get(1);
        ItemStack output = be.items.get(2);
        boolean changed = false;

        if (input.isEmpty()) {
            be.progress = 0;
        } else {
            ItemStack result = be.kilnMap().get(input.getItem());
            if (result == null || (!output.isEmpty() && !ItemStack.isSameItemSameComponents(output, result))) {
                be.progress = 0;
            } else {
                if (be.burnTime <= 0) {
                    ItemStack fuel = be.items.get(0);
                    if (!fuel.isEmpty()) {
                        be.burnTime = be.getFuelTime(fuel);
                        if (be.burnTime > 0) {
                            fuel.shrink(1);
                            changed = true;
                        }
                    }
                }
                if (be.burnTime > 0) {
                    be.burnTime--;
                    if (++be.progress >= KILN_TIME) {
                        be.progress = 0;
                        if (output.isEmpty()) {
                            be.items.set(2, result.copy());
                        } else {
                            output.grow(result.getCount());
                        }
                        input.shrink(1);
                        changed = true;
                    }
                }
            }
        }
        if (changed) {
            be.setChanged();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelperSaveHelper.save(tag, items, registries);
        tag.putInt("BurnTime", burnTime);
        tag.putInt("Progress", progress);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelperSaveHelper.load(tag, items, registries);
        burnTime = tag.getInt("BurnTime");
        progress = tag.getInt("Progress");
    }
}
