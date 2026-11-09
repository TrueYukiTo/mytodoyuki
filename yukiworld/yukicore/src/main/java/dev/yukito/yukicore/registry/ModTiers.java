package dev.yukito.yukicore.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

/** Уровни материалов YukiCore: Морозный → Ауроний → Целестий (выше незерита). */
public final class ModTiers {
    public static final Tier FROST = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            2600, 10.0f, 4.5f, 20, () -> Ingredient.of(ModItems.FROST_INGOT.get()));

    public static final Tier AURORIUM = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            4200, 12.5f, 6.0f, 24, () -> Ingredient.of(ModItems.AURORIUM_INGOT.get()));

    public static final Tier CELESTIUM = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            7000, 16.0f, 8.0f, 30, () -> Ingredient.of(ModItems.CELESTIUM_INGOT.get()));

    /** Клинок Юки: неразрушимый. */
    public static final Tier YUKI = new SimpleTier(BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            9999, 20.0f, 12.0f, 35, () -> Ingredient.of(ModItems.STAR_SHARD.get()));

    private ModTiers() {}
}
