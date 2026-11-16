package dev.yukito.yukicore.compat.jei;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.multiblock.ForgeRecipe;
import dev.yukito.yukicore.registry.ModBlocks;
import mezz.jei.api.gui.builder.IRecipeLayoutBuilder;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.ingredient.IRecipeSlotsView;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.recipe.IFocusGroup;
import mezz.jei.api.recipe.RecipeIngredientRole;
import mezz.jei.api.recipe.RecipeType;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.util.Arrays;

public class ForgeCategory implements IRecipeCategory<ForgeRecipe> {
    public static final RecipeType<ForgeRecipe> TYPE = RecipeType.create(YukiCore.MOD_ID, "frost_forging", ForgeRecipe.class);
    private final IDrawable icon, slot;

    public ForgeCategory(IGuiHelper gui) {
        icon = gui.createDrawableItemStack(new ItemStack(ModBlocks.FROST_FORGE.get()));
        slot = gui.getSlotDrawable();
    }

    @Override public RecipeType<ForgeRecipe> getRecipeType() { return TYPE; }
    @Override public Component getTitle() { return Component.translatable("jei.yukicore.forge"); }
    @Override public int getWidth() { return 150; }
    @Override public int getHeight() { return 50; }
    @Override public IDrawable getIcon() { return icon; }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, ForgeRecipe r, IFocusGroup focuses) {
        for (int i = 0; i < r.ingredients().size(); i++) {
            var ing = r.ingredients().get(i);
            b.addSlot(RecipeIngredientRole.INPUT, 2 + i * 19, 6).setBackground(slot, -1, -1)
                    .addItemStacks(Arrays.stream(ing.getItems()).toList());
        }
        b.addSlot(RecipeIngredientRole.OUTPUT, 128, 6).setBackground(slot, -1, -1).addItemStack(r.result());
    }

    @Override
    public void draw(ForgeRecipe r, IRecipeSlotsView view, GuiGraphics g, double mx, double my) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, "→", 106, 10, 0xFF8FD8FF, false);
        g.drawString(font, Component.translatable("jei.yukicore.forge.energy", String.format("%,d", r.energy()),
                r.time() / 20), 0, 32, 0xFF808080, false);
    }
}
