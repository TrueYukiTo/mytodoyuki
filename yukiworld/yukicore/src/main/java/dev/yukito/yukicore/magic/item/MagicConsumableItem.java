package dev.yukito.yukicore.magic.item;

import dev.yukito.yukicore.item.CoreItem;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.School;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Level;

import java.util.List;

/** Расходники магии: зелья маны, эссенции стихий, кристалл маны, свиток забвения. */
public class MagicConsumableItem extends CoreItem {
    public enum Kind { MANA_POTION, GREATER_MANA_POTION, FROST_ESSENCE, FLAME_ESSENCE, MANA_CRYSTAL, RESPEC,
        GREATER_FROST_ESSENCE, GREATER_FLAME_ESSENCE, KNOWLEDGE_ICE, KNOWLEDGE_FIRE }

    private final Kind kind;

    public MagicConsumableItem(Properties props, Kind kind, boolean foil) {
        super(props, foil);
        this.kind = kind;
    }

    private boolean potion() {
        return kind == Kind.MANA_POTION || kind == Kind.GREATER_MANA_POTION;
    }

    @Override
    public UseAnim getUseAnimation(ItemStack stack) {
        return potion() ? UseAnim.DRINK : UseAnim.BOW;
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return potion() ? 24 : 20;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return InteractionResultHolder.consume(player.getItemInHand(hand));
    }

    @Override
    public ItemStack finishUsingItem(ItemStack stack, Level level, LivingEntity entity) {
        if (!(entity instanceof ServerPlayer p)) return stack;
        MagicData d = Magic.get(p);
        switch (kind) {
            case MANA_POTION -> d.mana = Math.min(Magic.maxMana(p), d.mana + 60);
            case GREATER_MANA_POTION -> d.mana = Math.min(Magic.maxMana(p), d.mana + 220);
            case FROST_ESSENCE -> Magic.addXp(p, School.ICE, 250);
            case FLAME_ESSENCE -> Magic.addXp(p, School.FIRE, 250);
            case MANA_CRYSTAL -> {
                if (d.crystals >= 10) {
                    p.displayClientMessage(Component.translatable("message.yukicore.crystal_max").withStyle(ChatFormatting.RED), true);
                    return stack;
                }
                d.crystals++;
            }
            case RESPEC -> Magic.respec(p);
            case GREATER_FROST_ESSENCE -> Magic.addXp(p, School.ICE, 3000);
            case GREATER_FLAME_ESSENCE -> Magic.addXp(p, School.FIRE, 3000);
            case KNOWLEDGE_ICE, KNOWLEDGE_FIRE -> {
                int i = kind == Kind.KNOWLEDGE_ICE ? 0 : 1;
                if (d.bonusPoints[i] >= 10) {
                    p.displayClientMessage(Component.translatable("message.yukicore.knowledge_max").withStyle(ChatFormatting.RED), true);
                    return stack;
                }
                d.bonusPoints[i]++;
            }
        }
        d.dirty = true;
        ServerLevel s = p.serverLevel();
        s.sendParticles(ParticleTypes.ENCHANT, p.getX(), p.getY() + 1, p.getZ(), 40, 0.5, 1, 0.5, 0.5);
        s.playSound(null, p.blockPosition(), potion() ? SoundEvents.GENERIC_DRINK : SoundEvents.ENCHANTMENT_TABLE_USE,
                SoundSource.PLAYERS, 1f, 1.2f);
        if (!p.getAbilities().instabuild) {
            stack.shrink(1);
            if (potion()) p.getInventory().add(new ItemStack(Items.GLASS_BOTTLE));
        }
        return stack;
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, ctx, tooltip, flag);
    }
}
