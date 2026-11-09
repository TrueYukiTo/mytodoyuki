package dev.yukito.yukicore.client;

import dev.yukito.yukicore.Config;
import net.minecraft.client.Minecraft;

/**
 * Снижает лимит FPS, пока окно игры не в фокусе (свернуто / alt-tab).
 * Видеокарта и процессор перестают работать впустую, ноутбук не греется.
 */
public final class SmartFps {
    private static boolean throttled = false;

    private SmartFps() {}

    static void tick() {
        Minecraft mc = Minecraft.getInstance();
        boolean shouldThrottle = Config.SMART_FPS.get() && !mc.isWindowActive();

        if (shouldThrottle && !throttled) {
            mc.getWindow().setFramerateLimit(Config.UNFOCUSED_FPS.get());
            throttled = true;
        } else if (!shouldThrottle && throttled) {
            mc.getWindow().setFramerateLimit(mc.options.framerateLimit().get());
            throttled = false;
        }
    }
}
