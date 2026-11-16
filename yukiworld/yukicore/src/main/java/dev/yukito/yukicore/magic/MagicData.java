package dev.yukito.yukicore.magic;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

/** Магия игрока: мана, уровни школ, очки, ранги заклинаний и талантов, пройденные испытания. */
public class MagicData implements INBTSerializable<CompoundTag> {
    public static final int MAX_LEVEL = 30;

    public float mana = 100;
    public final int[] level = {1, 1};
    public final int[] xp = {0, 0};
    public final int[] points = {1, 1};
    public final int[] ranks = new int[Spell.values().length];
    public final int[] passives = new int[Passive.values().length];
    public final boolean[] trial = {false, false};
    public final long[] cooldownUntil = new long[Spell.values().length];
    public int selected = -1;
    public int crystals;
    public boolean dirty = true;

    public static int xpToNext(int level) {
        return 50 + 25 * level + 4 * level * level;
    }

    public int rank(Spell s) {
        return ranks[s.ordinal()];
    }

    public int passive(Passive p) {
        return passives[p.ordinal()];
    }

    public int level(School s) {
        return level[s.ordinal()];
    }

    public boolean hasAnySpell() {
        for (int r : ranks) if (r > 0) return true;
        return false;
    }

    public Spell selectedSpell() {
        Spell s = Spell.byId(selected);
        return s != null && rank(s) > 0 ? s : null;
    }

    /** Следующее открытое заклинание после выбранного (по кругу). */
    public Spell nextUnlocked(int dir) {
        int n = Spell.values().length;
        int start = selected < 0 ? (dir > 0 ? -1 : 0) : selected;
        for (int i = 1; i <= n; i++) {
            int idx = Math.floorMod(start + i * dir, n);
            if (ranks[idx] > 0) return Spell.values()[idx];
        }
        return null;
    }

    public void copyFrom(MagicData o) {
        mana = o.mana;
        System.arraycopy(o.level, 0, level, 0, 2);
        System.arraycopy(o.xp, 0, xp, 0, 2);
        System.arraycopy(o.points, 0, points, 0, 2);
        System.arraycopy(o.ranks, 0, ranks, 0, ranks.length);
        System.arraycopy(o.passives, 0, passives, 0, passives.length);
        System.arraycopy(o.trial, 0, trial, 0, 2);
        System.arraycopy(o.cooldownUntil, 0, cooldownUntil, 0, cooldownUntil.length);
        selected = o.selected;
        crystals = o.crystals;
        dirty = true;
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag t = new CompoundTag();
        t.putFloat("Mana", mana);
        t.putIntArray("Level", level);
        t.putIntArray("Xp", xp);
        t.putIntArray("Points", points);
        t.putIntArray("Ranks", ranks);
        t.putIntArray("Passives", passives);
        t.putBoolean("TrialIce", trial[0]);
        t.putBoolean("TrialFire", trial[1]);
        t.putLongArray("Cooldowns", cooldownUntil);
        t.putInt("Selected", selected);
        t.putInt("Crystals", crystals);
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag t) {
        mana = t.getFloat("Mana");
        copy(t.getIntArray("Level"), level);
        copy(t.getIntArray("Xp"), xp);
        copy(t.getIntArray("Points"), points);
        copy(t.getIntArray("Ranks"), ranks);
        copy(t.getIntArray("Passives"), passives);
        trial[0] = t.getBoolean("TrialIce");
        trial[1] = t.getBoolean("TrialFire");
        long[] cd = t.getLongArray("Cooldowns");
        System.arraycopy(cd, 0, cooldownUntil, 0, Math.min(cd.length, cooldownUntil.length));
        selected = t.contains("Selected") ? t.getInt("Selected") : -1;
        crystals = t.getInt("Crystals");
        for (int i = 0; i < 2; i++) level[i] = Math.max(1, level[i]);
        dirty = true;
    }

    private static void copy(int[] src, int[] dst) {
        System.arraycopy(src, 0, dst, 0, Math.min(src.length, dst.length));
    }
}
