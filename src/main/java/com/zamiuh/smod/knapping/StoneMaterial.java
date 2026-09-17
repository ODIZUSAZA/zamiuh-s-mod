package com.zamiuh.smod.knapping;

import com.zamiuh.smod.component.StoneProperties;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;

import javax.annotation.Nullable;
import java.util.Map;

/**
 * 可打制石材模板（docs/13 v0.1.1）：
 * 每种原石定义五维隐藏属性的基准值，实例属性在放置到敲石台时按模板 ±10 随机生成。
 */
public enum StoneMaterial {
    FLINT(Items.FLINT, 85, 75, 90, 20),
    COBBLESTONE(Items.COBBLESTONE, 60, 55, 50, 45),
    ANDESITE(Items.ANDESITE, 55, 60, 45, 40),
    DIORITE(Items.DIORITE, 50, 65, 40, 35),
    GRANITE(Items.GRANITE, 45, 70, 35, 40),
    TUFF(Items.TUFF, 70, 40, 65, 55),
    COBBLED_DEEPSLATE(Items.COBBLED_DEEPSLATE, 65, 60, 60, 35),
    BLACKSTONE(Items.BLACKSTONE, 60, 65, 55, 30);

    public final Item item;
    public final int lithology;   // 岩性成分
    public final int mechanics;   // 力学与断裂
    public final int knapPerf;    // 打制性能
    public final int defects;     // 缺陷与结构

    StoneMaterial(Item item, int lithology, int mechanics, int knapPerf, int defects) {
        this.item = item;
        this.lithology = lithology;
        this.mechanics = mechanics;
        this.knapPerf = knapPerf;
        this.defects = defects;
    }

    private static final Map<Item, StoneMaterial> BY_ITEM = Map.ofEntries(
            java.util.Arrays.stream(values()).map(m -> Map.entry(m.item, m)).toArray(Map.Entry[]::new));

    @Nullable
    public static StoneMaterial byItem(Item item) {
        return BY_ITEM.get(item);
    }

    /** 按模板生成实例属性（含随机风化）。 */
    public StoneProperties generate(RandomSource random) {
        return StoneProperties.generate(lithology, mechanics, knapPerf, defects, random);
    }
}
