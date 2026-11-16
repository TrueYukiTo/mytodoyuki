package dev.yukito.yukicore.item;

import dev.yukito.yukicore.YukiCore;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.EquipmentSlotGroup;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.ItemAttributeModifiers;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Броня YukiCore. Сверх обычной защиты каждая часть даёт процент снижения любого урона и здоровье,
 * полный комплект — ещё бонус (см. ArmorSetHandler). У части сетов — 3D-модель.
 */
public class YukiArmorItem extends ArmorItem {
    /** Параметры брони: ключ комплекта, снижение урона за часть, ❤ за часть, бонус комплекта, 3D-модель. */
    public record Tier(String set, float protection, int health, float setBonus, @Nullable String model) {}

    public static final Tier FROST = new Tier("frost", 0.03f, 2, 0.03f, null);
    public static final Tier AURORIUM = new Tier("aurorium", 0.05f, 4, 0.05f, null);
    public static final Tier CELESTIUM = new Tier("celestium", 0.08f, 6, 0.10f, null);
    public static final Tier YUKI = new Tier("yuki", 0.15f, 20, 0f, null);
    public static final Tier ETERNAL = new Tier("eternal", 0.15f, 10, 0.15f, "eternal");
    public static final Tier CRYOMANCER = new Tier("cryomancer", 0.02f, 1, 0.04f, "cryomancer");
    public static final Tier PYROMANCER = new Tier("pyromancer", 0.02f, 1, 0.04f, "pyromancer");
    public static final Tier YUKI_REGALIA = new Tier("yuki_regalia", 0.10f, 6, 0.10f, "yuki_regalia");
    public static final Tier SINTARA_REGALIA = new Tier("sintara_regalia", 0.10f, 6, 0.10f, "sintara_regalia");

    public final Tier tier;
    private ItemAttributeModifiers mods;

    public YukiArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, Tier tier) {
        super(material, type, properties);
        this.tier = tier;
    }

    /** Совместимость со старым конструктором (тир по имени). */
    public YukiArmorItem(Holder<ArmorMaterial> material, Type type, Properties properties, String tier) {
        this(material, type, properties, switch (tier) {
            case "aurorium" -> AURORIUM;
            case "celestium" -> CELESTIUM;
            case "yuki" -> YUKI;
            case "eternal" -> ETERNAL;
            default -> FROST;
        });
    }

    @Override
    public ItemAttributeModifiers getDefaultAttributeModifiers() {
        if (mods == null) {
            ItemAttributeModifiers base = super.getDefaultAttributeModifiers();
            EquipmentSlotGroup group = EquipmentSlotGroup.bySlot(getEquipmentSlot());
            mods = tier.health() <= 0 ? base : base.withModifierAdded(Attributes.MAX_HEALTH, new AttributeModifier(
                    YukiCore.id("armor_health_" + getType().getName()), tier.health(), AttributeModifier.Operation.ADD_VALUE), group);
        }
        return mods;
    }

    @Override
    public @Nullable ResourceLocation getArmorTexture(ItemStack stack, Entity entity, EquipmentSlot slot,
                                                      ArmorMaterial.Layer layer, boolean inner) {
        return tier.model() == null ? null : YukiCore.id("textures/models/armor3d/" + tier.model() + ".png");
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        tooltip.add(Component.translatable("tooltip.yukicore.armor_protection", Math.round(tier.protection() * 100), tier.health())
                .withStyle(ChatFormatting.BLUE));
        tooltip.add(Component.translatable("tooltip.yukicore.set." + tier.set()).withStyle(ChatFormatting.AQUA));
        super.appendHoverText(stack, context, tooltip, flag);
    }
}
