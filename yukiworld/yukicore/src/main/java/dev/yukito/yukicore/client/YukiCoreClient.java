package dev.yukito.yukicore.client;

import dev.yukito.yukicore.YukiCore;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;
import net.neoforged.neoforge.common.NeoForge;

@Mod(value = YukiCore.MOD_ID, dist = Dist.CLIENT)
public final class YukiCoreClient {
    public YukiCoreClient(IEventBus modBus, ModContainer container) {
        // Кнопка "Настройки" в списке модов открывает встроенный экран конфига
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);

        modBus.addListener(YukiCoreClient::registerGuiLayers);
        modBus.addListener(YukiCoreClient::registerRenderers);
        NeoForge.EVENT_BUS.register(ClientEvents.class);
    }

    private static void registerRenderers(net.neoforged.neoforge.client.event.EntityRenderersEvent.RegisterRenderers e) {
        e.registerBlockEntityRenderer(dev.yukito.yukicore.registry.ModBlockEntities.ALTAR.get(), ItemStandRenderer::new);
        e.registerBlockEntityRenderer(dev.yukito.yukicore.registry.ModBlockEntities.PEDESTAL.get(), ItemStandRenderer::new);
        e.registerEntityRenderer(dev.yukito.yukicore.registry.ModEntities.FROST_COLOSSUS.get(), BossRenderers.Colossus::new);
        e.registerEntityRenderer(dev.yukito.yukicore.registry.ModEntities.BLIZZARD_QUEEN.get(), BossRenderers.Queen::new);
        e.registerEntityRenderer(dev.yukito.yukicore.registry.ModEntities.ASH_LORD.get(), BossRenderers.Ash::new);
        e.registerEntityRenderer(dev.yukito.yukicore.registry.ModEntities.VOID_WYRM.get(), BossRenderers.Wyrm::new);
        e.registerEntityRenderer(dev.yukito.yukicore.registry.ModEntities.SHADOW_OF_YUKI.get(), BossRenderers.Shadow::new);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(YukiCore.id("hud"), new YukiHud());
    }
}
