package dev.yukito.yukicore.compat.adastra;

import dev.yukito.yukicore.YukiCore;
import earth.terrarium.adastra.client.models.entities.vehicles.RocketModel;
import earth.terrarium.adastra.client.renderers.entities.vehicles.RocketRenderer;
import earth.terrarium.adastra.client.neoforge.AdAstraClientNeoForge;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;

/** Клиент: модели ракет Ad Astra (4-й уровень) с собственными текстурами YukiCore. */
public final class AdAstraClient {
    private AdAstraClient() {}

    private static ResourceLocation tex(String name) {
        return YukiCore.id("textures/entity/rocket/" + name + ".png");
    }

    public static void renderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerEntityRenderer(AdAstraCompat.FROST_ROCKET_ENTITY.get(),
                ctx -> new RocketRenderer(ctx, RocketModel.TIER_4_LAYER, tex("frost_rocket")));
        e.registerEntityRenderer(AdAstraCompat.SINTARA_ROCKET_ENTITY.get(),
                ctx -> new RocketRenderer(ctx, RocketModel.TIER_4_LAYER, tex("sintara_rocket")));
        e.registerEntityRenderer(AdAstraCompat.STAR_ROCKET_ENTITY.get(),
                ctx -> new RocketRenderer(ctx, RocketModel.TIER_4_LAYER, tex("star_rocket")));
    }

    /** Ракеты — это RenderedItem Ad Astra: она сама вешает рендер предмета и ищет его в своей таблице. */
    public static void setup(FMLClientSetupEvent e) {
        put(AdAstraCompat.FROST_ROCKET.get(), "frost_rocket");
        put(AdAstraCompat.SINTARA_ROCKET.get(), "sintara_rocket");
        put(AdAstraCompat.STAR_ROCKET.get(), "star_rocket");
    }

    private static void put(net.minecraft.world.item.Item item, String name) {
        AdAstraClientNeoForge.ITEM_RENDERERS.put(item, new RocketRenderer.ItemRenderer(RocketModel.TIER_4_LAYER, tex(name)));
    }
}
