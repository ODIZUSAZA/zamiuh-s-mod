package com.zamiuh.smod.block;

import com.zamiuh.smod.blockentity.KnappingTableBlockEntity;
import com.zamiuh.smod.knapping.StoneMaterial;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

/**
 * 敲石台（docs/13 v0.1.1）：
 * - 手持可打制石材右键 → 放上台面（随机生成五维隐藏属性，中/重度风化拒绝）；
 * - 潜行 + 空手右键 → 收回台面上的原石；
 * - 空手右键 → 打开打制小游戏界面（32×32 网格 + 右侧 5 格打制工具槽）。
 */
public class KnappingTableBlock extends Block implements EntityBlock {

    public KnappingTableBlock(Properties properties) {
        super(properties);
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level level, BlockPos pos, Player player, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof KnappingTableBlockEntity table) {
            if (!level.isClientSide) {
                if (player.isShiftKeyDown()) {
                    table.retrieveStone(player);
                } else {
                    // NeoForge 扩展菜单：写入方块位置供客户端工厂解析
                    player.openMenu(table, buf -> buf.writeBlockPos(pos));
                }
            }
            return InteractionResult.sidedSuccess(level.isClientSide);
        }
        return InteractionResult.PASS;
    }

    @Override
    protected ItemInteractionResult useItemOn(net.minecraft.world.item.ItemStack stack, BlockState state, Level level, BlockPos pos,
                                          Player player, net.minecraft.world.InteractionHand hand, BlockHitResult hitResult) {
        if (level.getBlockEntity(pos) instanceof KnappingTableBlockEntity table
                && StoneMaterial.byItem(stack.getItem()) != null) {
            if (!level.isClientSide) {
                table.tryPlaceStone(player);
            }
            return ItemInteractionResult.sidedSuccess(level.isClientSide);
        }
        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new KnappingTableBlockEntity(pos, state);
    }
}
