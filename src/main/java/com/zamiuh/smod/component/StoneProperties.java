package com.zamiuh.smod.component;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.RandomSource;

/**
 * 石材五维隐藏属性（docs/14 v0.1.1）：
 * 不在游戏内显示，仅作为打制小游戏的后台参数。
 *
 * @param lithology  岩性成分 —— 影响基础可打性上限（成品锋利度上限）
 * @param weathering 成岩与风化（0=无 1=轻度 2=中度 3=重度；中/重度无法打制），影响原石耐久
 * @param mechanics  力学与断裂 —— 决定打击响应，崩边 / 炸裂概率
 * @param knapPerf   打制性能 —— 影响贝壳状断口、锋利度与稳定性（成品锋利度主来源）
 * @param defects    缺陷与结构 —— 影响打制中意外断裂风险与网格外围缺失（基础耐久）
 */
public record StoneProperties(int lithology, int weathering, int mechanics, int knapPerf, int defects) {

    public static final int WEATHERING_NONE = 0;
    public static final int WEATHERING_LIGHT = 1;
    public static final int WEATHERING_MODERATE = 2;
    public static final int WEATHERING_SEVERE = 3;

    public static final Codec<StoneProperties> CODEC = RecordCodecBuilder.create(inst -> inst.group(
            Codec.intRange(0, 100).fieldOf("lithology").forGetter(StoneProperties::lithology),
            Codec.intRange(0, 3).fieldOf("weathering").forGetter(StoneProperties::weathering),
            Codec.intRange(0, 100).fieldOf("mechanics").forGetter(StoneProperties::mechanics),
            Codec.intRange(0, 100).fieldOf("knap_perf").forGetter(StoneProperties::knapPerf),
            Codec.intRange(0, 100).fieldOf("defects").forGetter(StoneProperties::defects)
    ).apply(inst, StoneProperties::new));

    public static final StreamCodec<io.netty.buffer.ByteBuf, StoneProperties> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.VAR_INT, StoneProperties::lithology,
            ByteBufCodecs.VAR_INT, StoneProperties::weathering,
            ByteBufCodecs.VAR_INT, StoneProperties::mechanics,
            ByteBufCodecs.VAR_INT, StoneProperties::knapPerf,
            ByteBufCodecs.VAR_INT, StoneProperties::defects,
            StoneProperties::new);

    /** 中度及以上风化无法打制。 */
    public boolean canKnapp() {
        return weathering < WEATHERING_MODERATE;
    }

    /** 随机生成风化等级：无 60% / 轻度 30% / 中度 8% / 重度 2%。 */
    public static int randomWeathering(RandomSource random) {
        int roll = random.nextInt(100);
        if (roll < 60) return WEATHERING_NONE;
        if (roll < 90) return WEATHERING_LIGHT;
        if (roll < 98) return WEATHERING_MODERATE;
        return WEATHERING_SEVERE;
    }

    /** 以模板为基准 ±10 随机浮动生成实例属性。 */
    public static StoneProperties generate(int lith, int mech, int perf, int def, RandomSource random) {
        return new StoneProperties(
                clamp(lith + random.nextInt(21) - 10),
                randomWeathering(random),
                clamp(mech + random.nextInt(21) - 10),
                clamp(perf + random.nextInt(21) - 10),
                clamp(def + random.nextInt(21) - 10));
    }

    private static int clamp(int v) {
        return Math.max(0, Math.min(100, v));
    }
}
