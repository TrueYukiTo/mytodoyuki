package dev.yukito.yukicore.magic;

import dev.yukito.yukicore.magic.item.FocusItem;
import dev.yukito.yukicore.magic.item.MagicGearItem;
import dev.yukito.yukicore.magic.item.RobeItem;
import dev.yukito.yukicore.magic.net.MagicNet;
import dev.yukito.yukicore.registry.ModItems;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.PacketDistributor;

/** Формулы и операции магии: мана, сила, перезарядка, опыт, дерево навыков. Общие для сервера и клиента. */
public final class Magic {
    private Magic() {}

    private static final EquipmentSlot[] ARMOR = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    public static MagicData get(Player p) {
        return p.getData(ModMagic.DATA);
    }

    // ---------------------------------------------------------------- снаряжение
    /** Сумма бонусов надетых мантий школы: [сила, снижение цены, мана, кол-во частей]. */
    public static float[] robes(Player p, School s) {
        float power = 0, cheap = 0, mana = 0, n = 0;
        for (EquipmentSlot slot : ARMOR) {
            if (p.getItemBySlot(slot).getItem() instanceof RobeItem r && (r.school == null || r.school == s)) {
                power += r.power;
                cheap += r.cheap;
                mana += r.mana;
                n++;
            }
        }
        if (n == 4) power += 0.15f;
        return new float[]{power, cheap, mana, n};
    }

    public static boolean hasFocus(Player p, School s) {
        if (p.getAbilities().instabuild) return true;
        for (ItemStack st : p.getInventory().items) if (st.getItem() instanceof FocusItem f && f.supports(s)) return true;
        for (ItemStack st : p.getInventory().offhand) if (st.getItem() instanceof FocusItem f && f.supports(s)) return true;
        return false;
    }

    private static float heldFocus(Player p, School s) {
        float best = 0;
        for (ItemStack st : new ItemStack[]{p.getMainHandItem(), p.getOffhandItem()}) {
            if (st.getItem() instanceof FocusItem f && f.supports(s)) best = Math.max(best, f.bonus());
        }
        return best;
    }

