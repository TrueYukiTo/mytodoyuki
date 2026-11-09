package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import net.minecraft.Util;
import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.crafting.Ingredient;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.EnumMap;
import java.util.List;

public final class ModArmorMaterials {
    public static final DeferredRegister<ArmorMaterial> ARMOR_MATERIALS =
            DeferredRegister.create(Registries.ARMOR_MATERIAL, YukiCore.MOD_ID);

    public static final DeferredHolder<ArmorMaterial, ArmorMaterial> FROST = ARMOR_MATERIALS.register("frost",
            () -> new ArmorMaterial(
                    Util.make(new EnumMap<>(ArmorItem.Type.class), m -> {
                        m.put(ArmorItem.Type.BOOTS, 3);
                        m.put(ArmorItem.Type.LEGGINGS, 6);
                        m.put(ArmorItem.Type.CHESTPLATE, 8);
                        m.put(ArmorItem.Type.HELMET, 3);
                        m.put(ArmorItem.Type.BODY, 11);
                    }),
                    18,
                    SoundEvents.ARMOR_EQUIP_DIAMOND,
                    () -> Ingredient.of(ModItems.FROST_INGOT.get()),
                    List.of(new ArmorMaterial.Layer(YukiCore.id("frost"))),
                    2.5f,
                    0.05f));

    private ModArmorMaterials() {}
}
