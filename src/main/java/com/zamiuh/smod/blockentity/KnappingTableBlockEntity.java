package com.zamiuh.smod.blockentity;

import com.zamiuh.smod.registry.ModBlockEntities;
import com.zamiuh.smod.registry.ModItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

/**
 * 敲石台方块实体（docs/14）：
 * Input 放置的燧石 / KnockCount 已敲击次数 / TargetCount 目标次数。
 * TODO 打制小游戏 GUI（节点图案）。
 */
public class KnappingTableBlockEntity extends BlockEntity {
    private ItemStack inputStack = ItemStack.EMPTY;
    private int knockCount = 0;
    private int targetCount = 5;

    public KnappingTableBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.KNAPPING_TABLE.get(), pos, state);
    }

    /**
     * 服务端交互入口：
     * 空台 + 手持燧石 → 放上台面并随机目标次数；
     * 有燧石 + 空手 → 敲击计数，达标后掉落 3~5 个燧石碎片。
     */
    public InteractionResult tryKnapping(Player player) {
        if (level() == null || level().isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (inputStack.isEmpty()) {
            ItemStack held = player.getMainHandItem();
            if (held.is(Items.FLINT)) {
                inputStack = held.split(1);
                targetCount = 3 + level().random.nextInt(3);
                knockCount = 0;
                setChanged();
                return InteractionResult.CONSUME;
            }
            return InteractionResult.PASS;
        }
        if (player.getMainHandItem().isEmpty()) {
            knockCount++;
            level().playSound(null, worldPosition, SoundEvents.STONE_HIT, SoundSource.BLOCKS, 0.8F, 0.8F);
            if (knockCount >= targetCount) {
                Containers.dropItemStack(level(), worldPosition.getX() + 0.5, worldPosition.getY() + 0.5, worldPosition.getZ() + 0.5,
                        new ItemStack(ModItems.FLINT_SHARD.get(), 3 + level().random.nextInt(3)));
                reset();
            }
            setChanged();
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }

    private void reset() {
        inputStack = ItemStack.EMPTY;
        knockCount = 0;
        targetCount = 5;
        setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.put("Input", inputStack.save(registries, new CompoundTag()));
        tag.putInt("KnockCount", knockCount);
        tag.putInt("TargetCount", targetCount);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        ItemStack.parse(registries, tag.getCompound("Input")).ifPresent(s -> this.inputStack = s);
        knockCount = tag.getInt("KnockCount");
        targetCount = tag.getInt("TargetCount");
    }
}