    private static float gear(Player p, School s) {
        float ring = 0;
        boolean amulet = false;
        for (ItemStack st : p.getInventory().items) {
            if (st.getItem() instanceof MagicGearItem g && g.school == s) ring = Math.max(ring, g.power);
            if (s == School.FIRE && st.is(ModItems.SINTARA_AMULET.get())) amulet = true;
        }
        float bonus = ring + (amulet ? 0.15f : 0) + robes(p, s)[0];
        if (s == School.ICE && p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.CROWN_OF_YUKI.get())) bonus += 0.15f;
        if (s == School.FIRE && p.hasEffect(ModMagic.PHOENIX)) bonus += 0.3f;
        if (s == School.ICE && p.hasEffect(ModMagic.AVATAR_ICE)) bonus += 0.5f;
        if (s == School.FIRE && p.hasEffect(ModMagic.AVATAR_FIRE)) bonus += 0.5f;
        return bonus;
    }

    public static float maxMana(Player p) {
        MagicData d = get(p);
        float m = 100 + 5 * (d.level[0] + d.level[1] - 2) + 30 * d.rank(Skill.COLD_MIND)
                + 20 * d.rank(Skill.INNER_FIRE) + 10 * d.crystals;
        m += robes(p, School.ICE)[2] + robes(p, School.FIRE)[2];
        return m;
    }

    public static float regenPerTick(Player p) {
        MagicData d = get(p);
        float perSec = 3f + 1.0f * d.rank(Skill.BURNING_WILL) + 1.0f * d.rank(Skill.WINTER_HEART)
                + 0.08f * (d.level[0] + d.level[1]);
        perSec += 0.5f * (robes(p, School.ICE)[3] + robes(p, School.FIRE)[3]);
        return perSec / 20f;
    }

    /** Множитель силы заклинания. */
    public static float power(Player p, Skill s) {
        MagicData d = get(p);
        int rank = Math.max(1, d.rank(s));
        Skill pw = s.school == School.ICE ? Skill.PERMAFROST : Skill.SINTARA_HEAT;
        Skill mastery = s.school == School.ICE ? Skill.CRYO_MASTERY : Skill.PYRO_MASTERY;
        float base = 1f + 0.3f * (rank - 1);
        return growth(d.level(s.school)) * base * (1f + 0.1f * d.rank(pw) + 0.06f * d.rank(mastery) + 0.02f * d.level(s.school)
                + heldFocus(p, s.school) + gear(p, s.school));
    }

    /**
     * 7.3: сила растёт с уровнем школы — на 1-м уровне заклинания бьют как каменный меч (~20%),
     * на 30-м примерно вполовину, на 60-м — в полную силу.
     */
    public static float growth(int level) {
        return 0.2f + 0.8f * (float) Math.pow(Math.max(0, level) / (double) MagicData.MAX_LEVEL, 1.3);
    }

    public static int cost(Player p, Skill s) {
        Skill flow = s.school == School.ICE ? Skill.FROZEN_FLOW : Skill.KINDLING;
        float c = s.cost(get(p).rank(s)) * Math.max(0.3f, 1f - robes(p, s.school)[1] - 0.04f * get(p).rank(flow));
        if ((s.school == School.ICE && p.hasEffect(ModMagic.AVATAR_ICE)) || (s.school == School.FIRE && p.hasEffect(ModMagic.AVATAR_FIRE))) {
            c *= 0.25f;
        }
        return Math.round(c);
    }

    public static int cooldown(Player p, Skill s) {
        MagicData d = get(p);
        float k = 1f - 0.06f * d.rank(s.school == School.ICE ? Skill.ETERNAL_COLD : Skill.PHOENIX_SOUL);
        if ((s.school == School.ICE && p.hasEffect(ModMagic.AVATAR_ICE)) || (s.school == School.FIRE && p.hasEffect(ModMagic.AVATAR_FIRE))) {
            k *= 0.5f;
        }
        return Math.max(4, Math.round(s.cooldown(d.rank(s)) * k));
    }

    // ---------------------------------------------------------------- операции (сервер)
    public static void addXp(ServerPlayer p, School s, int amount) {
        if (amount <= 0) return;
        MagicData d = get(p);
        int i = s.ordinal();
        if (d.level[i] >= MagicData.MAX_LEVEL) return;
        d.xp[i] += amount;
        int before = d.level[i];
        while (d.level[i] < MagicData.MAX_LEVEL && d.xp[i] >= MagicData.xpToNext(d.level[i])) {
            d.xp[i] -= MagicData.xpToNext(d.level[i]);
            d.level[i]++;
        }
        if (d.level[i] >= MagicData.MAX_LEVEL) d.xp[i] = 0;
        d.dirty = true;
        if (d.level[i] > before) {
            ChatFormatting c = s == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD;
            p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 10));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("magic.yukicore.levelup." + s.id,
                    d.level[i]).withStyle(c, ChatFormatting.BOLD)));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("magic.yukicore.levelup.points",
                    d.points(s)).withStyle(ChatFormatting.WHITE)));
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1.4f);
            p.serverLevel().sendParticles(s == School.ICE ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME,
                    p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 1, 0.6, 0.1);
        }
    }

    /** Положить заклинание в ячейку быстрого доступа. */
    public static void assign(ServerPlayer p, int slot, int skill) {
        MagicData d = get(p);
        Skill s = Skill.byId(skill);
        if (slot < 0 || slot >= MagicData.SLOTS || s == null || s.passive() || d.rank(s) <= 0) return;
        for (int i = 0; i < MagicData.SLOTS; i++) if (d.slots[i] == skill) d.slots[i] = -1;
        d.slots[slot] = skill;
        d.activeSlot = slot;
        d.dirty = true;
    }

    public static void setActiveSlot(ServerPlayer p, int slot) {
        MagicData d = get(p);
        if (slot >= 0 && slot < MagicData.SLOTS) {
            d.activeSlot = slot;
            d.dirty = true;
        }
    }

    public static void upgrade(ServerPlayer p, int index) {
        MagicData d = get(p);
        Skill s = Skill.byId(index);
        if (s == null || !d.canUpgrade(s)) return;
        d.ranks[s.ordinal()]++;
        if (!s.passive() && d.ranks[s.ordinal()] == 1) {
            for (int i = 0; i < MagicData.SLOTS; i++) {
                if (d.slots[i] < 0) {
                    d.slots[i] = s.ordinal();
                    break;
                }
            }
        }
        d.dirty = true;
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1f, 1.3f);
        p.serverLevel().sendParticles(s.school == School.ICE ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME,
                p.getX(), p.getY() + 1, p.getZ(), 30, 0.5, 0.8, 0.5, 0.05);
    }

    /** 7.3: очков стало меньше — если игрок потратил больше, чем есть, дерево школы сбрасывается и очки возвращаются. */
    public static void rebuildIfOverspent(ServerPlayer p) {
        MagicData d = get(p);
        for (School s : School.values()) {
            if (d.points(s) >= 0) continue;
            for (Skill k : Skill.values()) if (k.school == s) d.ranks[k.ordinal()] = 0;
            for (int i = 0; i < MagicData.SLOTS; i++) {
                Skill k = Skill.byId(d.slots[i]);
                if (k != null && k.school == s) d.slots[i] = -1;
            }
            d.dirty = true;
            p.sendSystemMessage(Component.translatable("magic.yukicore.rebuilt." + s.id, d.points(s))
                    .withStyle(s == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD));
        }
    }

    /** Свиток забвения: снять все навыки — очки вернутся сами. */
    public static void respec(ServerPlayer p) {
        MagicData d = get(p);
        java.util.Arrays.fill(d.ranks, 0);
        java.util.Arrays.fill(d.slots, -1);
        d.dirty = true;
    }

    public static void sync(ServerPlayer p) {
        MagicData d = get(p);
        d.dirty = false;
        PacketDistributor.sendToPlayer(p, new MagicNet.Sync(d.serializeNBT(p.registryAccess())));
    }
}
