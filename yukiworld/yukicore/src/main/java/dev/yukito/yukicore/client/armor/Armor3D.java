package dev.yukito.yukicore.client.armor;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.item.YukiArmorItem;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.PlayerSkin;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import net.neoforged.neoforge.client.extensions.common.RegisterClientExtensionsEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

/** Регистрация 3D-брони: слои моделей, расширения предметов и светящийся слой на игроке. */
public final class Armor3D {
    private static final Map<String, Supplier<LayerDefinition>> DEFS = Map.of(
            "cryomancer", ArmorLayers::cryomancer, "pyromancer", ArmorLayers::pyromancer,
            "yuki_regalia", ArmorLayers::yukiRegalia, "sintara_regalia", ArmorLayers::sintaraRegalia,
            "eternal", ArmorLayers::eternal);
    /** 7.4: новые комплекты используют геометрию существующих, но свои текстуры. */
    private static final Map<String, String> GEOMETRY = Map.of("frost_queen", "yuki_regalia", "fire_demon", "eternal");
    private static final Map<String, ArmorModel3D> MODELS = new HashMap<>();
    private static final EquipmentSlot[] SLOTS = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};

    private Armor3D() {}

    private static ModelLayerLocation loc(String set) {
        return new ModelLayerLocation(YukiCore.id("armor_" + set), "main");
    }

    public static void layers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        DEFS.forEach((set, def) -> e.registerLayerDefinition(loc(set), def));
    }

    public static ArmorModel3D model(String set) {
        return MODELS.computeIfAbsent(set, s -> {
            String geo = GEOMETRY.getOrDefault(s, s);
            return new ArmorModel3D(Minecraft.getInstance().getEntityModels().bakeLayer(loc(geo)), geo);
        });
    }

    public static void extensions(RegisterClientExtensionsEvent e) {
        ModItems.ITEMS.getEntries().forEach(h -> {
            if (h.get() instanceof YukiArmorItem a && a.tier.model() != null) {
                String set = a.tier.model();
                e.registerItem(new IClientItemExtensions() {
                    @Override
                    public HumanoidModel<?> getHumanoidArmorModel(LivingEntity entity, ItemStack stack, EquipmentSlot slot,
                                                                   HumanoidModel<?> original) {
                        ArmorModel3D m = model(set);
                        m.show(slot);
                        return m;
                    }
                }, a);
            }
        });
    }

    public static void addLayers(EntityRenderersEvent.AddLayers e) {
        for (PlayerSkin.Model skin : e.getSkins()) {
            if (e.getSkin(skin) instanceof PlayerRenderer r) r.addLayer(new Glow(r));
        }
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private static void copyPose(HumanoidModel from, HumanoidModel to) {
        from.copyPropertiesTo(to);
    }

    /** Светящиеся части брони (лёд, лава, глаза) — поверх обычного слоя. */
    static class Glow extends RenderLayer<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> {
        Glow(RenderLayerParent<AbstractClientPlayer, PlayerModel<AbstractClientPlayer>> parent) {
            super(parent);
        }

        @Override
        public void render(PoseStack pose, MultiBufferSource buf, int light, AbstractClientPlayer p, float limb, float amount,
                           float partial, float age, float yaw, float pitch) {
            for (EquipmentSlot slot : SLOTS) {
                if (!(p.getItemBySlot(slot).getItem() instanceof YukiArmorItem a) || a.tier.model() == null) continue;
                ArmorModel3D m = model(a.tier.model());
                copyPose(getParentModel(), m);
                m.baseFor(slot);
                m.show(slot);
                RenderType rt = RenderType.eyes(YukiCore.id("textures/models/armor3d/" + a.tier.model() + "_glow.png"));
                m.renderToBuffer(pose, buf.getBuffer(rt), 0xF000F0, OverlayTexture.NO_OVERLAY);
            }
        }
    }

}
