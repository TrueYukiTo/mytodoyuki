package dev.yukito.yukicore.altar;

import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeInput;

import java.util.List;

/** Содержимое алтаря: центральный предмет, предметы на пьедесталах и уровень структуры. */
public record AltarInput(ItemStack center, List<ItemStack> pedestals, int tier) implements RecipeInput {
    @Override
    public ItemStack getItem(int index) {
        return index == 0 ? center : pedestals.get(index - 1);
    }

    @Override
    public int size() {
        return 1 + pedestals.size();
    }
}
