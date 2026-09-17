package com.zamiuh.smod.item;

import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;

/**
 * 工具材料等级（对应 docs/02_物品_石器工具.md 的属性对标表）
 * 前铁器工具不可修复：修复材料为空。
 */
public final class ModTiers {
    /** 燧石：耐久 90，速度 3.5，攻击加成 1.0，附魔能力 8 */
    public static final Tier FLINT = new Tier() {
        @Override
        public int getUses() {
            return 90;
        }

        @Override
        public float getSpeed() {
            return 3.5F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 1.0F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_STONE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 8;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    /** 磨制燧石：耐久 180，速度 5.0，攻击加成 1.5，附魔能力 8 */
    public static final Tier POLISHED_FLINT = new Tier() {
        @Override
        public int getUses() {
            return 180;
        }

        @Override
        public float getSpeed() {
            return 5.0F;
        }

        @Override
        public float getAttackDamageBonus() {
            return 1.5F;
        }

        @Override
        public TagKey<Block> getIncorrectBlocksForDrops() {
            return BlockTags.INCORRECT_FOR_STONE_TOOL;
        }

        @Override
        public int getEnchantmentValue() {
            return 8;
        }

        @Override
        public Ingredient getRepairIngredient() {
            return Ingredient.EMPTY;
        }
    };

    private ModTiers() {
    }
}
