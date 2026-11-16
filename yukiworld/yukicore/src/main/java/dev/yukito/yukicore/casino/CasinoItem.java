package dev.yukito.yukicore.casino;

import dev.yukito.yukicore.item.CoreItem;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** Кости Синтары (ставка 1 фишка) и Огненный сундук удачи. */
public class CasinoItem extends CoreItem {
    private final boolean dice;

    public CasinoItem(Properties props, boolean dice, boolean foil) {
        super(props, foil);
        this.dice = dice;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player instanceof ServerPlayer sp) {
            if (dice) {
                Casino.dice(sp);
                sp.getCooldowns().addCooldown(this, 15);
            } else {
                if (!sp.getAbilities().instabuild) stack.shrink(1);
                Casino.openBox(sp);
            }
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }
}
