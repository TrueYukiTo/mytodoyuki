package dev.yukito.yukicore.magic;

/** Двенадцать заклинаний: шесть ледяных (Юки) и шесть огненных (Синтара). Шестое в каждой ветке — ультимативное. */
public enum Spell {
    FROST_BOLT(School.ICE, 1, 10, 12),
    FROST_NOVA(School.ICE, 3, 28, 160),
    GLACIAL_ARMOR(School.ICE, 6, 40, 600),
    FROST_BLINK(School.ICE, 10, 25, 120),
    BLIZZARD(School.ICE, 15, 60, 500),
    ABSOLUTE_ZERO(School.ICE, 20, 100, 1800),
    EMBER_BOLT(School.FIRE, 1, 10, 12),
    FLAME_RING(School.FIRE, 3, 28, 160),
    PHOENIX_WINGS(School.FIRE, 6, 40, 600),
    FLAME_DASH(School.FIRE, 10, 25, 120),
    METEOR(School.FIRE, 15, 60, 500),
    SUPERNOVA(School.FIRE, 20, 100, 1800);

    public static final int MAX_RANK = 5;

    public final School school;
    public final int requiredLevel;
    public final int baseCost;
    public final int baseCooldown;

    Spell(School school, int requiredLevel, int baseCost, int baseCooldown) {
        this.school = school;
        this.requiredLevel = requiredLevel;
        this.baseCost = baseCost;
        this.baseCooldown = baseCooldown;
    }

    public boolean ultimate() {
        return ordinal() % 6 == 5;
    }

    /** Стоимость открытия/улучшения в очках навыков. */
    public int upgradeCost(int currentRank) {
        return ultimate() && currentRank == 0 ? 3 : 1;
    }

    public int cost(int rank) {
        return Math.round(baseCost * (1f - 0.05f * (rank - 1)));
    }

    public int cooldown(int rank) {
        return Math.round(baseCooldown * (1f - 0.08f * (rank - 1)));
    }

    public String id() {
        return name().toLowerCase(java.util.Locale.ROOT);
    }

    public static Spell byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : null;
    }
}
