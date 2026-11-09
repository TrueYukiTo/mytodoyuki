package dev.yukito.yukicore.client;

import dev.yukito.yukicore.Config;
import net.minecraft.client.gui.screens.TitleScreen;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ScreenEvent;

public final class ClientEvents {
    private ClientEvents() {}

    /**
     * Подменяем главное меню на меню YukiWorld.
     * Приоритет LOWEST: срабатываем последними, после модов, которые ставят своё меню
     * (например Cumulus Menus из The Aether) — их TitleScreen тоже заменяется.
     */
    @SubscribeEvent(priority = EventPriority.LOWEST)
    public static void onScreenOpening(ScreenEvent.Opening event) {
        if (event.getNewScreen() instanceof TitleScreen && Config.CUSTOM_TITLE_SCREEN.get()) {
            event.setNewScreen(new YukiTitleScreen());
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        SmartFps.tick();
    }
}
