package com.zamiuh.smod.registry;

import com.zamiuh.smod.SMod;
import com.zamiuh.smod.menu.KnappingTableMenu;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.inventory.MenuType;
import net.neoforged.neoforge.common.extensions.IMenuTypeExtension;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 菜单注册（docs/14 v0.1.1）。
 */
public final class ModMenus {
    public static final DeferredRegister<MenuType<?>> MENUS =
            DeferredRegister.create(Registries.MENU, SMod.MODID);

    public static final DeferredHolder<MenuType<?>, MenuType<KnappingTableMenu>> KNAPPING_TABLE =
            MENUS.register("knapping_table", () -> IMenuTypeExtension.create(KnappingTableMenu::new));

    private ModMenus() {
    }
}
