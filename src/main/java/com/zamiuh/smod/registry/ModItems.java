package com.zamiuh.smod.registry;

import com.zamiuh.smod.SMod;
import com.zamiuh.smod.item.FireDrillItem;
import com.zamiuh.smod.item.ModTiers;
import com.zamiuh.smod.item.StoneKnifeItem;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.ShovelItem;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

/**
 * 物品注册（对应 docs/11_物品_原始材料.md、docs/12_物品_石器工具.md）
 */
public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(SMod.MODID);

    // ==================== 原始材料 ====================
    public static final DeferredItem<Item> FLINT_SHARD = ITEMS.register("flint_shard",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POLISHED_FLINT = ITEMS.register("polished_flint",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static final DeferredItem<Item> PLANT_FIBER = ITEMS.register("plant_fiber",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> FIBER_ROPE = ITEMS.register("fiber_rope",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> BARK = ITEMS.register("bark",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> RESIN = ITEMS.register("resin",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> WET_CLAY_POT = ITEMS.register("wet_clay_pot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> CLAY_POT = ITEMS.register("clay_pot",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));

    // ==================== 晾晒产物 ====================
    public static final DeferredItem<Item> DRIED_FIBER = ITEMS.register("dried_fiber",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DRIED_CLAY_POT = ITEMS.register("dried_clay_pot",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> DRIED_MEAT = ITEMS.register("dried_meat",
            () -> new Item(new Item.Properties().food(new FoodProperties.Builder()
                    .nutrition(4).saturationModifier(0.6F).build())));

    // ==================== 工具头（敲石台打制产物） ====================
    public static final DeferredItem<Item> STONE_AXE_HEAD = ITEMS.register("stone_axe_head",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STONE_PICKAXE_HEAD = ITEMS.register("stone_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> STONE_SHOVEL_HEAD = ITEMS.register("stone_shovel_head",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POLISHED_AXE_HEAD = ITEMS.register("polished_axe_head",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POLISHED_PICKAXE_HEAD = ITEMS.register("polished_pickaxe_head",
            () -> new Item(new Item.Properties()));
    public static final DeferredItem<Item> POLISHED_SHOVEL_HEAD = ITEMS.register("polished_shovel_head",
            () -> new Item(new Item.Properties()));

    // ==================== 工具 ====================
    public static final DeferredItem<AxeItem> STONE_AXE = ITEMS.register("stone_axe",
            () -> new AxeItem(ModTiers.FLINT, new Item.Properties()));
    public static final DeferredItem<PickaxeItem> STONE_PICKAXE = ITEMS.register("stone_pickaxe",
            () -> new PickaxeItem(ModTiers.FLINT, new Item.Properties()));
    public static final DeferredItem<ShovelItem> STONE_SHOVEL = ITEMS.register("stone_shovel",
            () -> new ShovelItem(ModTiers.FLINT, new Item.Properties()));
    public static final DeferredItem<StoneKnifeItem> STONE_KNIFE = ITEMS.register("stone_knife",
            () -> new StoneKnifeItem(ModTiers.FLINT, new Item.Properties().stacksTo(1)));
    public static final DeferredItem<AxeItem> POLISHED_AXE = ITEMS.register("polished_axe",
            () -> new AxeItem(ModTiers.POLISHED_FLINT, new Item.Properties()));
    public static final DeferredItem<PickaxeItem> POLISHED_PICKAXE = ITEMS.register("polished_pickaxe",
            () -> new PickaxeItem(ModTiers.POLISHED_FLINT, new Item.Properties()));
    public static final DeferredItem<ShovelItem> POLISHED_SHOVEL = ITEMS.register("polished_shovel",
            () -> new ShovelItem(ModTiers.POLISHED_FLINT, new Item.Properties()));
    public static final DeferredItem<FireDrillItem> FIRE_DRILL = ITEMS.register("fire_drill",
            () -> new FireDrillItem(new Item.Properties().stacksTo(1)));

    // ==================== 方块物品 ====================
    public static final DeferredItem<Item> KNAPPING_TABLE = ITEMS.registerSimpleBlockItem(ModBlocks.KNAPPING_TABLE);
    public static final DeferredItem<Item> FIRE_PIT = ITEMS.registerSimpleBlockItem(ModBlocks.FIRE_PIT);
    public static final DeferredItem<Item> DRYING_RACK = ITEMS.registerSimpleBlockItem(ModBlocks.DRYING_RACK);
    public static final DeferredItem<Item> PIT_KILN = ITEMS.registerSimpleBlockItem(ModBlocks.PIT_KILN);
    public static final DeferredItem<Item> WICKER_BASKET = ITEMS.registerSimpleBlockItem(ModBlocks.WICKER_BASKET);

    private ModItems() {
    }
}
