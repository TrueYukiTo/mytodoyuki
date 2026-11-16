package dev.yukito.yukicore.magic;

import java.util.List;
import java.util.Locale;

import static dev.yukito.yukicore.magic.Skill.Kind.*;
import static dev.yukito.yukicore.magic.School.FIRE;
import static dev.yukito.yukicore.magic.School.ICE;

/**
 * Дерево навыков магии: по 22 навыка у Юки (лёд) и Синтары (пламя).
 * Колонка — ступень дерева (0..8), ряд — 0..2. Навык открывается, только когда изучены все его предки.
 */
public enum Skill {
    // ============================================================ ❄ ЮКИ
    FROST_BOLT(ICE, ACTIVE, 0, 1, 8, 8),
    ICE_SHARDS(ICE, ACTIVE, 1, 0, 18, 30, "FROST_BOLT"),
    FROST_NOVA(ICE, ACTIVE, 1, 1, 30, 120, "FROST_BOLT"),
    COLD_MIND(ICE, PASSIVE, 1, 2, 0, 0, "FROST_BOLT"),
    ICE_SPIKES(ICE, ACTIVE, 2, 0, 35, 140, "ICE_SHARDS"),
    GLACIAL_ARMOR(ICE, ACTIVE, 2, 1, 40, 600, "FROST_NOVA"),
    FROSTBITE(ICE, PASSIVE, 2, 2, 0, 0, "COLD_MIND"),
    ICE_WALL(ICE, ACTIVE, 3, 0, 40, 300, "ICE_SPIKES"),
    FROST_BLINK(ICE, ACTIVE, 3, 1, 20, 80, "GLACIAL_ARMOR"),
    PERMAFROST(ICE, PASSIVE, 3, 2, 0, 0, "FROSTBITE"),
    GLACIER_CRASH(ICE, ACTIVE, 4, 0, 70, 400, "ICE_WALL"),
    BLIZZARD(ICE, ACTIVE, 4, 1, 60, 500, "FROST_BLINK", "ICE_WALL"),
    WINTER_HEART(ICE, PASSIVE, 4, 2, 0, 0, "PERMAFROST"),
    ICE_PRISON(ICE, ACTIVE, 5, 0, 60, 360, "GLACIER_CRASH"),
    FROZEN_ORB(ICE, ACTIVE, 5, 1, 70, 400, "BLIZZARD"),
    SHATTER(ICE, PASSIVE, 5, 2, 0, 0, "WINTER_HEART"),
    ICE_GOLEM(ICE, ACTIVE, 6, 0, 90, 1200, "ICE_PRISON"),
    AVALANCHE(ICE, ACTIVE, 6, 1, 80, 500, "FROZEN_ORB"),
    ETERNAL_COLD(ICE, PASSIVE, 6, 2, 0, 0, "SHATTER"),
    POLAR_STORM(ICE, ACTIVE, 7, 1, 120, 1200, "AVALANCHE"),
    ABSOLUTE_ZERO(ICE, ULTIMATE, 8, 0, 200, 2400, "POLAR_STORM", "ICE_GOLEM"),
    YUKI_AVATAR(ICE, ULTIMATE, 8, 2, 250, 6000, "ABSOLUTE_ZERO", "ETERNAL_COLD"),

