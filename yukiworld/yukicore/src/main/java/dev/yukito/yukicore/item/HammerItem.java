package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

/** Молот: копает 3×3 (логика в {@link dev.yukito.yukicore.HammerHandler}); с Shift — один блок. */
public class HammerItem extends PickaxeItem {
    public HammerItem(Tier tier, Properties properties) {
        super(tier, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.yukicore.hammer").withStyle(ChatFormatting.AQUA));
    }
}
