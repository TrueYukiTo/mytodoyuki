package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Броня YukiCore: в подсказке — бонус полного комплекта. */
public class YukiArmorItem extends ArmorItem {
    private final String setKey;

    public YukiArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, String tier) {
        super(material, type, properties);
        this.setKey = "tooltip.yukicore.set." + tier;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable(setKey).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
