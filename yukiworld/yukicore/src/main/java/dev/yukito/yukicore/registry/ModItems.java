package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.item.CoreItem;
import dev.yukito.yukicore.item.FrostSwordItem;
import dev.yukito.yukicore.item.InfinityStarItem;
import dev.yukito.yukicore.item.SigilItem;
import dev.yukito.yukicore.item.YukiArmorItem;
import dev.yukito.yukicore.item.YukiStarItem;
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
        blockItem(ModBlocks.CRYO_GENERATOR, Rarity.RARE);
        blockItem(ModBlocks.FESTIVE_GARLAND, Rarity.COMMON);
        blockItem(ModBlocks.CHRISTMAS_ORNAMENT, Rarity.COMMON);
        blockItem(ModBlocks.YUKI_TREE, Rarity.UNCOMMON);
        blockItem(ModBlocks.SNOW_GLOBE, Rarity.UNCOMMON);
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
                .fireResistant(), boss, YukiCore.id(planet)));
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
