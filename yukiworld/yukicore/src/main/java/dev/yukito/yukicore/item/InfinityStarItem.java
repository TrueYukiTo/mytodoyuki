package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/**
 * Бесконечная звезда Юки — финальный предмет YukiWorld.
 * Пока лежит в инвентаре: полёт, ночное зрение, сопротивление, насыщение
 * (логика в {@link dev.yukito.yukicore.InfinityStarHandler}).
 */
public class InfinityStarItem extends CoreItem {
    public InfinityStarItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("item.yukicore.yuki_infinity_star.desc").withStyle(ChatFormatting.LIGHT_PURPLE));
        tooltip.add(Component.translatable("item.yukicore.yuki_infinity_star.desc2").withStyle(ChatFormatting.AQUA));
    }
}
