package dev.yukito.yukicore.compat.jei;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.altar.AltarRecipe;
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

public class AltarCategory implements IRecipeCategory<AltarRecipe> {
    public static final RecipeType<AltarRecipe> TYPE = RecipeType.create(YukiCore.MOD_ID, "altar", AltarRecipe.class);
    private static final int W = 160, H = 110, CX = 52, CY = 46;
    /** Позиции пьедесталов вокруг центра: сначала 4 по сторонам, потом 4 по диагонали. */
    private static final int[][] POS = {{0, -36}, {36, 0}, {0, 36}, {-36, 0}, {26, -26}, {26, 26}, {-26, 26}, {-26, -26}};

    private final IDrawable icon;
    private final IDrawable slot;

    public AltarCategory(IGuiHelper gui) {
        this.icon = gui.createDrawableItemStack(new ItemStack(ModBlocks.YUKI_ALTAR.get()));
        this.slot = gui.getSlotDrawable();
    }

    @Override
    public RecipeType<AltarRecipe> getRecipeType() {
        return TYPE;
    }

    @Override
    public Component getTitle() {
        return Component.translatable("jei.yukicore.altar");
    }

    @Override
    public int getWidth() {
        return W;
    }

    @Override
    public int getHeight() {
        return H;
    }

    @Override
    public IDrawable getIcon() {
        return icon;
    }

    @Override
    public void setRecipe(IRecipeLayoutBuilder b, AltarRecipe r, IFocusGroup focuses) {
        b.addSlot(RecipeIngredientRole.INPUT, CX, CY).setBackground(slot, -1, -1).addIngredients(r.center());
        for (int i = 0; i < r.pedestals().size(); i++) {
            b.addSlot(RecipeIngredientRole.INPUT, CX + POS[i][0], CY + POS[i][1]).setBackground(slot, -1, -1)
                    .addIngredients(r.pedestals().get(i));
        }
        b.addSlot(RecipeIngredientRole.OUTPUT, 132, CY).setBackground(slot, -1, -1).addItemStack(r.result());
    }

    @Override
    public void draw(AltarRecipe r, IRecipeSlotsView view, GuiGraphics g, double mouseX, double mouseY) {
        var font = Minecraft.getInstance().font;
        g.drawString(font, "→", 108, CY + 4, 0xFF8FD8FF, false);
        int color = switch (r.tier()) {
            case 3 -> 0xFFFF9EDC;
            case 2 -> 0xFFF2CF55;
            default -> 0xFF8FD8FF;
        };
        g.drawString(font, Component.translatable("jei.yukicore.altar.tier", r.tier()), 0, 0, color, false);
        g.drawString(font, Component.translatable("jei.yukicore.altar.time", r.duration() / 20), 0, H - 9, 0xFF808080, false);
    }
}
