package com.zamiuh.smod.registry;

import com.zamiuh.smod.SMod;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 创造模式标签页：工业模组：原始时期
 */
public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, SMod.MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> PRIMITIVE_ERA =
            CREATIVE_MODE_TABS.register("primitive_era", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.smod.primitive_era"))
                    .icon(() -> ModItems.STONE_KNIFE.get().getDefaultInstance())
                    .displayItems((parameters, output) -> {
                        // 原始材料
                        output.accept(ModItems.FLINT_SHARD.get());
                        output.accept(ModItems.POLISHED_FLINT.get());
                        output.accept(ModItems.PLANT_FIBER.get());
                        output.accept(ModItems.DRIED_FIBER.get());
                        output.accept(ModItems.FIBER_ROPE.get());
                        output.accept(ModItems.BARK.get());
                        output.accept(ModItems.RESIN.get());
                        output.accept(ModItems.WET_CLAY_POT.get());
                        output.accept(ModItems.DRIED_CLAY_POT.get());
                        output.accept(ModItems.CLAY_POT.get());
                        output.accept(ModItems.DRIED_MEAT.get());
                        // 工具头
                        output.accept(ModItems.STONE_AXE_HEAD.get());
                        output.accept(ModItems.STONE_PICKAXE_HEAD.get());
                        output.accept(ModItems.STONE_SHOVEL_HEAD.get());
                        output.accept(ModItems.POLISHED_AXE_HEAD.get());
                        output.accept(ModItems.POLISHED_PICKAXE_HEAD.get());
                        output.accept(ModItems.POLISHED_SHOVEL_HEAD.get());
                        // 打制工具
                        output.accept(ModItems.KNAPPING_TOOL_STONE.get());
                        // 工具
                        output.accept(ModItems.STONE_AXE.get());
                        output.accept(ModItems.STONE_PICKAXE.get());
                        output.accept(ModItems.STONE_SHOVEL.get());
                        output.accept(ModItems.STONE_KNIFE.get());
                        output.accept(ModItems.POLISHED_AXE.get());
                        output.accept(ModItems.POLISHED_PICKAXE.get());
                        output.accept(ModItems.POLISHED_SHOVEL.get());
                        output.accept(ModItems.FIRE_DRILL.get());
                        // 方块
                        output.accept(ModBlocks.KNAPPING_TABLE.get());
                        output.accept(ModBlocks.FIRE_PIT.get());
                        output.accept(ModBlocks.DRYING_RACK.get());
                        output.accept(ModBlocks.PIT_KILN.get());
                        output.accept(ModBlocks.WICKER_BASKET.get());
                    })
                    .build());

    private ModCreativeTabs() {
    }
}
