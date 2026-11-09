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

    /** Клинок Юки: финальное оружие, неразрушимый. */
    public static final Tier YUKI = new SimpleTier(
            BlockTags.INCORRECT_FOR_NETHERITE_TOOL,
            4000, 12.0f, 6.0f, 30,
            () -> Ingredient.of(ModItems.YUKI_INFINITY_STAR.get()));

    private ModTiers() {}
}
