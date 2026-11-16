package dev.yukito.yukicore.magic.item;

import dev.yukito.yukicore.item.YukiArmorItem;
import dev.yukito.yukicore.magic.School;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import javax.annotation.Nullable;
import java.util.List;

/** Мантии магов (3D): криомант/пиромант и регалии Юки/Синтары. Полный комплект — +15% силы сверху. */
public class RobeItem extends YukiArmorItem {
    @Nullable
    public final School school;
    public final float power, cheap, mana;

    public RobeItem(Holder<ArmorMaterial> material, Type type, Properties props, @Nullable School school,
                    float power, float cheap, float mana, Tier tier) {
        super(material, type, props, tier);
        this.school = school;
        this.power = power;
        this.cheap = cheap;
        this.mana = mana;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        String s = school == null ? "both" : school.id;
        tooltip.add(Component.translatable("tooltip.yukicore.robe." + s, Math.round(power * 100), Math.round(cheap * 100),
                Math.round(mana)).withStyle(school == School.FIRE ? ChatFormatting.GOLD : ChatFormatting.AQUA));
        super.appendHoverText(stack, ctx, tooltip, flag);
    }
}
