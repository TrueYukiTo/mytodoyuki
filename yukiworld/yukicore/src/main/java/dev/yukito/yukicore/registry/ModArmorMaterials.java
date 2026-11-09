package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import net.minecraft.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;
import java.util.function.Supplier;

public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, YukiCore.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FROST = register("frost",
            4, 9, 7, 4, 20, 3.5f, 0.1f, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> ModItems.FROST_INGOT.get());
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> AURORIUM = register("aurorium",
            5, 10, 8, 5, 24, 4.5f, 0.15f, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> ModItems.AURORIUM_INGOT.get());
    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> CELESTIUM = register("celestium",
            6, 12, 9, 6, 30, 6.0f, 0.25f, SoundEvents.ARMOR_EQUIP_NETHERITE, () -> ModItems.CELESTIUM_INGOT.get());

    private static DeferredHolder<ArmorMaterial, ArmorMaterial> register(String name, int boots, int chest, int legs,
            int helmet, int enchant, float toughness, float knockback, Holder<SoundEvent> sound, Supplier<Item> repair) {
        return ARMOR_MATERIALS.register(name, () -> new ArmorMaterial(
                Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                    m.put(ArmorItem.Type.BOOTS, boots);
                    m.put(ArmorItem.Type.LEGGINGS, legs);
                    m.put(ArmorItem.Type.CHESTPLATE, chest);
                    m.put(ArmorItem.Type.HELMET, helmet);
                    m.put(ArmorItem.Type.BODY, chest + 3);
                }),
                enchant, sound, () -> Ingredient.of(repair.get()),
                List.of(new ArmorMaterial.Layer(YukiCore.id(name))), toughness, knockback));
    }

    private ModArmorMaterials() {}
}
