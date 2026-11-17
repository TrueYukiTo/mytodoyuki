package dev.yukito.yukicore.gear;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.item.DescBlockItem;
import dev.yukito.yukicore.item.YukiArmorItem;
import dev.yukito.yukicore.registry.ModArmorMaterials;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModItems;
import dev.yukito.yukicore.registry.ModMagic;
import dev.yukito.yukicore.registry.ModTiers;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;

import java.util.EnumMap;
import java.util.List;

/**
 * 7.4 «Арсенал стихий»: оружие, снаряжение, инструменты, расходники, скакуны, наковальня стихий и камни,
 * два новых 3D-комплекта брони. Регистрируется в общих реестрах YukiCore (вызов {@link #init()} до их регистрации).
 */
public final class GearItems {
    private GearItems() {}

    public static void init() {
        // загрузка класса регистрирует все поля
    }

    // ------------------------------------------------------------------ эффекты зелий
    public static final DeferredHolder<MobEffect, MobEffect> ICE_SKIN = ModMagic.EFFECTS.register("ice_skin",
            () -> new GearEffects.IceSkin().addAttributeModifier(Attributes.ARMOR, YukiCore.id("ice_skin"), 8,
                    AttributeModifier.Operation.ADD_VALUE));
    public static final DeferredHolder<MobEffect, MobEffect> FIRE_BLOOD = ModMagic.EFFECTS.register("fire_blood",
            () -> new GearEffects.Plain(MobEffectCategory.BENEFICIAL, 0xFF5A1A));
    public static final DeferredHolder<MobEffect, MobEffect> SINTARA_LUCK = ModMagic.EFFECTS.register("sintara_luck",
            () -> new GearEffects.Plain(MobEffectCategory.BENEFICIAL, 0xFFD060).addAttributeModifier(Attributes.LUCK,
                    YukiCore.id("sintara_luck"), 3, AttributeModifier.Operation.ADD_VALUE));

