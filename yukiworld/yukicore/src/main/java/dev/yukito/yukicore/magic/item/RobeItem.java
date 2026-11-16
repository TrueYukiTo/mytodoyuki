package dev.yukito.yukicore.magic.item;

import dev.yukito.yukicore.magic.School;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Мантия мага (криомант / пиромант): полный комплект усиливает свою школу. */
public class RobeItem extends ArmorItem {
    public final School school;

    public RobeItem(Holder<ArmorMaterial> material, Type type, Properties props, School school) {
        super(material, type, props);
        this.school = school;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.yukicore.robe." + school.id).withStyle(
                school == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD));
        tooltip.add(Component.translatable("tooltip.yukicore.robe.set").withStyle(ChatFormatting.GRAY));
    }
}
