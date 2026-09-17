package com.zamiuh.smod.registry;

import com.zamiuh.smod.SMod;
import com.zamiuh.smod.component.StoneProperties;
import com.zamiuh.smod.component.StoneToolStats;
import net.minecraft.core.component.DataComponentType;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 数据组件注册（docs/14 v0.1.1）。
 */
public final class ModDataComponents {
    public static final DeferredRegister<DataComponentType<?>> DATA_COMPONENTS =
            DeferredRegister.create(Registries.DATA_COMPONENT_TYPE, SMod.MODID);

    /** 石材五维隐藏属性（挂在原石 / 成品上）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoneProperties>> STONE_PROPERTIES =
            DATA_COMPONENTS.register("stone_properties", () -> DataComponentType.<StoneProperties>builder()
                    .persistent(StoneProperties.CODEC)
                    .networkSynchronized(StoneProperties.STREAM_CODEC)
                    .build());

    /** 石质工具组件（打制产物，后续扩展手柄效果）。 */
    public static final DeferredHolder<DataComponentType<?>, DataComponentType<StoneToolStats>> STONE_TOOL =
            DATA_COMPONENTS.register("stone_tool", () -> DataComponentType.<StoneToolStats>builder()
                    .persistent(StoneToolStats.CODEC)
                    .networkSynchronized(StoneToolStats.STREAM_CODEC)
                    .build());

    private ModDataComponents() {
    }
}
