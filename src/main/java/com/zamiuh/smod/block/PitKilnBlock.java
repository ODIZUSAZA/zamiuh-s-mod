package com.zamiuh.smod.block;

import com.zamiuh.smod.blockentity.PitKilnBlockEntity;
import com.zamiuh.smod.registry.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 土窑（骨架版：单方块；正式版 1×1×2 多方块结构见 docs/14）。
 * 槽位 0 燃料 / 1 输入 / 2 输出，自动烧制湿陶坯、干陶坯为陶罐。
 */
public class PitKilnBlock extends Block implements EntityBlock {

    public PitKilnBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln) {
            return kiln.handleInteraction(stack);
        }
        return ItemInteractionResult.sidedSuccess(level.isClientSide);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln) {
            return kiln.takeLast(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @SuppressWarnings("unchecked")
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide) {
            return null;
        }
        return type == ModBlockEntities.PIT_KILN.get()
                ? (BlockEntityTicker<T>) (BlockEntityTicker<PitKilnBlockEntity>) PitKilnBlockEntity::serverTick
                : null;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // 中途破坏 → 未完成陶坯按原状态掉落（对齐"进度丢失"原则）
        if (!state.is(newState.getBlock()) && level.getBlockEntity(pos) instanceof PitKilnBlockEntity kiln) {
            Containers.dropContents(level, pos, kiln.getItems());
        }
        super.onRemove(state, level, pos, newState, movedByPiston);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new PitKilnBlockEntity(pos, state);
    }
}
