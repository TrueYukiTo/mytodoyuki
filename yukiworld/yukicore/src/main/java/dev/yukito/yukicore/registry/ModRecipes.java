package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.altar.AltarRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModRecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, YukiCore.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS =
            DeferredRegister.create(Registries.RECIPE_SERIALIZER, YukiCore.MOD_ID);

    public static final DeferredHolder<RecipeType<?>, RecipeType<AltarRecipe>> ALTAR_TYPE =
            TYPES.register("altar", () -> RecipeType.simple(YukiCore.id("altar")));
    public static final DeferredHolder<RecipeSerializer<?>, AltarRecipe.Serializer> ALTAR_SERIALIZER =
            SERIALIZERS.register("altar", AltarRecipe.Serializer::new);

    private ModRecipes() {}
}
