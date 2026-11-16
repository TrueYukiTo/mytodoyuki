package dev.yukito.yukicore.compat.jei;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.altar.AltarRecipe;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModRecipes;
import mezz.jei.api.IModPlugin;
import mezz.jei.api.JeiPlugin;
import mezz.jei.api.registration.IRecipeCatalystRegistration;
import mezz.jei.api.registration.IRecipeCategoryRegistration;
import mezz.jei.api.registration.IRecipeRegistration;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeHolder;

import java.util.List;

/** JEI: показывает ритуалы Алтаря Юки. Загружается только если JEI установлен. */
@JeiPlugin
public class YukiJeiPlugin implements IModPlugin {
    @Override
    public ResourceLocation getPluginUid() {
        return YukiCore.id("jei");
    }

    @Override
    public void registerCategories(IRecipeCategoryRegistration r) {
        r.addRecipeCategories(new AltarCategory(r.getJeiHelpers().getGuiHelper()));
    }

    @Override
    public void registerRecipes(IRecipeRegistration r) {
        if (Minecraft.getInstance().level == null) return;
        List<AltarRecipe> recipes = Minecraft.getInstance().level.getRecipeManager()
                .getAllRecipesFor(ModRecipes.ALTAR_TYPE.get()).stream().map(RecipeHolder::value).toList();
        r.addRecipes(AltarCategory.TYPE, recipes);
    }

    @Override
    public void registerRecipeCatalysts(IRecipeCatalystRegistration r) {
        r.addRecipeCatalyst(new ItemStack(ModBlocks.YUKI_ALTAR.get()), AltarCategory.TYPE);
        r.addRecipeCatalyst(new ItemStack(ModBlocks.YUKI_PEDESTAL.get()), AltarCategory.TYPE);
    }
}
