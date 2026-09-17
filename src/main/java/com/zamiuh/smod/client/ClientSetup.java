package com.zamiuh.smod.client;

import com.zamiuh.smod.SMod;
import com.zamiuh.smod.registry.ModMenus;
import com.zamiuh.smod.screen.KnappingTableScreen;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RegisterMenuScreensEvent;

/**
 * 客户端事件注册（仅客户端加载）。
 */
@EventBusSubscriber(modid = SMod.MODID, bus = EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public final class ClientSetup {

    @SubscribeEvent
    public static void onRegisterMenuScreens(RegisterMenuScreensEvent event) {
        event.register(ModMenus.KNAPPING_TABLE.get(), KnappingTableScreen::new);
    }

    private ClientSetup() {
    }
}
