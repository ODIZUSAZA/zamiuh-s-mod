package com.zamiuh.smod.block;

import com.zamiuh.smod.blockentity.KnappingTableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 敲石台：手持燧石右键放上工作台，空手连续右键敲击，
 * 达到目标次数（3~5）后掉落燧石碎片。
 * TODO 打制小游戏 GUI（节点图案），见 docs/03。
 */
public class KnappingTableBlock extends Block implements EntityBlock {

    public KnappingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (!level.isClientSide && level.getBlockEntity(pos) instanceof KnappingTableBlockEntity knappingTable) {
            return knappingTable.tryKnapping(player);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KnappingTableBlockEntity(pos, state);
    }
}
