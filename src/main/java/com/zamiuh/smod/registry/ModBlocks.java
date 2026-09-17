package com.zamiuh.smod.registry;

import com.zamiuh.smod.SMod;
import com.zamiuh.smod.block.DryingRackBlock;
import com.zamiuh.smod.block.FirePitBlock;
import com.zamiuh.smod.block.KnappingTableBlock;
import com.zamiuh.smod.block.PitKilnBlock;
import com.zamiuh.smod.block.WickerBasketBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.MapColor;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 方块注册（对应 docs/03_方块_原始工作站.md）
 */
public final class ModBlocks {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(SMod.MODID);

    public static final DeferredBlock<KnappingTableBlock> KNAPPING_TABLE = BLOCKS.register("knapping_table",
            () -> new KnappingTableBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(2.0F).sound(SoundType.WOOD)));
    public static final DeferredBlock<FirePitBlock> FIRE_PIT = BLOCKS.register("fire_pit",
            () -> new FirePitBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.STONE).strength(2.0F).sound(SoundType.STONE)
                    .lightLevel(state -> state.getValue(FirePitBlock.LIT) ? 15 : 0).noOcclusion()));
    public static final DeferredBlock<DryingRackBlock> DRYING_RACK = BLOCKS.register("drying_rack",
            () -> new DryingRackBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.WOOD).strength(1.5F).sound(SoundType.WOOD).noOcclusion()));
    public static final DeferredBlock<PitKilnBlock> PIT_KILN = BLOCKS.register("pit_kiln",
            () -> new PitKilnBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.DIRT).strength(1.5F).sound(SoundType.GRAVEL)));
    public static final DeferredBlock<WickerBasketBlock> WICKER_BASKET = BLOCKS.register("wicker_basket",
            () -> new WickerBasketBlock(BlockBehaviour.Properties.of()
                    .mapColor(MapColor.PLANT).strength(0.8F).sound(SoundType.GRASS).noOcclusion()));

    private ModBlocks() {
    }
}
