package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.item.CoreItem;
import dev.yukito.yukicore.item.FrostSwordItem;
import dev.yukito.yukicore.item.InfinityStarItem;
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
    }

    private static DeferredItem<Item> material(String name, Rarity rarity, boolean foil) {
        return ITEMS.register(name, () -> new CoreItem(new Item.Properties().rarity(rarity), foil));
    }

    private static DeferredItem<Item> core(String name, Rarity rarity, boolean foil) {
        return ITEMS.register(name, () -> new CoreItem(new Item.Properties().rarity(rarity).fireResistant(), foil));
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
                () -> new BlockItem(block.get(), new Item.Properties().rarity(rarity)));
    }

    private ModItems() {}
}
