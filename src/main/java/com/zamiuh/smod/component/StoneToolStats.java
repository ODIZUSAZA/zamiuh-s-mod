package com.zamiuh.smod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;

/**
 * 石质工具组件（docs/14 v0.1.1）：
 * 打制完成的石头携带此组件，是「打制工具」类别的核心数据，
 * 后续可在此扩展手柄等加成字段。
 *
 * @param knappedness 打制度 —— 缺失方格越多越高
 * @param sharpness   锋利度 —— 由石材打制性能决定，影响打制时的伤害与效率
 * @param pointedness 尖锐度 —— 由成品左右上三面边界轮廓检测得出，影响伤害
 */
public record StoneToolStats(int knappedness, int sharpness, int pointedness) {

    public static final StoneToolStats ZERO = new StoneToolStats(0, 0, 0);

    public static final Codec<StoneToolStats> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.intRange(0, 100).fieldOf("knappedness").forGetter(StoneToolStats::knappedness),
            Codec.intRange(0, 100).fieldOf("sharpness").forGetter(StoneToolStats::sharpness),
            Codec.intRange(0, 100).fieldOf("pointedness").forGetter(StoneToolStats::pointedness)
    ).apply(inst, StoneToolStats::new));

    public static final StreamCodec<io.netty.buffer.ByteBuf, StoneToolStats> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StoneToolStats::knappedness,
            ByteBufCodecs.VAR_INT, StoneToolStats::sharpness,
            ByteBufCodecs.VAR_INT, StoneToolStats::pointedness,
            StoneToolStats::new);
}
