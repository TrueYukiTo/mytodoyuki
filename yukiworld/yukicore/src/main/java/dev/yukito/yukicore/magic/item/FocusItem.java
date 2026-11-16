package dev.yukito.yukicore.magic.item;

import dev.yukito.yukicore.item.CoreItem;
import dev.yukito.yukicore.magic.School;
import dev.yukito.yukicore.magic.SpellCaster;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Фокус магии: гримуар, фолиант или посох. Пока он в инвентаре — можно творить заклинания своей школы.
 * ПКМ — сотворить выбранное заклинание, Shift+ПКМ — открыть Гримуар стихий (дерево навыков).
 * В руке посох усиливает заклинания своей школы.
 */
public class FocusItem extends CoreItem {
    @Nullable
    private final School school;
    private final float bonus;

    public FocusItem(Properties props, @Nullable School school, float bonus, boolean foil) {
        super(props, foil);
        this.school = school;
        this.bonus = bonus;
    }

    public boolean supports(School s) {
        return school == null || school == s;
    }

    public float bonus() {
        return bonus;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (level.isClientSide) dev.yukito.yukicore.client.magic.ClientMagic.openGrimoire();
            return InteractionResultHolder.success(stack);
        }
        if (player instanceof ServerPlayer sp) SpellCaster.castFromItem(sp, this);
        return InteractionResultHolder.consume(stack);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);
        String sk = school == null ? "both" : school.id;
        tooltip.add(Component.translatable("tooltip.yukicore.focus." + sk).withStyle(
                school == School.ICE ? ChatFormatting.AQUA : school == School.FIRE ? ChatFormatting.GOLD : ChatFormatting.LIGHT_PURPLE));
        if (bonus > 0) {
            tooltip.add(Component.translatable("tooltip.yukicore.focus.bonus", Math.round(bonus * 100))
                    .withStyle(ChatFormatting.GREEN));
        }
        tooltip.add(Component.translatable("tooltip.yukicore.focus.use").withStyle(ChatFormatting.GRAY));
    }
}
