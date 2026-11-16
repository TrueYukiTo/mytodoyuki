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

/** Формулы и операции магии: мана, сила, опыт, улучшения. Общие для сервера и клиента. */
public final class Magic {
    private Magic() {}

    public static MagicData get(Player p) {
        return p.getData(ModMagic.DATA);
    }

    // ---------------------------------------------------------------- снаряжение
    public static boolean robeSet(Player p, School s) {
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(p.getItemBySlot(slot).getItem() instanceof RobeItem r) || r.school != s) return false;
        }
        return true;
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
        float bonus = ring + (amulet ? 0.15f : 0);
        if (s == School.ICE && p.getItemBySlot(EquipmentSlot.HEAD).is(ModItems.CROWN_OF_YUKI.get())) bonus += 0.15f;
        if (robeSet(p, s)) bonus += 0.25f;
        if (s == School.FIRE && p.hasEffect(ModMagic.PHOENIX)) bonus += 0.3f;
        return bonus;
    }

    public static float maxMana(Player p) {
        MagicData d = get(p);
        float m = 100 + 20 * d.passive(Passive.ICE_HEART) + 4 * (d.level[0] + d.level[1] - 2) + 10 * d.crystals;
        if (robeSet(p, School.ICE) || robeSet(p, School.FIRE)) m += 40;
        return m;
    }

    public static float regenPerTick(Player p) {
        MagicData d = get(p);
        float perSec = 2f + 0.6f * d.passive(Passive.FIRE_WILL) + 0.05f * (d.level[0] + d.level[1]);
        if (robeSet(p, School.ICE) || robeSet(p, School.FIRE)) perSec += 1f;
        return perSec / 20f;
    }

    /** Множитель урона/силы заклинания. */
    public static float power(Player p, Spell s) {
        MagicData d = get(p);
        int rank = Math.max(1, d.rank(s));
        Passive pw = s.school == School.ICE ? Passive.PERMAFROST : Passive.SINTARA_HEAT;
        float base = 1f + 0.25f * (rank - 1);
        return base * (1f + 0.08f * d.passive(pw) + 0.015f * d.level(s.school) + heldFocus(p, s.school) + gear(p, s.school));
    }

    public static int cost(Player p, Spell s) {
        int c = s.cost(Math.max(1, get(p).rank(s)));
        return robeSet(p, s.school) ? Math.round(c * 0.8f) : c;
    }

    // ---------------------------------------------------------------- операции (сервер)
    public static void addXp(ServerPlayer p, School s, int amount) {
        if (amount <= 0) return;
        MagicData d = get(p);
        int i = s.ordinal();
        if (d.level[i] >= MagicData.MAX_LEVEL) return;
        d.xp[i] += amount;
        boolean up = false;
        while (d.level[i] < MagicData.MAX_LEVEL && d.xp[i] >= MagicData.xpToNext(d.level[i])) {
            d.xp[i] -= MagicData.xpToNext(d.level[i]);
            d.level[i]++;
            d.points[i] += d.level[i] % 5 == 0 ? 2 : 1;
            up = true;
        }
        if (d.level[i] >= MagicData.MAX_LEVEL) d.xp[i] = 0;
        d.dirty = true;
        if (up) {
            ChatFormatting c = s == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD;
            p.connection.send(new ClientboundSetTitlesAnimationPacket(5, 40, 10));
            p.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("magic.yukicore.levelup." + s.id,
                    d.level[i]).withStyle(c, ChatFormatting.BOLD)));
            p.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("magic.yukicore.levelup.points",
                    d.points[i]).withStyle(ChatFormatting.WHITE)));
            p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 1f, 1.4f);
            p.serverLevel().sendParticles(s == School.ICE ? ParticleTypes.SNOWFLAKE : ParticleTypes.FLAME,
                    p.getX(), p.getY() + 1, p.getZ(), 80, 0.6, 1, 0.6, 0.1);
        }
    }

    public static void select(ServerPlayer p, int spell) {
        MagicData d = get(p);
        Spell s = Spell.byId(spell);
        if (s != null && d.rank(s) > 0) {
            d.selected = spell;
            d.dirty = true;
        }
    }

    public static void upgrade(ServerPlayer p, int kind, int index) {
        MagicData d = get(p);
        if (kind == 0) {
            Spell s = Spell.byId(index);
            if (s == null) return;
            int i = s.school.ordinal();
            int r = d.rank(s);
            int cost = s.upgradeCost(r);
            if (r >= Spell.MAX_RANK || d.points[i] < cost) return;
            if (d.level[i] < s.requiredLevel + r * 2) return;
            if (s.ultimate() && !d.trial[i]) return;
            d.points[i] -= cost;
            d.ranks[s.ordinal()]++;
            if (d.selected < 0) d.selected = s.ordinal();
        } else if (kind == 1) {
            if (index < 0 || index >= Passive.values().length) return;
            Passive ps = Passive.values()[index];
            int i = ps.school.ordinal();
            if (d.passives[index] >= Passive.MAX_RANK || d.points[i] < 1) return;
            d.points[i]--;
            d.passives[index]++;
        } else {
            return;
        }
        d.dirty = true;
        p.serverLevel().playSound(null, p.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 1f, 1.3f);
    }

    /** Свиток забвения: вернуть все очки. */
    public static void respec(ServerPlayer p) {
        MagicData d = get(p);
        for (Spell s : Spell.values()) {
            int i = s.school.ordinal();
            for (int r = d.ranks[s.ordinal()]; r > 0; r--) d.points[i] += s.upgradeCost(r - 1);
            d.ranks[s.ordinal()] = 0;
        }
        for (Passive ps : Passive.values()) {
            d.points[ps.school.ordinal()] += d.passives[ps.ordinal()];
            d.passives[ps.ordinal()] = 0;
        }
        d.selected = -1;
        d.dirty = true;
    }

    public static void sync(ServerPlayer p) {
        MagicData d = get(p);
        d.dirty = false;
        PacketDistributor.sendToPlayer(p, new MagicNet.Sync(d.serializeNBT(p.registryAccess())));
    }
}
