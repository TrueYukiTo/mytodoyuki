package dev.yukito.yukicore.item;

import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.CustomData;
import net.minecraft.world.level.Level;

import java.util.List;

/** Адвент-календарь Юки: раз в игровой день открывает новое окошко с подарком; на 7-й день — большой подарок. */
public class AdventCalendarItem extends CoreItem {
    public AdventCalendarItem(Properties properties) {
        super(properties, true);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (level.isClientSide) return InteractionResultHolder.success(stack);
        long day = level.getDayTime() / 24000L;
        CompoundTag tag = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag();
        if (tag.contains("last") && tag.getLong("last") >= day) {
            player.displayClientMessage(Component.translatable("message.yukicore.calendar.wait")
                    .withStyle(ChatFormatting.GRAY), true);
            return InteractionResultHolder.fail(stack);
        }
        int opened = tag.getInt("opened") + 1;
        tag.putLong("last", day);
        tag.putInt("opened", opened);
        stack.set(DataComponents.CUSTOM_DATA, CustomData.of(tag));
        ItemStack gift = new ItemStack(opened % 7 == 0 ? ModItems.GRAND_GIFT.get() : ModItems.GIFT.get());
        if (!player.addItem(gift)) player.drop(gift, false);
        level.playSound(null, player.blockPosition(), SoundEvents.NOTE_BLOCK_CHIME.value(), SoundSource.PLAYERS, 1f, 1.5f);
        player.displayClientMessage(Component.translatable("message.yukicore.calendar.open", opened)
                .withStyle(ChatFormatting.GOLD), true);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);
        int opened = stack.getOrDefault(DataComponents.CUSTOM_DATA, CustomData.EMPTY).copyTag().getInt("opened");
        tooltip.add(Component.translatable("tooltip.yukicore.calendar", opened).withStyle(ChatFormatting.GOLD));
    }
}
