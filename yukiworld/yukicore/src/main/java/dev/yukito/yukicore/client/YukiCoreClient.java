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
        NeoForge.EVENT_BUS.register(ClientEvents.class);
    }

    private static void registerGuiLayers(RegisterGuiLayersEvent event) {
        event.registerAboveAll(YukiCore.id("hud"), new YukiHud());
    }
}
