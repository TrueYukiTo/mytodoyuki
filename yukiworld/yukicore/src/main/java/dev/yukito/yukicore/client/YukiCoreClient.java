package dev.yukito.yukicore.client;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.client.boss.BossLayers;
import dev.yukito.yukicore.client.boss.BossModels;
import dev.yukito.yukicore.client.boss.YukiBossRenderer;
import dev.yukito.yukicore.client.magic.ClientMagic;
import dev.yukito.yukicore.client.magic.MagicHud;
import dev.yukito.yukicore.client.magic.MagicProjectileRenderer;
import dev.yukito.yukicore.registry.ModEntities;
import dev.yukito.yukicore.registry.ModMagic;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = YukiCore.MOD_ID, dist = Dist.CLIENT)
public final class YukiCoreClient {
    private static final ModelLayerLocation COLOSSUS = layer("frost_colossus");
    private static final ModelLayerLocation QUEEN = layer("blizzard_queen");
    private static final ModelLayerLocation ASH = layer("ash_lord");
    private static final ModelLayerLocation WYRM = layer("void_wyrm");
    private static final ModelLayerLocation SHADOW = layer("shadow_of_yuki");
    private static final ModelLayerLocation ARCHON = layer("glacial_archon");
    private static final ModelLayerLocation PHOENIX = layer("infernal_phoenix");

    public YukiCoreClient(IEventBus modBus, ModContainer container) {
        // Кнопка "Настройки" в списке модов открывает встроенный экран конфига
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(YukiCoreClient::registerGuiLayers);
        modBus.addListener(YukiCoreClient::registerRenderers);
        modBus.addListener(YukiCoreClient::registerLayers);
        modBus.addListener(ClientMagic::keys);
        modBus.addListener(dev.yukito.yukicore.client.armor.Armor3D::layers);
        modBus.addListener(dev.yukito.yukicore.client.armor.Armor3D::extensions);
        modBus.addListener(dev.yukito.yukicore.client.armor.Armor3D::addLayers);
        NeoForge.EVENT_BUS.addListener(ClientMagic::tick);
        NeoForge.EVENT_BUS.addListener(GearClient::tick);
        modBus.addListener((net.neoforged.fml.event.lifecycle.FMLClientSetupEvent ev) -> ev.enqueueWork(GearClient::bowProperties));
        NeoForge.EVENT_BUS.addListener(ClientMagic::camera);
        NeoForge.EVENT_BUS.register(ClientEvents.class);
        if (ModList.get().isLoaded("ad_astra")) {
            modBus.addListener(dev.yukito.yukicore.compat.adastra.AdAstraClient::setup);
        }
    }

    private static ModelLayerLocation layer(String name) {
        return new ModelLayerLocation(YukiCore.id(name), "main");
    }

    private static void registerLayers(EntityRenderersEvent.RegisterLayerDefinitions e) {
        e.registerLayerDefinition(COLOSSUS, BossLayers::frostColossus);
        e.registerLayerDefinition(QUEEN, BossLayers::blizzardQueen);
        e.registerLayerDefinition(ASH, BossLayers::ashLord);
        e.registerLayerDefinition(WYRM, BossLayers::voidWyrm);
        e.registerLayerDefinition(SHADOW, BossLayers::shadowOfYuki);
        e.registerLayerDefinition(ARCHON, BossLayers::glacialArchon);
        e.registerLayerDefinition(PHOENIX, BossLayers::infernalPhoenix);
    }

    private static void registerRenderers(EntityRenderersEvent.RegisterRenderers e) {
        e.registerBlockEntityRenderer(dev.yukito.yukicore.registry.ModBlockEntities.ALTAR.get(), ItemStandRenderer::new);
        e.registerBlockEntityRenderer(dev.yukito.yukicore.registry.ModBlockEntities.PEDESTAL.get(), ItemStandRenderer::new);
        e.registerEntityRenderer(ModEntities.FROST_COLOSSUS.get(), ctx -> new YukiBossRenderer<>(ctx,
                new BossModels.Colossus(ctx.bakeLayer(COLOSSUS)), "frost_colossus", 1.4f, 2.0f));
        e.registerEntityRenderer(ModEntities.BLIZZARD_QUEEN.get(), ctx -> new YukiBossRenderer<>(ctx,
                new BossModels.Queen(ctx.bakeLayer(QUEEN)), "blizzard_queen", 1.4f, 1.0f));
        e.registerEntityRenderer(ModEntities.ASH_LORD.get(), ctx -> new YukiBossRenderer<>(ctx,
                new BossModels.Ash(ctx.bakeLayer(ASH)), "ash_lord", 1.4f, 2.0f));
        e.registerEntityRenderer(ModEntities.VOID_WYRM.get(), ctx -> new YukiBossRenderer<>(ctx,
                new BossModels.Wyrm(ctx.bakeLayer(WYRM)), "void_wyrm", 1.6f, 2.0f));
        e.registerEntityRenderer(ModEntities.SHADOW_OF_YUKI.get(), ctx -> new YukiBossRenderer<>(ctx,
                new BossModels.Shadow(ctx.bakeLayer(SHADOW)), "shadow_of_yuki", 1.4f, 1.0f));
        e.registerEntityRenderer(ModEntities.GLACIAL_ARCHON.get(), ctx -> new YukiBossRenderer<>(ctx,
                new BossModels.Archon(ctx.bakeLayer(ARCHON)), "glacial_archon", 1.35f, 1.5f));
        e.registerEntityRenderer(ModEntities.INFERNAL_PHOENIX.get(), ctx -> new YukiBossRenderer<>(ctx,
                new BossModels.Phoenix(ctx.bakeLayer(PHOENIX)), "infernal_phoenix", 1.6f, 1.5f));
        e.registerEntityRenderer(ModMagic.PROJECTILE.get(), MagicProjectileRenderer::new);
        if (ModList.get().isLoaded("ad_astra")) dev.yukito.yukicore.compat.adastra.AdAstraClient.renderers(e);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(YukiCore.id("hud"), new YukiHud());
        event.registerAboveAll(YukiCore.id("magic"), new MagicHud());
    }
}
