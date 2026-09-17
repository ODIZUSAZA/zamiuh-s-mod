package com.zamiuh.smod;

import com.zamiuh.smod.network.KnappingPayloads;
import com.zamiuh.smod.registry.ModBlockEntities;
import com.zamiuh.smod.registry.ModBlocks;
import com.zamiuh.smod.registry.ModCreativeTabs;
import com.zamiuh.smod.registry.ModDataComponents;
import com.zamiuh.smod.registry.ModItems;
import com.zamiuh.smod.registry.ModMenus;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

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
        ModDataComponents.DATA_COMPONENTS.register(modEventBus);
        ModMenus.MENUS.register(modEventBus);
        ModCreativeTabs.CREATIVE_MODE_TABS.register(modEventBus);
        modEventBus.addListener(SMod::registerPayloads);
    }

    /** 打制小游戏网络包注册（v0.1.1）。 */
    private static void registerPayloads(RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        registrar.playToServer(KnappingPayloads.AttachToolPayload.TYPE, KnappingPayloads.AttachToolPayload.STREAM_CODEC, KnappingPayloads::handleAttach);
        registrar.playToServer(KnappingPayloads.PutToolPayload.TYPE, KnappingPayloads.PutToolPayload.STREAM_CODEC, KnappingPayloads::handlePut);
        registrar.playToServer(KnappingPayloads.StrikePayload.TYPE, KnappingPayloads.StrikePayload.STREAM_CODEC, KnappingPayloads::handleStrike);
        registrar.playToServer(KnappingPayloads.TakeResultPayload.TYPE, KnappingPayloads.TakeResultPayload.STREAM_CODEC, KnappingPayloads::handleTake);
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }
}
