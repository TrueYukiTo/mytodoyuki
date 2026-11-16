package dev.yukito.yukicore.compat.adastra;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.registry.ModArmorMaterials;
import earth.terrarium.adastra.common.entities.vehicles.Rocket;
import earth.terrarium.adastra.common.items.armor.SpaceSuitItem;
import earth.terrarium.adastra.common.items.vehicles.RocketItem;
import earth.terrarium.adastra.common.tags.ModFluidTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.ArmorMaterial;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Rarity;
import net.minecraft.world.item.TooltipFlag;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.event.BuildCreativeModeTabContentsEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

/**
 * Ракеты и скафандры YukiCore на основе Ad Astra. Класс загружается только если Ad Astra установлен.
 * Ракеты 5–7 уровня открывают дальние планеты Системы Юки.
 */
public final class AdAstraCompat {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(YukiCore.MOD_ID);
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(Registries.ENTITY_TYPE, YukiCore.MOD_ID);

    public static final String[] ROCKETS = {"frost_rocket", "sintara_rocket", "star_rocket"};

    public static final DeferredItem<Item> FROST_ROCKET = rocketItem("frost_rocket", 5, Rarity.RARE);
    public static final DeferredItem<Item> SINTARA_ROCKET = rocketItem("sintara_rocket", 6, Rarity.EPIC);
    public static final DeferredItem<Item> STAR_ROCKET = rocketItem("star_rocket", 7, Rarity.EPIC);

    public static final DeferredHolder<EntityType<?>, EntityType<Rocket>> FROST_ROCKET_ENTITY = rocket("frost_rocket", 5, FROST_ROCKET);
    public static final DeferredHolder<EntityType<?>, EntityType<Rocket>> SINTARA_ROCKET_ENTITY = rocket("sintara_rocket", 6, SINTARA_ROCKET);
    public static final DeferredHolder<EntityType<?>, EntityType<Rocket>> STAR_ROCKET_ENTITY = rocket("star_rocket", 7, STAR_ROCKET);

    public static final DeferredItem<Item>[] FROST_SUIT = suit("frost", ModArmorMaterials.FROST_SUIT, 2000, 33, Rarity.RARE);
    public static final DeferredItem<Item>[] SINTARA_SUIT = suit("sintara", ModArmorMaterials.SINTARA_SUIT, 4000, 37, Rarity.EPIC);
    public static final DeferredItem<Item>[] STAR_SUIT = suit("star", ModArmorMaterials.STAR_SUIT, 8000, 45, Rarity.EPIC);

    private AdAstraCompat() {}

    public static void register(IEventBus modBus) {
        ITEMS.register(modBus);
        ENTITIES.register(modBus);
        modBus.addListener(AdAstraCompat::tabs);
    }

    private static void tabs(BuildCreativeModeTabContentsEvent e) {
        if (e.getTabKey().location().equals(YukiCore.id("yukiworld"))) {
            ITEMS.getEntries().forEach(i -> e.accept(i.get()));
        }
    }

    private static DeferredItem<Item> rocketItem(String name, int tier, Rarity rarity) {
        return ITEMS.register(name, () -> new RocketItem(() -> switch (name) {
            case "frost_rocket" -> FROST_ROCKET_ENTITY.get();
            case "sintara_rocket" -> SINTARA_ROCKET_ENTITY.get();
            default -> STAR_ROCKET_ENTITY.get();
        }, new Item.Properties().stacksTo(1).fireResistant().rarity(rarity)) {
            @Override
            public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                tooltip.add(Component.translatable("tooltip.yukicore.rocket_tier", tier).withStyle(ChatFormatting.AQUA));
                super.appendHoverText(stack, ctx, tooltip, flag);
            }
        });
    }

    private static DeferredHolder<EntityType<?>, EntityType<Rocket>> rocket(String name, int tier, DeferredItem<Item> item) {
        return ENTITIES.register(name, () -> EntityType.Builder.<Rocket>of(
                        (type, level) -> new Rocket(type, level,
                                new Rocket.RocketProperties(tier, item.get(), 1.7f, ModFluidTags.TIER_4_ROCKET_FUEL)),
                        MobCategory.MISC)
                .sized(1.1f, 5.5f).fireImmune().clientTrackingRange(10).build(name));
    }

    @SuppressWarnings("unchecked")
    private static DeferredItem<Item>[] suit(String mat, Holder<ArmorMaterial> material, long tank, int durability, Rarity r) {
        ArmorItem.Type[] types = {ArmorItem.Type.HELMET, ArmorItem.Type.CHESTPLATE, ArmorItem.Type.LEGGINGS, ArmorItem.Type.BOOTS};
        String[] names = {"helmet", "suit", "pants", "boots"};
        DeferredItem<Item>[] out = new DeferredItem[4];
        for (int i = 0; i < 4; i++) {
            ArmorItem.Type t = types[i];
            out[i] = ITEMS.register(mat + "_space_" + names[i], () -> new SpaceSuitItem(material, t,
                    t == ArmorItem.Type.CHESTPLATE ? tank : 0, new Item.Properties().fireResistant().rarity(r), durability) {
                @Override
                public void appendHoverText(ItemStack stack, TooltipContext ctx, List<Component> tooltip, TooltipFlag flag) {
                    tooltip.add(Component.translatable("tooltip.yukicore.space_suit." + mat).withStyle(ChatFormatting.AQUA));
                    super.appendHoverText(stack, ctx, tooltip, flag);
                }
            });
        }
        return out;
    }
}
