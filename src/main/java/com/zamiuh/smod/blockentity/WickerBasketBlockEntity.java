package com.zamiuh.smod.blockentity;

import com.zamiuh.smod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 简易储物篮方块实体（docs/04）：9 格纯容器。
 * TODO 正式 GUI（Menu/Screen）、实现 WorldlyContainer 兼容漏斗。
 */
public class WickerBasketBlockEntity extends BlockEntity {
    private final NonNullList<ItemStack> items = NonNullList.withSize(9, ItemStack.EMPTY);

    public WickerBasketBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.WICKER_BASKET.get(), pos, state);
    }

    public NonNullList<ItemStack> getItems() {
        return items;
    }

    /**
     * 手持物品右键：整组放入第一空格。
     */
    public ItemInteractionResult tryInsert(ItemStack stack) {
        for (int i = 0; i < items.size(); i++) {
            if (items.get(i).isEmpty()) {
                items.set(i, stack.split(1));
                setChanged();
                return ItemInteractionResult.CONSUME;
            }
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    /**
     * 空手右键：取回最后放入的一组。
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

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        ContainerHelperSaveHelper.save(tag, items, registries);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ContainerHelperSaveHelper.load(tag, items, registries);
    }
}
