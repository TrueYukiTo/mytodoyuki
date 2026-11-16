package dev.yukito.yukicore.item;

import net.minecraft.ChatFormatting;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.block.Block;

import java.util.List;

/** Блок-предмет с подсказкой из перевода «<ключ>.desc», если она есть. */
public class DescBlockItem extends BlockItem {
    public DescBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        String key = getDescriptionId() + ".desc";
        if (Language.getInstance().has(key)) tooltip.add(Component.translatable(key).withStyle(ChatFormatting.GRAY));
    }
}
