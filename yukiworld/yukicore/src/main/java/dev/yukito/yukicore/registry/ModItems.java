package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.item.FrostSwordItem;
import dev.yukito.yukicore.item.YukiStarItem;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.Block;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(YukiCore.MOD_ID);

    // --- Материалы ---
    public static final DeferredItem<Item> YUKI_CRYSTAL = ITEMS.registerSimpleItem("yuki_crystal");
    public static final DeferredItem<Item> RAW_FROST = ITEMS.registerSimpleItem("raw_frost");
    public static final DeferredItem<Item> FROST_INGOT = ITEMS.registerSimpleItem("frost_ingot");

    // --- Особые предметы ---
    public static final DeferredItem<Item> YUKI_STAR = ITEMS.register("yuki_star",
            () -> new YukiStarItem(new Item.Properties().stacksTo(1).rarity(Rarity.EPIC)));

    public static final DeferredItem<Item> FROST_APPLE = ITEMS.register("frost_apple",
            () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON).food(new FoodProperties.Builder()
                    .nutrition(6).saturationModifier(1.2f).alwaysEdible()
                    .effect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 20 * 30, 1), 1.0f)
                    .effect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 20 * 60, 0), 1.0f)
                    .build())));

    // --- Инструменты ---
    public static final DeferredItem<Item> FROST_SWORD = ITEMS.register("frost_sword",
            () -> new FrostSwordItem(ModTiers.FROST, new Item.Properties()
                    .attributes(SwordItem.createAttributes(ModTiers.FROST, 3, -2.3f))));
    public static final DeferredItem<Item> FROST_PICKAXE = ITEMS.register("frost_pickaxe",
            () -> new PickaxeItem(ModTiers.FROST, new Item.Properties()
                    .attributes(DiggerItem.createAttributes(ModTiers.FROST, 1.0f, -2.8f))));
    public static final DeferredItem<Item> FROST_AXE = ITEMS.register("frost_axe",
            () -> new AxeItem(ModTiers.FROST, new Item.Properties()
                    .attributes(DiggerItem.createAttributes(ModTiers.FROST, 5.0f, -3.0f))));
    public static final DeferredItem<Item> FROST_SHOVEL = ITEMS.register("frost_shovel",
            () -> new ShovelItem(ModTiers.FROST, new Item.Properties()
                    .attributes(DiggerItem.createAttributes(ModTiers.FROST, 1.5f, -3.0f))));
    public static final DeferredItem<Item> FROST_HOE = ITEMS.register("frost_hoe",
            () -> new HoeItem(ModTiers.FROST, new Item.Properties()
                    .attributes(DiggerItem.createAttributes(ModTiers.FROST, -3.0f, 0.0f))));

    // --- Броня ---
    public static final DeferredItem<Item> FROST_HELMET = armor("frost_helmet", ArmorItem.Type.HELMET);
    public static final DeferredItem<Item> FROST_CHESTPLATE = armor("frost_chestplate", ArmorItem.Type.CHESTPLATE);
    public static final DeferredItem<Item> FROST_LEGGINGS = armor("frost_leggings", ArmorItem.Type.LEGGINGS);
    public static final DeferredItem<Item> FROST_BOOTS = armor("frost_boots", ArmorItem.Type.BOOTS);

    // --- Блоки ---
    static {
        blockItem(ModBlocks.YUKI_CRYSTAL_ORE);
        blockItem(ModBlocks.DEEPSLATE_YUKI_CRYSTAL_ORE);
        blockItem(ModBlocks.FROST_ORE);
        blockItem(ModBlocks.FROST_BLOCK);
        blockItem(ModBlocks.YUKI_CRYSTAL_BLOCK);
        blockItem(ModBlocks.YUKI_LAMP);
    }

    private static DeferredItem<Item> armor(String name, ArmorItem.Type type) {
        return ITEMS.register(name, () -> new ArmorItem(ModArmorMaterials.FROST, type,
                new Item.Properties().durability(type.getDurability(35))));
    }

    private static void blockItem(DeferredBlock<? extends Block> block) {
        ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(), new Item.Properties()));
    }

    private ModItems() {}
}
