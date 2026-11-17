package dev.yukito.yukicore.magic;

import dev.yukito.yukicore.magic.item.FocusItem;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;

/** Сотворение заклинаний на сервере: проверки фокуса, маны и перезарядки, опыт. */
public final class SpellCaster {
    private SpellCaster() {}

    /** Клавиша «Сотворить»: заклинание из активной ячейки (или из указанной). */
    public static void castFromKey(ServerPlayer p, int slot) {
        MagicData d = Magic.get(p);
        Skill s = d.slotSkill(slot >= 0 && slot < MagicData.SLOTS ? slot : d.activeSlot);
        if (s == null) {
            p.displayClientMessage(Component.translatable("magic.yukicore.no_spell").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        cast(p, s);
    }

    /** ПКМ фокусом: активная ячейка, если школа подходит, иначе первое изученное заклинание школы фокуса. */
    public static void castFromItem(ServerPlayer p, FocusItem focus) {
        MagicData d = Magic.get(p);
        Skill s = d.activeSkill();
        if (s == null || !focus.supports(s.school)) {
            s = null;
            for (int i = 0; i < MagicData.SLOTS && s == null; i++) {
                Skill c = d.slotSkill(i);
                if (c != null && focus.supports(c.school)) s = c;
            }
            for (Skill c : Skill.values()) {
                if (s == null && !c.passive() && d.rank(c) > 0 && focus.supports(c.school)) s = c;
            }
        }
        if (s == null) {
            p.displayClientMessage(Component.translatable("magic.yukicore.learn_first").withStyle(ChatFormatting.GRAY), true);
            return;
        }
        cast(p, s);
    }

    public static boolean cast(ServerPlayer p, Skill s) {
        MagicData d = Magic.get(p);
        int rank = d.rank(s);
        if (rank <= 0 || s.passive()) return false;
        if (!Magic.hasFocus(p, s.school)) {
            p.displayClientMessage(Component.translatable("magic.yukicore.no_focus." + s.school.id).withStyle(ChatFormatting.RED), true);
            return false;
        }
        long now = p.level().getGameTime();
        if (d.cooldownUntil[s.ordinal()] > now) {
            p.displayClientMessage(Component.translatable("magic.yukicore.cooldown",
                    Component.translatable("spell.yukicore." + s.id()),
                    String.format("%.1f", (d.cooldownUntil[s.ordinal()] - now) / 20f)).withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        int cost = Magic.cost(p, s);
        boolean free = p.getAbilities().instabuild;
        if (!free && d.mana < cost) {
            p.displayClientMessage(Component.translatable("magic.yukicore.no_mana").withStyle(ChatFormatting.BLUE), true);
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS, 0.5f, 1.8f);
            return false;
        }
        if (!Spells.cast(p, s, rank, Magic.power(p, s))) {
            p.displayClientMessage(Component.translatable("magic.yukicore.no_target").withStyle(ChatFormatting.GRAY), true);
            return false;
        }
        if (!free) d.mana -= cost;
        d.cooldownUntil[s.ordinal()] = now + Magic.cooldown(p, s);
        d.dirty = true;
        Magic.addXp(p, s.school, Math.max(3, cost / 2));
        dev.yukito.yukicore.world.Contracts.onCast(p);
        p.swing(InteractionHand.MAIN_HAND, true);
        return true;
    }
}
