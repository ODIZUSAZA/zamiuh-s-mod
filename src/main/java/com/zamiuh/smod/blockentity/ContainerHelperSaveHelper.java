package com.zamiuh.smod.blockentity;

import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.ItemStack;

/**
 * 容器 NBT 序列化辅助（封装 ContainerHelper，统一标签键名）。
 */
public final class ContainerHelperSaveHelper {
    private static final String TAG_ITEMS = "Items";
    private static final String TAG_SLOT = "Slot";

    public static void save(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        CompoundTag itemsTag = new CompoundTag();
        for (int i = 0; i < items.size(); i++) {
            if (!items.get(i).isEmpty()) {
                itemsTag.put(String.valueOf(i), items.get(i).save(registries, new CompoundTag()));
            }
        }
        tag.put(TAG_ITEMS, itemsTag);
    }

    public static void load(CompoundTag tag, NonNullList<ItemStack> items, HolderLookup.Provider registries) {
        CompoundTag itemsTag = tag.getCompound(TAG_ITEMS);
        for (int i = 0; i < items.size(); i++) {
            if (itemsTag.contains(String.valueOf(i))) {
                int slot = i;
                ItemStack.parse(registries, itemsTag.getCompound(String.valueOf(i)))
                        .ifPresent(s -> items.set(slot, s));
            }
        }
    }

    private ContainerHelperSaveHelper() {
    }
}
