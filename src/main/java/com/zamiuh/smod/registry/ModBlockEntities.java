package com.zamiuh.smod.registry;

import com.zamiuh.smod.SMod;
import com.zamiuh.smod.blockentity.DryingRackBlockEntity;
import com.zamiuh.smod.blockentity.FirePitBlockEntity;
import com.zamiuh.smod.blockentity.KnappingTableBlockEntity;
import com.zamiuh.smod.blockentity.PitKilnBlockEntity;
import com.zamiuh.smod.blockentity.WickerBasketBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块实体注册（对应 docs/04_方块实体_功能实体.md）
 */
public final class ModBlockEntities {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITIES =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, SMod.MODID);

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<KnappingTableBlockEntity>> KNAPPING_TABLE =
            BLOCK_ENTITIES.register("knapping_table", () -> BlockEntityType.Builder
                    .of(KnappingTableBlockEntity::new, ModBlocks.KNAPPING_TABLE.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<FirePitBlockEntity>> FIRE_PIT =
            BLOCK_ENTITIES.register("fire_pit", () -> BlockEntityType.Builder
                    .of(FirePitBlockEntity::new, ModBlocks.FIRE_PIT.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<DryingRackBlockEntity>> DRYING_RACK =
            BLOCK_ENTITIES.register("drying_rack", () -> BlockEntityType.Builder
                    .of(DryingRackBlockEntity::new, ModBlocks.DRYING_RACK.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<PitKilnBlockEntity>> PIT_KILN =
            BLOCK_ENTITIES.register("pit_kiln", () -> BlockEntityType.Builder
                    .of(PitKilnBlockEntity::new, ModBlocks.PIT_KILN.get()).build(null));

    public static final DeferredHolder<BlockEntityType<?>, BlockEntityType<WickerBasketBlockEntity>> WICKER_BASKET =
            BLOCK_ENTITIES.register("wicker_basket", () -> BlockEntityType.Builder
                    .of(WickerBasketBlockEntity::new, ModBlocks.WICKER_BASKET.get()).build(null));

    private ModBlockEntities() {
    }
}
