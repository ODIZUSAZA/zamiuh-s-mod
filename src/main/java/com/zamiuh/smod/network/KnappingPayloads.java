package com.zamiuh.smod.network;

import com.zamiuh.smod.blockentity.KnappingTableBlockEntity;
import com.zamiuh.smod.SMod;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.handling.IPayloadContext;

/**
 * 打制小游戏 C2S 网络包（docs/14 v0.1.1）：
 * 吸附工具 / 放下工具 / 敲击 / 取出成品。
 * 网格状态由方块实体 update tag 同步，客户端直接读取客户端侧方块实体渲染。
 */
public final class KnappingPayloads {
    private static final double MAX_INTERACT_DISTANCE_SQR = 64.0;

    // ==================== C2S ====================

    public record AttachToolPayload(BlockPos pos, int slot) implements CustomPacketPayload {
        public static final Type<AttachToolPayload> TYPE = new Type<>(SMod.id("knapping_attach_tool"));
        public static final StreamCodec<RegistryFriendlyByteBuf, AttachToolPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, AttachToolPayload::pos,
                ByteBufCodecs.VAR_INT, AttachToolPayload::slot,
                AttachToolPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record PutToolPayload(BlockPos pos, int slotHint) implements CustomPacketPayload {
        public static final Type<PutToolPayload> TYPE = new Type<>(SMod.id("knapping_put_tool"));
        public static final StreamCodec<RegistryFriendlyByteBuf, PutToolPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, PutToolPayload::pos,
                ByteBufCodecs.VAR_INT, PutToolPayload::slotHint,
                PutToolPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    /** power：蓄力 0~100。 */
    public record StrikePayload(BlockPos pos, int x, int y, int power) implements CustomPacketPayload {
        public static final Type<StrikePayload> TYPE = new Type<>(SMod.id("knapping_strike"));
        public static final StreamCodec<RegistryFriendlyByteBuf, StrikePayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, StrikePayload::pos,
                ByteBufCodecs.VAR_INT, StrikePayload::x,
                ByteBufCodecs.VAR_INT, StrikePayload::y,
                ByteBufCodecs.VAR_INT, StrikePayload::power,
                StrikePayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    public record TakeResultPayload(BlockPos pos) implements CustomPacketPayload {
        public static final Type<TakeResultPayload> TYPE = new Type<>(SMod.id("knapping_take_result"));
        public static final StreamCodec<RegistryFriendlyByteBuf, TakeResultPayload> STREAM_CODEC = StreamCodec.composite(
                BlockPos.STREAM_CODEC, TakeResultPayload::pos,
                TakeResultPayload::new);

        @Override
        public Type<? extends CustomPacketPayload> type() {
            return TYPE;
        }
    }

    // ==================== 服务端处理 ====================

    public static void handleAttach(AttachToolPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withTable(context, payload.pos(), table -> table.attachTool(payload.slot())));
    }

    public static void handlePut(PutToolPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withTable(context, payload.pos(), table -> table.putTool(payload.slotHint())));
    }

    public static void handleStrike(StrikePayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withTable(context, payload.pos(),
                table -> table.strike(payload.x(), payload.y(), payload.power())));
    }

    public static void handleTake(TakeResultPayload payload, IPayloadContext context) {
        context.enqueueWork(() -> withTable(context, payload.pos(),
                table -> table.takeResult(context.player())));
    }

    private static void withTable(IPayloadContext context, BlockPos pos, java.util.function.Consumer<KnappingTableBlockEntity> action) {
        if (context.player() instanceof ServerPlayer serverPlayer
                && serverPlayer.level().getBlockEntity(pos) instanceof KnappingTableBlockEntity table
                && serverPlayer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) < MAX_INTERACT_DISTANCE_SQR) {
            action.accept(table);
        }
    }

    private KnappingPayloads() {
    }
}
