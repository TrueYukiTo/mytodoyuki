package dev.yukito.yukicore.client.armor;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/** 3D-броня: гуманоидная модель с дополнительными частями. Префикс части — какой слот её показывает. */
public class ArmorModel3D extends HumanoidModel<LivingEntity> {
    private final List<ModelPart> helm = new ArrayList<>(), chest = new ArrayList<>(), legs = new ArrayList<>(),
            boots = new ArrayList<>();

    public ArmorModel3D(ModelPart root, String set) {
        super(root);
        ArmorLayers.PATHS.get(set).forEach((name, path) -> {
            ModelPart p = root;
            for (String s : path) p = p.getChild(s);
            switch (name.substring(0, Math.min(2, name.length()))) {
                case "h_" -> helm.add(p);
                case "c_" -> chest.add(p);
                case "l_" -> legs.add(p);
                case "b_" -> boots.add(p);
                default -> {
                }
            }
        });
    }

    /** Показать только части нужного слота. */
    public void show(EquipmentSlot slot) {
        helm.forEach(p -> p.visible = slot == EquipmentSlot.HEAD);
        chest.forEach(p -> p.visible = slot == EquipmentSlot.CHEST);
        legs.forEach(p -> p.visible = slot == EquipmentSlot.LEGS);
        boots.forEach(p -> p.visible = slot == EquipmentSlot.FEET);
    }

    /** Видимость базовых частей как у ванильного слоя брони. */
    public void baseFor(EquipmentSlot slot) {
        setAllVisible(false);
        switch (slot) {
            case HEAD -> head.visible = true;
            case CHEST -> {
                body.visible = true;
                rightArm.visible = true;
                leftArm.visible = true;
            }
            case LEGS -> {
                body.visible = true;
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
            case FEET -> {
                rightLeg.visible = true;
                leftLeg.visible = true;
            }
            default -> {
            }
        }
    }
}
