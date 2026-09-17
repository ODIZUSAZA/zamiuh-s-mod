package com.zamiuh.smod.item;

import com.zamiuh.smod.block.FirePitBlock;
import com.zamiuh.smod.blockentity.FirePitBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

/**
 * 钻木取火器：对未点燃的篝火坑使用，40% 概率点燃。耐久 10，不可修复。
 */
public class FireDrillItem extends Item {
    private static final float IGNITE_CHANCE = 0.4F;

    public FireDrillItem(Properties properties) {
        super(properties.durability(10));
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        BlockState state = level.getBlockState(pos);

        if (!(state.getBlock() instanceof FirePitBlock) || state.getValue(BlockStateProperties.LIT)) {
            return InteractionResult.PASS;
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }
        if (level.getBlockEntity(pos) instanceof FirePitBlockEntity firePit && firePit.tryIgnite(IGNITE_CHANCE)) {
            context.getItemInHand().hurtAndBreak(1, context.getPlayer(), LivingEntity.getSlotForHand(context.getHand()));
            level.playSound(null, pos, SoundEvents.WOOD_BREAK, SoundSource.BLOCKS, 1.0F, 0.7F);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.PASS;
    }
}
