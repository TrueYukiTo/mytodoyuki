package dev.yukito.yukicore.registry;

import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.common.SimpleTier;

public final class ModTiers {
    /** Между алмазом и незеритом: быстрее копает, хорошо зачаровывается. */
    public static final Tier FROST = new SimpleTier(
            BlockTags.INCORRECT_FOR_DIAMOND_TOOL,
            1800, 9.5f, 3.5f, 20,
            () -> Ingredient.of(ModItems.FROST_INGOT.get()));

    private ModTiers() {}
}
