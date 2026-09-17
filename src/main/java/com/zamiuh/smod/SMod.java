package com.zamiuh.smod;

import com.zamiuh.smod.registry.ModBlockEntities;
import com.zamiuh.smod.registry.ModBlocks;
import com.zamiuh.smod.registry.ModCreativeTabs;
import com.zamiuh.smod.registry.ModItems;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;

/**
 * Zamiuh's Mod 主类
 * 当前阶段：原始时期（石器 / 篝火 / 陶器雏形）
 */
@Mod(SMod.MODID)
public class SMod {
    public static final String MODID = "smod";

    public SMod(IEventBus modEventBus) {
        ModItems.ITEMS.register(modEventBus);
        ModBlocks.BLOCKS.register(modEventBus);
        ModBlockEntities.BLOCK_ENTITIES.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
    }
}
