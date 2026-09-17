package com.zamiuh.smod.item;

import com.zamiuh.smod.component.StoneToolStats;
import com.zamiuh.smod.registry.ModDataComponents;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * 打制工具（docs/12 v0.1.1）：
 * 全新工具类别「打制工具」，当前形态为其它石头，由敲石台打制获得。
 * 携带石质工具组件（smod:stone_tool），后续可扩展手柄等加成。
 */
public class KnappingToolItem extends Item {

    public KnappingToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        StoneToolStats stats = stack.get(ModDataComponents.STONE_TOOL.get());
        if (stats != null) {
            tooltip.add(Component.translatable("tooltip.smod.knappedness", stats.knappedness())
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.smod.sharpness", stats.sharpness())
                    .withStyle(ChatFormatting.GRAY));
            tooltip.add(Component.translatable("tooltip.smod.pointedness", stats.pointedness())
                    .withStyle(ChatFormatting.GRAY));
        }
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