    // ============================================================ ✹ СИНТАРА
    EMBER_BOLT(FIRE, ACTIVE, 0, 1, 8, 8),
    FIRE_SPRAY(FIRE, ACTIVE, 1, 0, 25, 100, "EMBER_BOLT"),
    FLAME_RING(FIRE, ACTIVE, 1, 1, 30, 120, "EMBER_BOLT"),
    BURNING_WILL(FIRE, PASSIVE, 1, 2, 0, 0, "EMBER_BOLT"),
    FIRE_PILLARS(FIRE, ACTIVE, 2, 0, 45, 200, "FIRE_SPRAY"),
    PHOENIX_WINGS(FIRE, ACTIVE, 2, 1, 50, 900, "FLAME_RING"),
    IGNITE(FIRE, PASSIVE, 2, 2, 0, 0, "BURNING_WILL"),
    LAVA_POOL(FIRE, ACTIVE, 3, 0, 50, 300, "FIRE_PILLARS"),
    FLAME_DASH(FIRE, ACTIVE, 3, 1, 25, 80, "PHOENIX_WINGS"),
    SINTARA_HEAT(FIRE, PASSIVE, 3, 2, 0, 0, "IGNITE"),
    FIREBALL_BARRAGE(FIRE, ACTIVE, 4, 0, 70, 360, "LAVA_POOL"),
    METEOR(FIRE, ACTIVE, 4, 1, 70, 400, "FLAME_DASH", "LAVA_POOL"),
    INNER_FIRE(FIRE, PASSIVE, 4, 2, 0, 0, "SINTARA_HEAT"),
    DRAGON_BREATH(FIRE, ACTIVE, 5, 0, 80, 500, "FIREBALL_BARRAGE"),
    FLAME_TORNADO(FIRE, ACTIVE, 5, 1, 90, 600, "METEOR"),
    COMBUSTION(FIRE, PASSIVE, 5, 2, 0, 0, "INNER_FIRE"),
    FIRE_SPIRITS(FIRE, ACTIVE, 6, 0, 90, 1200, "DRAGON_BREATH"),
    METEOR_SHOWER(FIRE, ACTIVE, 6, 1, 140, 1400, "FLAME_TORNADO"),
    PHOENIX_SOUL(FIRE, PASSIVE, 6, 2, 0, 0, "COMBUSTION"),
    SUN_STRIKE(FIRE, ACTIVE, 7, 1, 130, 1200, "METEOR_SHOWER"),
    SUPERNOVA(FIRE, ULTIMATE, 8, 0, 220, 2400, "SUN_STRIKE", "FIRE_SPIRITS"),
    SINTARA_AVATAR(FIRE, ULTIMATE, 8, 2, 250, 6000, "SUPERNOVA", "PHOENIX_SOUL");

    public enum Kind { ACTIVE, PASSIVE, ULTIMATE }

    public static final int MAX_RANK = 5;
    public static final int COLUMNS = 9;
    /** Требуемый уровень школы для 1-го ранга по ступеням дерева. */
    private static final int[] LEVEL = {1, 3, 7, 12, 18, 24, 30, 36, 42};

    public final School school;
    public final Kind kind;
    public final int column, row;
    public final int baseCost, baseCooldown;
    private final String[] parentNames;
    private List<Skill> parents;

    Skill(School school, Kind kind, int column, int row, int cost, int cooldown, String... parents) {
        this.school = school;
        this.kind = kind;
        this.column = column;
        this.row = row;
        this.baseCost = cost;
        this.baseCooldown = cooldown;
        this.parentNames = parents;
    }

    public List<Skill> parents() {
        if (parents == null) parents = java.util.Arrays.stream(parentNames).map(Skill::valueOf).toList();
        return parents;
    }

    public boolean passive() {
        return kind == PASSIVE;
    }

    public boolean ultimate() {
        return kind == ULTIMATE;
    }

    /** Аватар — второй ультимейт, ему нужен 50-й уровень. */
    public int requiredLevel(int rank) {
        int base = this == YUKI_AVATAR || this == SINTARA_AVATAR ? 50 : LEVEL[column];
        return base + rank * 3;
    }

    public int upgradeCost(int currentRank) {
        return ultimate() && currentRank == 0 ? 3 : 1;
    }

    public int cost(int rank) {
        return Math.round(baseCost * (1f - 0.05f * (Math.max(1, rank) - 1)));
    }

    public int cooldown(int rank) {
        return Math.round(baseCooldown * (1f - 0.08f * (Math.max(1, rank) - 1)));
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public static Skill byId(int id) {
        return id >= 0 && id < values().length ? values()[id] : null;
    }

    public static Skill byName(String id) {
        try {
            return valueOf(id.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return null;
        }
    }
}
