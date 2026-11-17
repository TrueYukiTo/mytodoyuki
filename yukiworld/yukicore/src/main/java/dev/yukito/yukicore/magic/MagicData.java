package dev.yukito.yukicore.magic;

import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.neoforged.neoforge.common.util.INBTSerializable;

import java.util.Arrays;

/**
 * Магия игрока: мана, уровни школ (до 60), ранги навыков дерева, 6 ячеек быстрых заклинаний, испытания.
 * Очки навыков не хранятся — считаются из уровня минус потраченное, поэтому сброс и обновления безопасны.
 */
public class MagicData implements INBTSerializable<CompoundTag> {
    public static final int MAX_LEVEL = 60;
    public static final int SLOTS = 6;

    public float mana = 100;
    public final int[] level = {1, 1};
    public final int[] xp = {0, 0};
    public final int[] bonusPoints = {0, 0};
    public final int[] ranks = new int[Skill.values().length];
    public final boolean[] trial = {false, false};
    public final long[] cooldownUntil = new long[Skill.values().length];
    public final int[] slots = new int[SLOTS];
    public int activeSlot;
    public int crystals;
    public boolean dirty = true;
    /** Полёт выдан магией (крылья феникса / аватар) — снять, когда эффект кончится. */
    public transient boolean magicFlight;

    public MagicData() {
        Arrays.fill(slots, -1);
    }

    public static int xpToNext(int level) {
        return 60 + 25 * level + 3 * level * level;
    }

    public static int earnedPoints(int level) {
        return level + level / 10;
    }

    public int rank(Skill s) {
        return ranks[s.ordinal()];
    }

    public int level(School s) {
        return level[s.ordinal()];
    }

    public int spent(School s) {
        int n = 0;
        for (Skill k : Skill.values()) {
            if (k.school != s) continue;
            for (int r = 0; r < ranks[k.ordinal()]; r++) n += k.upgradeCost(r);
        }
        return n;
    }

    public int points(School s) {
        return earnedPoints(level(s)) + bonusPoints[s.ordinal()] - spent(s);
    }

    public boolean unlocked(Skill s) {
        for (Skill p : s.parents()) if (rank(p) <= 0) return false;
        return true;
    }

    public boolean canUpgrade(Skill s) {
        int r = rank(s), i = s.school.ordinal();
        return r < Skill.MAX_RANK && unlocked(s) && points(s.school) >= s.upgradeCost(r)
                && level[i] >= s.requiredLevel(r) && (!s.ultimate() || trial[i]);
    }

    public boolean hasAnySpell() {
        for (Skill s : Skill.values()) if (!s.passive() && rank(s) > 0) return true;
        return false;
    }

    public Skill slotSkill(int slot) {
        Skill s = Skill.byId(slots[slot]);
        return s != null && !s.passive() && rank(s) > 0 ? s : null;
    }

    public Skill activeSkill() {
        return slotSkill(activeSlot);
    }

    public void copyFrom(MagicData o) {
        deserializeNBT(null, o.serializeNBT(null));
    }

    @Override
    public CompoundTag serializeNBT(HolderLookup.Provider provider) {
        CompoundTag t = new CompoundTag();
        t.putFloat("Mana", mana);
        t.putIntArray("Level", level);
        t.putIntArray("Xp", xp);
        t.putIntArray("Bonus", bonusPoints);
        CompoundTag sk = new CompoundTag();
        for (Skill s : Skill.values()) if (ranks[s.ordinal()] > 0) sk.putInt(s.id(), ranks[s.ordinal()]);
        t.put("Skills", sk);
        CompoundTag cd = new CompoundTag();
        for (Skill s : Skill.values()) if (cooldownUntil[s.ordinal()] > 0) cd.putLong(s.id(), cooldownUntil[s.ordinal()]);
        t.put("Cooldowns", cd);
        CompoundTag sl = new CompoundTag();
        for (int i = 0; i < SLOTS; i++) {
            Skill s = Skill.byId(slots[i]);
            if (s != null) sl.putString(String.valueOf(i), s.id());
        }
        t.put("Slots", sl);
        t.putInt("ActiveSlot", activeSlot);
        t.putBoolean("TrialIce", trial[0]);
        t.putBoolean("TrialFire", trial[1]);
        t.putInt("Crystals", crystals);
        return t;
    }

    @Override
    public void deserializeNBT(HolderLookup.Provider provider, CompoundTag t) {
        mana = t.getFloat("Mana");
        copy(t.getIntArray("Level"), level);
        copy(t.getIntArray("Xp"), xp);
        copy(t.getIntArray("Bonus"), bonusPoints);
        Arrays.fill(ranks, 0);
        Arrays.fill(cooldownUntil, 0);
        Arrays.fill(slots, -1);
        CompoundTag sk = t.getCompound("Skills");
        for (String k : sk.getAllKeys()) {
            Skill s = Skill.byName(k);
            if (s != null) ranks[s.ordinal()] = Math.min(Skill.MAX_RANK, sk.getInt(k));
        }
        CompoundTag cd = t.getCompound("Cooldowns");
        for (String k : cd.getAllKeys()) {
            Skill s = Skill.byName(k);
            if (s != null) cooldownUntil[s.ordinal()] = cd.getLong(k);
        }
        CompoundTag sl = t.getCompound("Slots");
        for (String k : sl.getAllKeys()) {
            Skill s = Skill.byName(sl.getString(k));
            try {
                int i = Integer.parseInt(k);
                if (s != null && i >= 0 && i < SLOTS) slots[i] = s.ordinal();
            } catch (NumberFormatException ignored) {
            }
        }
        activeSlot = Math.max(0, Math.min(SLOTS - 1, t.getInt("ActiveSlot")));
        trial[0] = t.getBoolean("TrialIce");
        trial[1] = t.getBoolean("TrialFire");
        crystals = t.getInt("Crystals");
        for (int i = 0; i < 2; i++) level[i] = Math.max(1, Math.min(MAX_LEVEL, level[i]));
        dirty = true;
    }

    private static void copy(int[] src, int[] dst) {
        System.arraycopy(src, 0, dst, 0, Math.min(src.length, dst.length));
    }
}