    // ------------------------------------------------------------------ броня: Ледяная королева и Огненный демон
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FROST_QUEEN_MAT = material("frost_queen",
            SoundEvents.ARMOR_EQUIP_DIAMOND, () -> ModItems.AURORIUM_INGOT.get());
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FIRE_DEMON_MAT = material("fire_demon",
            SoundEvents.ARMOR_EQUIP_NETHERITE, () -> ModItems.AURORIUM_INGOT.get());
    public static final YukiArmorItem.Tier FROST_QUEEN = new YukiArmorItem.Tier("frost_queen", 0.08f, 5, 0.08f, "frost_queen");
    public static final YukiArmorItem.Tier FIRE_DEMON = new YukiArmorItem.Tier("fire_demon", 0.08f, 5, 0.08f, "fire_demon");

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> material(String name, Holder<net.minecraft.sounds.SoundEvent> sound,
                                                                       java.util.function.Supplier<Item> repair) {
        return ModArmorMaterials.ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                    m.put(ArmorItem.Type.BOOTS, 4);
                    m.put(ArmorItem.Type.LEGGINGS, 9);
                    m.put(ArmorItem.Type.CHESTPLATE, 11);
                    m.put(ArmorItem.Type.HELMET, 5);
                    m.put(ArmorItem.Type.BODY, 11);
                }), 30, sound, () -> Ingredient.of(repair.get()),
                List.of(new ArmorMaterial.Layer(YukiCore.id(name))), 4f, 0.15f));
    }

    private static DeferredItem<Item> armor(String set, Holder<ArmorMaterial> mat, ArmorItem.Type type, YukiArmorItem.Tier tier) {
        return ModItems.ITEMS.register(set + "_" + type.getName(), () -> new YukiArmorItem(mat, type,
                props(Rarity.EPIC).durability(type.getDurability(60)), tier));
    }

    public static final DeferredItem<Item> FROST_QUEEN_HELMET = armor("frost_queen", FROST_QUEEN_MAT, ArmorItem.Type.HELMET, FROST_QUEEN);
    public static final DeferredItem<Item> FROST_QUEEN_CHESTPLATE = armor("frost_queen", FROST_QUEEN_MAT, ArmorItem.Type.CHESTPLATE, FROST_QUEEN);
    public static final DeferredItem<Item> FROST_QUEEN_LEGGINGS = armor("frost_queen", FROST_QUEEN_MAT, ArmorItem.Type.LEGGINGS, FROST_QUEEN);
    public static final DeferredItem<Item> FROST_QUEEN_BOOTS = armor("frost_queen", FROST_QUEEN_MAT, ArmorItem.Type.BOOTS, FROST_QUEEN);
    public static final DeferredItem<Item> FIRE_DEMON_HELMET = armor("fire_demon", FIRE_DEMON_MAT, ArmorItem.Type.HELMET, FIRE_DEMON);
    public static final DeferredItem<Item> FIRE_DEMON_CHESTPLATE = armor("fire_demon", FIRE_DEMON_MAT, ArmorItem.Type.CHESTPLATE, FIRE_DEMON);
    public static final DeferredItem<Item> FIRE_DEMON_LEGGINGS = armor("fire_demon", FIRE_DEMON_MAT, ArmorItem.Type.LEGGINGS, FIRE_DEMON);
    public static final DeferredItem<Item> FIRE_DEMON_BOOTS = armor("fire_demon", FIRE_DEMON_MAT, ArmorItem.Type.BOOTS, FIRE_DEMON);

    // ------------------------------------------------------------------ оружие
    private static Item.Properties props(Rarity r) {
        return new Item.Properties().rarity(r).fireResistant();
    }

    public static final DeferredItem<Item> SINTARA_SCYTHE = ModItems.ITEMS.register("sintara_scythe",
            () -> new Weapons.Melee(ModTiers.AURORIUM, Weapons.Kind.SCYTHE, props(Rarity.EPIC)
                    .attributes(SwordItem.createAttributes(ModTiers.AURORIUM, 9, -2.8f))));
    public static final DeferredItem<Item> TWIN_BLADES = ModItems.ITEMS.register("twin_blades",
            () -> new Weapons.Melee(ModTiers.AURORIUM, Weapons.Kind.TWIN, props(Rarity.EPIC)
                    .attributes(SwordItem.createAttributes(ModTiers.AURORIUM, 4, -1.6f))));
    public static final DeferredItem<Item> METEOR_HAMMER = ModItems.ITEMS.register("meteor_hammer",
            () -> new Weapons.Melee(ModTiers.AURORIUM, Weapons.Kind.HAMMER, props(Rarity.EPIC)
                    .attributes(SwordItem.createAttributes(ModTiers.AURORIUM, 12, -3.2f))));
    public static final DeferredItem<Item> BATTLE_FAN = ModItems.ITEMS.register("battle_fan",
            () -> new Weapons.Melee(ModTiers.FROST, Weapons.Kind.FAN, props(Rarity.RARE)
                    .attributes(SwordItem.createAttributes(ModTiers.FROST, 3, -2.0f))));
    public static final DeferredItem<Item> YUKI_BOW = ModItems.ITEMS.register("yuki_bow",
            () -> new Weapons.FrostBow(props(Rarity.EPIC).durability(1200)));

    // ------------------------------------------------------------------ снаряжение
    public static final DeferredItem<Item> CLOUD_RING = gear("cloud_ring", Trinkets.Kind.DOUBLE_JUMP, Rarity.RARE);
    public static final DeferredItem<Item> SECOND_CHANCE_AMULET = gear("second_chance_amulet", Trinkets.Kind.SECOND_CHANCE, Rarity.EPIC);
    public static final DeferredItem<Item> PHOENIX_CLOAK = gear("phoenix_cloak", Trinkets.Kind.PHOENIX_CLOAK, Rarity.EPIC);
    public static final DeferredItem<Item> MINER_RING = gear("miner_ring", Trinkets.Kind.MINER, Rarity.RARE);
    public static final DeferredItem<Item> DASH_BOOTS = ModItems.ITEMS.register("dash_boots",
            () -> new Trinkets.Boots(ModArmorMaterials.FROST, props(Rarity.RARE).durability(ArmorItem.Type.BOOTS.getDurability(40)), false));
    public static final DeferredItem<Item> ICE_SKATES = ModItems.ITEMS.register("ice_skates",
            () -> new Trinkets.Boots(ModArmorMaterials.FROST, props(Rarity.RARE).durability(ArmorItem.Type.BOOTS.getDurability(40)), true));

    private static DeferredItem<Item> gear(String name, Trinkets.Kind kind, Rarity r) {
        return ModItems.ITEMS.register(name, () -> new Trinkets.Trinket(props(r).stacksTo(1), kind));
    }

    // ------------------------------------------------------------------ инструменты
    public static final DeferredItem<Item> YUKI_DRILL = ModItems.ITEMS.register("yuki_drill",
            () -> new Tools.Drill(ModTiers.AURORIUM, props(Rarity.EPIC)
                    .attributes(DiggerItem.createAttributes(ModTiers.AURORIUM, 2.0f, -2.8f))));
    public static final DeferredItem<Item> BUILDER_WAND = ModItems.ITEMS.register("builder_wand",
            () -> new Tools.BuilderWand(props(Rarity.RARE).stacksTo(1)));
    public static final DeferredItem<Item> TELEPORT_ORB = ModItems.ITEMS.register("teleport_orb",
            () -> new Tools.TeleportOrb(props(Rarity.EPIC).stacksTo(1)));
    public static final DeferredItem<Item> SINTARA_BACKPACK = ModItems.ITEMS.register("sintara_backpack",
            () -> new Backpack(props(Rarity.EPIC).stacksTo(1)
                    .component(DataComponents.CONTAINER, net.minecraft.world.item.component.ItemContainerContents.EMPTY)));

    // ------------------------------------------------------------------ расходники
    public static final DeferredItem<Item> FROST_BOMB = ModItems.ITEMS.register("frost_bomb",
            () -> new Consumables.Bomb(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), 0));
    public static final DeferredItem<Item> FIRE_BOMB = ModItems.ITEMS.register("fire_bomb",
            () -> new Consumables.Bomb(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), 1));
    public static final DeferredItem<Item> SNARE_BOMB = ModItems.ITEMS.register("snare_bomb",
            () -> new Consumables.Bomb(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), 2));
    public static final DeferredItem<Item> ICE_SKIN_POTION = ModItems.ITEMS.register("ice_skin_potion",
            () -> new Consumables.ElementPotion(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 0));
    public static final DeferredItem<Item> FIRE_BLOOD_POTION = ModItems.ITEMS.register("fire_blood_potion",
            () -> new Consumables.ElementPotion(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 1));
    public static final DeferredItem<Item> LUCK_POTION = ModItems.ITEMS.register("luck_potion",
            () -> new Consumables.ElementPotion(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 2));
    public static final DeferredItem<Item> RECALL_SCROLL = ModItems.ITEMS.register("recall_scroll",
            () -> new Consumables.Scroll(new Item.Properties().stacksTo(16).rarity(Rarity.UNCOMMON), 0));
    public static final DeferredItem<Item> GOLEM_SCROLL = ModItems.ITEMS.register("golem_scroll",
            () -> new Consumables.Scroll(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 1));
    public static final DeferredItem<Item> FIRE_RAIN_SCROLL = ModItems.ITEMS.register("fire_rain_scroll",
            () -> new Consumables.Scroll(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), 2));

    // ------------------------------------------------------------------ скакуны
    public static final DeferredItem<Item> FROST_STEED_WHISTLE = ModItems.ITEMS.register("frost_steed_whistle",
            () -> new Mounts.Whistle(props(Rarity.EPIC).stacksTo(1), true));
    public static final DeferredItem<Item> FLAME_STEED_WHISTLE = ModItems.ITEMS.register("flame_steed_whistle",
            () -> new Mounts.Whistle(props(Rarity.EPIC).stacksTo(1), false));

    // ------------------------------------------------------------------ наковальня стихий и камни
    public static final DeferredBlock<Block> ELEMENTAL_ANVIL = ModBlocks.BLOCKS.register("elemental_anvil",
            () -> new Gems.AnvilBlock(BlockBehaviour.Properties.ofFullCopy(Blocks.ANVIL).lightLevel(s -> 7)));
    public static final DeferredItem<Item> ELEMENTAL_ANVIL_ITEM = ModItems.ITEMS.register("elemental_anvil",
            () -> new DescBlockItem(ELEMENTAL_ANVIL.get(), new Item.Properties().rarity(Rarity.EPIC)));
    public static final DeferredItem<Item> EMBER_GEM = gem("ember_gem", Gems.Type.EMBER);
    public static final DeferredItem<Item> FROST_GEM = gem("frost_gem", Gems.Type.FROST);
    public static final DeferredItem<Item> BLOOD_GEM = gem("blood_gem", Gems.Type.BLOOD);
    public static final DeferredItem<Item> STORM_GEM = gem("storm_gem", Gems.Type.STORM);

    private static DeferredItem<Item> gem(String name, Gems.Type type) {
        return ModItems.ITEMS.register(name, () -> new Gems.GemItem(new Item.Properties().stacksTo(16).rarity(Rarity.RARE), type));
    }
}
