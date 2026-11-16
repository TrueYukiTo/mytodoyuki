package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.item.CoreItem;
import dev.yukito.yukicore.item.FrostSwordItem;
import dev.yukito.yukicore.item.InfinityStarItem;
import dev.yukito.yukicore.item.SigilItem;
import dev.yukito.yukicore.item.YukiArmorItem;
import dev.yukito.yukicore.item.YukiStarItem;
import dev.yukito.yukicore.magic.School;
import dev.yukito.yukicore.magic.item.FocusItem;
import dev.yukito.yukicore.magic.item.MagicConsumableItem;
import dev.yukito.yukicore.magic.item.MagicGearItem;
import dev.yukito.yukicore.magic.item.RobeItem;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.Unbreakable;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(YukiCore.MOD_ID);

    // --- Сырьё из Энда ---
    public static final DeferredItem<Item> RAW_FROST = ITEMS.registerSimpleItem("raw_frost");
    public static final DeferredItem<Item> FROST_INGOT = ITEMS.registerSimpleItem("frost_ingot");
    public static final DeferredItem<Item> YUKI_CRYSTAL = ITEMS.registerSimpleItem("yuki_crystal");
    public static final DeferredItem<Item> STARLIGHT_SHARD = material("starlight_shard", Rarity.RARE, true);

    // --- Промежуточные компоненты (собираются машинами других модов) ---
    public static final DeferredItem<Item> FROST_PLATE = material("frost_plate", Rarity.COMMON, false);
    public static final DeferredItem<Item> YUKI_ESSENCE = material("yuki_essence", Rarity.UNCOMMON, false);
    public static final DeferredItem<Item> CRYO_CIRCUIT = material("cryo_circuit", Rarity.UNCOMMON, false);
    public static final DeferredItem<Item> INCOMPLETE_FROST_MECHANISM = ITEMS.register("incomplete_frost_mechanism",
            () -> new Item(new Item.Properties().stacksTo(1)));
    public static final DeferredItem<Item> FROST_MECHANISM = material("frost_mechanism", Rarity.UNCOMMON, false);
    public static final DeferredItem<Item> AURORIUM_INGOT = material("aurorium_ingot", Rarity.RARE, false);
    public static final DeferredItem<Item> CELESTIUM_INGOT = material("celestium_ingot", Rarity.EPIC, false);
    public static final DeferredItem<Item> STAR_SHARD = material("star_shard", Rarity.EPIC, true);

    // --- Шаблоны кузнечного улучшения ---
    public static final DeferredItem<Item> FROST_UPGRADE_TEMPLATE = material("frost_upgrade_template", Rarity.UNCOMMON, false);
    public static final DeferredItem<Item> AURORIUM_UPGRADE_TEMPLATE = material("aurorium_upgrade_template", Rarity.RARE, false);
    public static final DeferredItem<Item> CELESTIUM_UPGRADE_TEMPLATE = material("celestium_upgrade_template", Rarity.EPIC, false);

    // --- Особые предметы ---
    public static final DeferredItem<Item> YUKI_STAR = ITEMS.register("yuki_star",
            () -> new YukiStarItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> FROST_APPLE = ITEMS.register("frost_apple",
            () -> new Item(new Item.Properties().rarity(Rarity.RARE).food(new FoodProperties.Builder()
                    .nutrition(8).saturationModifier(1.4f).alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60, 1), 1.0f)
                    .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 120, 0), 1.0f)
                    .effect(new MobEffectInstance(MobEffects.ABSORPTION, 20 * 120, 2), 1.0f)
                    .build())));

    // --- Эндгейм ---
    public static final DeferredItem<Item> FROST_CORE = core("frost_core", Rarity.UNCOMMON, false);
    public static final DeferredItem<Item> TECH_CORE = core("tech_core", Rarity.RARE, false);
    public static final DeferredItem<Item> MAGIC_CORE = core("magic_core", Rarity.RARE, false);
    public static final DeferredItem<Item> NATURE_CORE = core("nature_core", Rarity.RARE, false);
    public static final DeferredItem<Item> DIMENSION_CORE = core("dimension_core", Rarity.RARE, false);
    public static final DeferredItem<Item> YUKI_HEART = core("yuki_heart", Rarity.EPIC, true);
    public static final DeferredItem<Item> UNSTABLE_INFINITY_STAR = core("unstable_infinity_star", Rarity.EPIC, true);
    public static final DeferredItem<Item> YUKI_INFINITY_STAR = ITEMS.register("yuki_infinity_star",
            () -> new InfinityStarItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant()));
    public static final DeferredItem<Item> YUKI_BLADE = ITEMS.register("yuki_blade",
            () -> new FrostSwordItem(ModTiers.YUKI, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                    .attributes(SwordItem.createAttributes(ModTiers.YUKI, 30, -2.0f))));

    // --- Алтарь ---
    public static final DeferredItem<Item> FROST_WAND = ITEMS.register("frost_wand",
            () -> new CoreItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), true));

    // --- Материалы планет Системы Юки ---
    public static final DeferredItem<Item> PERMAFROST_CRYSTAL = material("permafrost_crystal", Rarity.RARE, false);
    public static final DeferredItem<Item> AURORA_SHARD = material("aurora_shard", Rarity.RARE, true);
    public static final DeferredItem<Item> RAW_PYROLITE = material("raw_pyrolite", Rarity.RARE, false);
    public static final DeferredItem<Item> PYROLITE_INGOT = material("pyrolite_ingot", Rarity.RARE, false);
    public static final DeferredItem<Item> VOID_SHARD = material("void_shard", Rarity.EPIC, false);
    public static final DeferredItem<Item> STELLAR_SHARD = material("stellar_shard", Rarity.EPIC, true);

    // --- Трофеи боссов ---
    public static final DeferredItem<Item> COLOSSUS_HEART = core("colossus_heart", Rarity.EPIC, true);
    public static final DeferredItem<Item> BLIZZARD_CROWN = core("blizzard_crown", Rarity.EPIC, true);
    public static final DeferredItem<Item> ASH_CORE = core("ash_core", Rarity.EPIC, true);
    public static final DeferredItem<Item> VOID_SCALE = core("void_scale", Rarity.EPIC, true);
    public static final DeferredItem<Item> YUKI_TEAR = core("yuki_tear", Rarity.EPIC, true);

    // --- Печати призыва ---
    public static final DeferredItem<Item> SIGIL_COLOSSUS = sigil("sigil_colossus", ModEntities.FROST_COLOSSUS, "yukia");
    public static final DeferredItem<Item> SIGIL_QUEEN = sigil("sigil_queen", ModEntities.BLIZZARD_QUEEN, "aurora");
    public static final DeferredItem<Item> SIGIL_ASH_LORD = sigil("sigil_ash_lord", ModEntities.ASH_LORD, "pyrolith");
    public static final DeferredItem<Item> SIGIL_WYRM = sigil("sigil_wyrm", ModEntities.VOID_WYRM, "nyx");
    public static final DeferredItem<Item> SIGIL_SHADOW = sigil("sigil_shadow", ModEntities.SHADOW_OF_YUKI, "celestia");

    // --- Награды финала ---
    public static final DeferredItem<Item> CROWN_OF_YUKI = ITEMS.register("crown_of_yuki",
            () -> new dev.yukito.yukicore.item.YukiArmorItem(ModArmorMaterials.YUKI, ArmorItem.Type.HELMET,
                    new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                            .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                            .durability(ArmorItem.Type.HELMET.getDurability(99)), "yuki"));
    public static final DeferredItem<Item> ETERNAL_WINTER_BLADE = ITEMS.register("eternal_winter_blade",
            () -> new FrostSwordItem(ModTiers.YUKI, new Item.Properties().rarity(Rarity.EPIC).fireResistant()
                    .component(DataComponents.UNBREAKABLE, new Unbreakable(true))
                    .attributes(SwordItem.createAttributes(ModTiers.YUKI, 50, -1.8f))));

    // --- Полезные вещи ---
    public static final DeferredItem<Item> FROST_MAGNET = ITEMS.register("frost_magnet",
            () -> new dev.yukito.yukicore.item.FrostMagnetItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), 8));
    public static final DeferredItem<Item> CELESTIAL_MAGNET = ITEMS.register("celestial_magnet",
            () -> new dev.yukito.yukicore.item.FrostMagnetItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
                    .fireResistant(), 16));
    public static final DeferredItem<Item> AURORIUM_HAMMER = ITEMS.register("aurorium_hammer",
            () -> new dev.yukito.yukicore.item.HammerItem(ModTiers.AURORIUM, toolProps(Rarity.RARE)
                    .attributes(DiggerItem.createAttributes(ModTiers.AURORIUM, 6.0f, -3.2f))));
    public static final DeferredItem<Item> CELESTIUM_HAMMER = ITEMS.register("celestium_hammer",
            () -> new dev.yukito.yukicore.item.HammerItem(ModTiers.CELESTIUM, toolProps(Rarity.EPIC)
                    .attributes(DiggerItem.createAttributes(ModTiers.CELESTIUM, 7.0f, -3.0f))));
    public static final DeferredItem<Item> SNOW_BOOTS = ITEMS.register("snow_boots",
            () -> new dev.yukito.yukicore.item.SnowBootsItem(ModArmorMaterials.FROST,
                    toolProps(Rarity.RARE).durability(ArmorItem.Type.BOOTS.getDurability(40))));

    // --- 5.0: сплавы Морозной кузни ---
    public static final DeferredItem<Item> CRYOSTEEL_INGOT = material("cryosteel_ingot", Rarity.UNCOMMON, false);
    public static final DeferredItem<Item> STELLAR_ALLOY = material("stellar_alloy", Rarity.EPIC, true);

    // --- Для Sintara ---
    public static final DeferredItem<Item> SINTARA_STAFF = ITEMS.register("sintara_staff", () -> new FocusItem(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), School.FIRE, 0.25f, true));
    public static final DeferredItem<Item> SINTARA_AMULET = ITEMS.register("sintara_amulet",
            () -> new dev.yukito.yukicore.item.SintaraAmuletItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    // --- Новый год ---
    public static final DeferredItem<Item> GIFT = ITEMS.register("gift",
            () -> new dev.yukito.yukicore.item.GiftItem(new Item.Properties().rarity(Rarity.UNCOMMON), "gift"));
    public static final DeferredItem<Item> GRAND_GIFT = ITEMS.register("grand_gift",
            () -> new dev.yukito.yukicore.item.GiftItem(new Item.Properties().rarity(Rarity.EPIC), "grand_gift"));
    public static final DeferredItem<Item> ADVENT_CALENDAR = ITEMS.register("advent_calendar",
            () -> new dev.yukito.yukicore.item.AdventCalendarItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE)));
    public static final DeferredItem<Item> TANGERINE = food("tangerine", 4, 0.6f, Rarity.COMMON,
            new MobEffectInstance(MobEffects.REGENERATION, 20 * 5, 0));
    public static final DeferredItem<Item> CANDY_CANE = food("candy_cane", 3, 0.4f, Rarity.UNCOMMON,
            new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 60, 1), new MobEffectInstance(MobEffects.DIG_SPEED, 20 * 60, 1));
    public static final DeferredItem<Item> GINGERBREAD = food("gingerbread", 7, 1.0f, Rarity.UNCOMMON,
            new MobEffectInstance(MobEffects.ABSORPTION, 20 * 90, 1));
    public static final DeferredItem<Item> OLIVIER = ITEMS.register("olivier", () -> new Item(new Item.Properties()
            .stacksTo(16).rarity(Rarity.UNCOMMON).food(new FoodProperties.Builder().nutrition(12).saturationModifier(1.2f)
                    .usingConvertsTo(Items.BOWL)
                    .effect(new MobEffectInstance(MobEffects.HEALTH_BOOST, 20 * 300, 1), 1f)
                    .effect(new MobEffectInstance(MobEffects.DAMAGE_RESISTANCE, 20 * 120, 0), 1f).build())));
    public static final DeferredItem<Item> HOT_COCOA = ITEMS.register("hot_cocoa", () -> new dev.yukito.yukicore.item.DrinkItem(
            new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON).food(new FoodProperties.Builder().nutrition(5)
                    .saturationModifier(0.8f).alwaysEdible().usingConvertsTo(Items.GLASS_BOTTLE)
                    .effect(new MobEffectInstance(MobEffects.REGENERATION, 20 * 20, 1), 1f)
                    .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0), 1f).build())));

    // --- 6.0: магия Юки (лёд) и Синтары (пламя) ---
    public static final DeferredItem<Item> YUKI_GRIMOIRE = ITEMS.register("yuki_grimoire", () -> new FocusItem(
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), School.ICE, 0f, false));
    public static final DeferredItem<Item> SINTARA_TOME = ITEMS.register("sintara_tome", () -> new FocusItem(
            new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON), School.FIRE, 0f, false));
    public static final DeferredItem<Item> YUKI_STAFF = ITEMS.register("yuki_staff", () -> new FocusItem(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), School.ICE, 0.25f, true));
    public static final DeferredItem<Item> STAFF_OF_ELEMENTS = ITEMS.register("staff_of_elements", () -> new FocusItem(
            new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), null, 0.4f, true));
    public static final DeferredItem<Item> MANA_POTION = consumable("mana_potion", MagicConsumableItem.Kind.MANA_POTION, Rarity.UNCOMMON, 16);
    public static final DeferredItem<Item> GREATER_MANA_POTION = consumable("greater_mana_potion", MagicConsumableItem.Kind.GREATER_MANA_POTION, Rarity.RARE, 16);
    public static final DeferredItem<Item> FROST_ESSENCE = consumable("frost_essence", MagicConsumableItem.Kind.FROST_ESSENCE, Rarity.UNCOMMON, 64);
    public static final DeferredItem<Item> FLAME_ESSENCE = consumable("flame_essence", MagicConsumableItem.Kind.FLAME_ESSENCE, Rarity.UNCOMMON, 64);
    public static final DeferredItem<Item> MANA_CRYSTAL = consumable("mana_crystal", MagicConsumableItem.Kind.MANA_CRYSTAL, Rarity.EPIC, 16);
    public static final DeferredItem<Item> RESPEC_SCROLL = consumable("respec_scroll", MagicConsumableItem.Kind.RESPEC, Rarity.RARE, 16);
    public static final DeferredItem<Item> FROST_RING = ring("frost_ring", School.ICE, 0.1f, Rarity.RARE);
    public static final DeferredItem<Item> FLAME_RING = ring("flame_ring", School.FIRE, 0.1f, Rarity.RARE);
    public static final DeferredItem<Item> RING_OF_ETERNAL_WINTER = ring("ring_of_eternal_winter", School.ICE, 0.2f, Rarity.EPIC);
    public static final DeferredItem<Item> RING_OF_SINTARA = ring("ring_of_sintara", School.FIRE, 0.2f, Rarity.EPIC);
    public static final DeferredItem<Item> CRYOMANCER_HOOD = robe("cryomancer_hood", ModArmorMaterials.CRYOMANCER, ArmorItem.Type.HELMET, School.ICE, 0.06f, 0.05f, 15, YukiArmorItem.CRYOMANCER);
    public static final DeferredItem<Item> CRYOMANCER_ROBE = robe("cryomancer_robe", ModArmorMaterials.CRYOMANCER, ArmorItem.Type.CHESTPLATE, School.ICE, 0.06f, 0.05f, 15, YukiArmorItem.CRYOMANCER);
    public static final DeferredItem<Item> CRYOMANCER_LEGGINGS = robe("cryomancer_leggings", ModArmorMaterials.CRYOMANCER, ArmorItem.Type.LEGGINGS, School.ICE, 0.06f, 0.05f, 15, YukiArmorItem.CRYOMANCER);
    public static final DeferredItem<Item> CRYOMANCER_BOOTS = robe("cryomancer_boots", ModArmorMaterials.CRYOMANCER, ArmorItem.Type.BOOTS, School.ICE, 0.06f, 0.05f, 15, YukiArmorItem.CRYOMANCER);
    public static final DeferredItem<Item> PYROMANCER_HOOD = robe("pyromancer_hood", ModArmorMaterials.PYROMANCER, ArmorItem.Type.HELMET, School.FIRE, 0.06f, 0.05f, 15, YukiArmorItem.PYROMANCER);
    public static final DeferredItem<Item> PYROMANCER_ROBE = robe("pyromancer_robe", ModArmorMaterials.PYROMANCER, ArmorItem.Type.CHESTPLATE, School.FIRE, 0.06f, 0.05f, 15, YukiArmorItem.PYROMANCER);
    public static final DeferredItem<Item> PYROMANCER_LEGGINGS = robe("pyromancer_leggings", ModArmorMaterials.PYROMANCER, ArmorItem.Type.LEGGINGS, School.FIRE, 0.06f, 0.05f, 15, YukiArmorItem.PYROMANCER);
    public static final DeferredItem<Item> PYROMANCER_BOOTS = robe("pyromancer_boots", ModArmorMaterials.PYROMANCER, ArmorItem.Type.BOOTS, School.FIRE, 0.06f, 0.05f, 15, YukiArmorItem.PYROMANCER);
    public static final DeferredItem<Item> ARCHON_HEART = core("archon_heart", Rarity.EPIC, true);
    public static final DeferredItem<Item> PHOENIX_FEATHER = core("phoenix_feather", Rarity.EPIC, true);
    public static final DeferredItem<Item> SIGIL_ARCHON = sigil("sigil_archon", ModEntities.GLACIAL_ARCHON, null);
    public static final DeferredItem<Item> SIGIL_PHOENIX = sigil("sigil_phoenix", ModEntities.INFERNAL_PHOENIX, null);

    // --- 7.0: регалии магов, Вечная зима, знания ---
    public static final DeferredItem<Item> YUKI_REGALIA_HOOD = robe("yuki_regalia_hood", ModArmorMaterials.YUKI_REGALIA, ArmorItem.Type.HELMET, School.ICE, 0.15f, 0.08f, 60, YukiArmorItem.YUKI_REGALIA);
    public static final DeferredItem<Item> YUKI_REGALIA_ROBE = robe("yuki_regalia_robe", ModArmorMaterials.YUKI_REGALIA, ArmorItem.Type.CHESTPLATE, School.ICE, 0.15f, 0.08f, 60, YukiArmorItem.YUKI_REGALIA);
    public static final DeferredItem<Item> YUKI_REGALIA_LEGGINGS = robe("yuki_regalia_leggings", ModArmorMaterials.YUKI_REGALIA, ArmorItem.Type.LEGGINGS, School.ICE, 0.15f, 0.08f, 60, YukiArmorItem.YUKI_REGALIA);
    public static final DeferredItem<Item> YUKI_REGALIA_BOOTS = robe("yuki_regalia_boots", ModArmorMaterials.YUKI_REGALIA, ArmorItem.Type.BOOTS, School.ICE, 0.15f, 0.08f, 60, YukiArmorItem.YUKI_REGALIA);
    public static final DeferredItem<Item> SINTARA_REGALIA_HOOD = robe("sintara_regalia_hood", ModArmorMaterials.SINTARA_REGALIA, ArmorItem.Type.HELMET, School.FIRE, 0.15f, 0.08f, 60, YukiArmorItem.SINTARA_REGALIA);
    public static final DeferredItem<Item> SINTARA_REGALIA_ROBE = robe("sintara_regalia_robe", ModArmorMaterials.SINTARA_REGALIA, ArmorItem.Type.CHESTPLATE, School.FIRE, 0.15f, 0.08f, 60, YukiArmorItem.SINTARA_REGALIA);
    public static final DeferredItem<Item> SINTARA_REGALIA_LEGGINGS = robe("sintara_regalia_leggings", ModArmorMaterials.SINTARA_REGALIA, ArmorItem.Type.LEGGINGS, School.FIRE, 0.15f, 0.08f, 60, YukiArmorItem.SINTARA_REGALIA);
    public static final DeferredItem<Item> SINTARA_REGALIA_BOOTS = robe("sintara_regalia_boots", ModArmorMaterials.SINTARA_REGALIA, ArmorItem.Type.BOOTS, School.FIRE, 0.15f, 0.08f, 60, YukiArmorItem.SINTARA_REGALIA);
    public static final DeferredItem<Item> ETERNAL_HELMET = ITEMS.register("eternal_helmet", () -> new YukiArmorItem(ModArmorMaterials.ETERNAL, ArmorItem.Type.HELMET,
            toolProps(Rarity.EPIC).component(DataComponents.UNBREAKABLE, new Unbreakable(true)).durability(ArmorItem.Type.HELMET.getDurability(120)), YukiArmorItem.ETERNAL));
    public static final DeferredItem<Item> ETERNAL_CHESTPLATE = ITEMS.register("eternal_chestplate", () -> new YukiArmorItem(ModArmorMaterials.ETERNAL, ArmorItem.Type.CHESTPLATE,
            toolProps(Rarity.EPIC).component(DataComponents.UNBREAKABLE, new Unbreakable(true)).durability(ArmorItem.Type.CHESTPLATE.getDurability(120)), YukiArmorItem.ETERNAL));
    public static final DeferredItem<Item> ETERNAL_LEGGINGS = ITEMS.register("eternal_leggings", () -> new YukiArmorItem(ModArmorMaterials.ETERNAL, ArmorItem.Type.LEGGINGS,
            toolProps(Rarity.EPIC).component(DataComponents.UNBREAKABLE, new Unbreakable(true)).durability(ArmorItem.Type.LEGGINGS.getDurability(120)), YukiArmorItem.ETERNAL));
    public static final DeferredItem<Item> ETERNAL_BOOTS = ITEMS.register("eternal_boots", () -> new YukiArmorItem(ModArmorMaterials.ETERNAL, ArmorItem.Type.BOOTS,
            toolProps(Rarity.EPIC).component(DataComponents.UNBREAKABLE, new Unbreakable(true)).durability(ArmorItem.Type.BOOTS.getDurability(120)), YukiArmorItem.ETERNAL));
    public static final DeferredItem<Item> GREATER_FROST_ESSENCE = consumable("greater_frost_essence", MagicConsumableItem.Kind.GREATER_FROST_ESSENCE, Rarity.RARE, 64);
    public static final DeferredItem<Item> GREATER_FLAME_ESSENCE = consumable("greater_flame_essence", MagicConsumableItem.Kind.GREATER_FLAME_ESSENCE, Rarity.RARE, 64);
    public static final DeferredItem<Item> TOME_OF_WINTER = consumable("tome_of_winter", MagicConsumableItem.Kind.KNOWLEDGE_ICE, Rarity.EPIC, 16);
    public static final DeferredItem<Item> TOME_OF_FLAME = consumable("tome_of_flame", MagicConsumableItem.Kind.KNOWLEDGE_FIRE, Rarity.EPIC, 16);

    // --- 7.2: Казино Синтары (только игровые фишки) ---
    public static final DeferredItem<Item> SINTARA_CHIP = ITEMS.register("sintara_chip",
            () -> new CoreItem(new Item.Properties().rarity(Rarity.UNCOMMON), false));
    public static final DeferredItem<Item> LUCKY_DICE = ITEMS.register("lucky_dice",
            () -> new dev.yukito.yukicore.casino.CasinoItem(new Item.Properties().stacksTo(1).rarity(Rarity.RARE), true, false));
    public static final DeferredItem<Item> FORTUNE_BOX = ITEMS.register("fortune_box",
            () -> new dev.yukito.yukicore.casino.CasinoItem(new Item.Properties().stacksTo(16).rarity(Rarity.EPIC), false, true));
    public static final DeferredItem<Item> LUCKY_SEVEN = ITEMS.register("lucky_seven",
            () -> new MagicGearItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC).fireResistant(), School.FIRE, 0.15f));

    // --- снаряды (только внешний вид, в творческой вкладке скрыты) ---
    public static final DeferredItem<Item> ORB_FROST = ITEMS.registerSimpleItem("orb_frost");
    public static final DeferredItem<Item> ORB_FLAME = ITEMS.registerSimpleItem("orb_flame");
    public static final DeferredItem<Item> ORB_VOID = ITEMS.registerSimpleItem("orb_void");
    public static final DeferredItem<Item> ORB_STAR = ITEMS.registerSimpleItem("orb_star");
    public static final DeferredItem<Item> ORB_BOULDER = ITEMS.registerSimpleItem("orb_boulder");
    public static final DeferredItem<Item> ORB_METEOR = ITEMS.registerSimpleItem("orb_meteor");
    public static final DeferredItem<Item> ORB_ICICLE = ITEMS.registerSimpleItem("orb_icicle");
    public static final DeferredItem<Item> ORB_FEATHER = ITEMS.registerSimpleItem("orb_feather");
    public static final DeferredItem<Item> ORB_BLADE = ITEMS.registerSimpleItem("orb_blade");

    // --- Инструменты и броня трёх уровней ---
    public static final DeferredItem<Item> FROST_SWORD = sword("frost", ModTiers.FROST, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_PICKAXE = pickaxe("frost", ModTiers.FROST, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_AXE = axe("frost", ModTiers.FROST, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_SHOVEL = shovel("frost", ModTiers.FROST, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_HOE = hoe("frost", ModTiers.FROST, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_HELMET = armor("frost", ModArmorMaterials.FROST, ArmorItem.Type.HELMET, 40, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_CHESTPLATE = armor("frost", ModArmorMaterials.FROST, ArmorItem.Type.CHESTPLATE, 40, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_LEGGINGS = armor("frost", ModArmorMaterials.FROST, ArmorItem.Type.LEGGINGS, 40, Rarity.UNCOMMON);
    public static final DeferredItem<Item> FROST_BOOTS = armor("frost", ModArmorMaterials.FROST, ArmorItem.Type.BOOTS, 40, Rarity.UNCOMMON);

    public static final DeferredItem<Item> AURORIUM_SWORD = sword("aurorium", ModTiers.AURORIUM, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_PICKAXE = pickaxe("aurorium", ModTiers.AURORIUM, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_AXE = axe("aurorium", ModTiers.AURORIUM, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_SHOVEL = shovel("aurorium", ModTiers.AURORIUM, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_HOE = hoe("aurorium", ModTiers.AURORIUM, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_HELMET = armor("aurorium", ModArmorMaterials.AURORIUM, ArmorItem.Type.HELMET, 55, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_CHESTPLATE = armor("aurorium", ModArmorMaterials.AURORIUM, ArmorItem.Type.CHESTPLATE, 55, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_LEGGINGS = armor("aurorium", ModArmorMaterials.AURORIUM, ArmorItem.Type.LEGGINGS, 55, Rarity.RARE);
    public static final DeferredItem<Item> AURORIUM_BOOTS = armor("aurorium", ModArmorMaterials.AURORIUM, ArmorItem.Type.BOOTS, 55, Rarity.RARE);

    public static final DeferredItem<Item> CELESTIUM_SWORD = sword("celestium", ModTiers.CELESTIUM, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_PICKAXE = pickaxe("celestium", ModTiers.CELESTIUM, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_AXE = axe("celestium", ModTiers.CELESTIUM, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_SHOVEL = shovel("celestium", ModTiers.CELESTIUM, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_HOE = hoe("celestium", ModTiers.CELESTIUM, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_HELMET = armor("celestium", ModArmorMaterials.CELESTIUM, ArmorItem.Type.HELMET, 80, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_CHESTPLATE = armor("celestium", ModArmorMaterials.CELESTIUM, ArmorItem.Type.CHESTPLATE, 80, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_LEGGINGS = armor("celestium", ModArmorMaterials.CELESTIUM, ArmorItem.Type.LEGGINGS, 80, Rarity.EPIC);
    public static final DeferredItem<Item> CELESTIUM_BOOTS = armor("celestium", ModArmorMaterials.CELESTIUM, ArmorItem.Type.BOOTS, 80, Rarity.EPIC);

    // --- Блоки ---
    static {
        blockItem(ModBlocks.FROST_ORE, Rarity.COMMON);
        blockItem(ModBlocks.YUKI_CRYSTAL_ORE, Rarity.COMMON);
        blockItem(ModBlocks.STARLIGHT_ORE, Rarity.RARE);
        blockItem(ModBlocks.FROST_BLOCK, Rarity.COMMON);
        blockItem(ModBlocks.YUKI_CRYSTAL_BLOCK, Rarity.COMMON);
        blockItem(ModBlocks.STARLIGHT_BLOCK, Rarity.RARE);
        blockItem(ModBlocks.AURORIUM_BLOCK, Rarity.RARE);
        blockItem(ModBlocks.CELESTIUM_BLOCK, Rarity.EPIC);
        blockItem(ModBlocks.YUKI_LAMP, Rarity.COMMON);
        blockItem(ModBlocks.YUKI_ALTAR, Rarity.EPIC);
        blockItem(ModBlocks.YUKI_PEDESTAL, Rarity.RARE);
        blockItem(ModBlocks.PERMAFROST_ORE, Rarity.RARE);
        blockItem(ModBlocks.AURORA_ORE, Rarity.RARE);
        blockItem(ModBlocks.PYROLITE_ORE, Rarity.RARE);
        blockItem(ModBlocks.VOID_ORE, Rarity.EPIC);
        blockItem(ModBlocks.STELLAR_ORE, Rarity.EPIC);
        blockItem(ModBlocks.PERMAFROST_BLOCK, Rarity.RARE);
        blockItem(ModBlocks.AURORA_BLOCK, Rarity.RARE);
        blockItem(ModBlocks.PYROLITE_BLOCK, Rarity.RARE);
        blockItem(ModBlocks.VOID_BLOCK, Rarity.EPIC);
        blockItem(ModBlocks.STELLAR_BLOCK, Rarity.EPIC);
        blockItem(ModBlocks.YUKI_CHRONO, Rarity.EPIC);
        blockItem(ModBlocks.FROST_CASING, Rarity.UNCOMMON);
        blockItem(ModBlocks.REACTOR_CASING, Rarity.RARE);
        blockItem(ModBlocks.CRYOSTEEL_BLOCK, Rarity.UNCOMMON);
        blockItem(ModBlocks.FROST_FORGE, Rarity.RARE);
        blockItem(ModBlocks.STAR_REACTOR, Rarity.EPIC);
        blockItem(ModBlocks.AURORA_LAMP, Rarity.RARE);
        blockItem(ModBlocks.CRYO_GENERATOR, Rarity.RARE);
        blockItem(ModBlocks.FESTIVE_GARLAND, Rarity.COMMON);
        blockItem(ModBlocks.CHRISTMAS_ORNAMENT, Rarity.COMMON);
        blockItem(ModBlocks.YUKI_TREE, Rarity.UNCOMMON);
        blockItem(ModBlocks.SNOW_GLOBE, Rarity.UNCOMMON);
        blockItem(ModBlocks.SLOT_MACHINE, Rarity.EPIC);
    }

    private static DeferredItem<Item> food(String name, int nutrition, float sat, Rarity rarity, MobEffectInstance... effects) {
        return ITEMS.register(name, () -> {
            FoodProperties.Builder b = new FoodProperties.Builder().nutrition(nutrition).saturationModifier(sat).alwaysEdible();
            for (MobEffectInstance e : effects) b.effect(new MobEffectInstance(e), 1f);
            return new Item(new Item.Properties().rarity(rarity).food(b.build()));
        });
    }

    private static DeferredItem<Item> material(String name, Rarity rarity, boolean foil) {
        return ITEMS.register(name, () -> new CoreItem(new Item.Properties().rarity(rarity), foil));
    }

    private static DeferredItem<Item> core(String name, Rarity rarity, boolean foil) {
        return ITEMS.register(name, () -> new CoreItem(new Item.Properties().rarity(rarity).fireResistant(), foil));
    }

    private static DeferredItem<Item> sigil(String name,
            java.util.function.Supplier<? extends net.minecraft.world.entity.EntityType<? extends net.minecraft.world.entity.Mob>> boss,
            String planet) {
        return ITEMS.register(name, () -> new SigilItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)
                .fireResistant(), boss, planet == null ? null : YukiCore.id(planet)));
    }

    private static DeferredItem<Item> consumable(String name, MagicConsumableItem.Kind kind, Rarity rarity, int stack) {
        return ITEMS.register(name, () -> new MagicConsumableItem(new Item.Properties().stacksTo(stack).rarity(rarity),
                kind, rarity == Rarity.EPIC));
    }

    private static DeferredItem<Item> ring(String name, School school, float power, Rarity rarity) {
        return ITEMS.register(name, () -> new MagicGearItem(new Item.Properties().stacksTo(1).rarity(rarity), school, power));
    }

    private static DeferredItem<Item> robe(String name, Holder<ArmorMaterial> material, ArmorItem.Type type, School school,
                                           float power, float cheap, float mana, YukiArmorItem.Tier tier) {
        boolean top = tier.protection() >= 0.1f;
        return ITEMS.register(name, () -> new RobeItem(material, type, new Item.Properties().rarity(top ? Rarity.EPIC : Rarity.RARE)
                .fireResistant().durability(type.getDurability(top ? 80 : 30)), school, power, cheap, mana, tier));
    }

    private static Item.Properties toolProps(Rarity rarity) {
        return new Item.Properties().rarity(rarity).fireResistant();
    }

    private static DeferredItem<Item> sword(String mat, Tier tier, Rarity r) {
        return ITEMS.register(mat + "_sword", () -> new FrostSwordItem(tier,
                toolProps(r).attributes(SwordItem.createAttributes(tier, 3, -2.3f))));
    }

    private static DeferredItem<Item> pickaxe(String mat, Tier tier, Rarity r) {
        return ITEMS.register(mat + "_pickaxe", () -> new PickaxeItem(tier,
                toolProps(r).attributes(DiggerItem.createAttributes(tier, 1.0f, -2.8f))));
    }

    private static DeferredItem<Item> axe(String mat, Tier tier, Rarity r) {
        return ITEMS.register(mat + "_axe", () -> new AxeItem(tier,
                toolProps(r).attributes(DiggerItem.createAttributes(tier, 5.0f, -3.0f))));
    }

    private static DeferredItem<Item> shovel(String mat, Tier tier, Rarity r) {
        return ITEMS.register(mat + "_shovel", () -> new ShovelItem(tier,
                toolProps(r).attributes(DiggerItem.createAttributes(tier, 1.5f, -3.0f))));
    }

    private static DeferredItem<Item> hoe(String mat, Tier tier, Rarity r) {
        return ITEMS.register(mat + "_hoe", () -> new HoeItem(tier,
                toolProps(r).attributes(DiggerItem.createAttributes(tier, -4.0f, 0.0f))));
    }

    private static DeferredItem<Item> armor(String mat, Holder<ArmorMaterial> material, ArmorItem.Type type,
                                            int durability, Rarity r) {
        return ITEMS.register(mat + "_" + type.getName(), () -> new YukiArmorItem(material, type,
                toolProps(r).durability(type.getDurability(durability)), mat));
    }

    private static void blockItem(DeferredBlock<? extends Block> block, Rarity rarity) {
        ITEMS.register(block.getId().getPath(),
                () -> new dev.yukito.yukicore.item.DescBlockItem(block.get(), new Item.Properties().rarity(rarity)));
    }

    private ModItems() {}
}
