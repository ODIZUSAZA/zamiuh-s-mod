package com.zamiuh.smod.item;

import net.minecraft.world.item.SwordItem;
import net.minecraft.world.item.Tier;

/**
 * 石刀：原版缺失的原始工具（docs/12）。
 * 攻速更快、伤害更低的轻量武器。
 * TODO 事件监听：
 *  - 收割高草丛掉落 plant_fiber
 *  - 对动物强化皮革/羽毛掉落（+50%）
 */
public class StoneKnifeItem extends SwordItem {
    public StoneKnifeItem(Tier tier, Properties properties) {
        super(tier, properties.attributes(SwordItem.createAttributes(tier, 0.5F, -1.5F)));
    }
}
