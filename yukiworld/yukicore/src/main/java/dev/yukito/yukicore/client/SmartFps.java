package dev.yukito.yukicore.client;

import dev.yukito.yukicore.Config;
import net.minecraft.client.Minecraft;

/**
 * Снижает лимит FPS, пока окно игры не в фокусе (свернуто / alt-tab).
 * Видеокарта и процессор перестают работать впустую, ноутбук не греется.
 */
public final class SmartFps {
    private static boolean throttled = false;
    private static int userMax = -1;      // дальность, выбранная игроком
    private static int lastSet = -1;      // дальность, которую поставили мы
    private static long lastCheck = 0;

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
        dynamicRenderDistance(mc);
    }

    /**
     * Адаптивная дальность прорисовки: если FPS долго ниже цели — уменьшаем на 1 чанк,
     * если запас большой — возвращаем к значению игрока. Шаг раз в 4 секунды, без рывков.
     */
    private static void dynamicRenderDistance(Minecraft mc) {
        if (!Config.DYNAMIC_RENDER_DISTANCE.get() || mc.level == null || mc.screen != null || throttled) return;
        long now = System.currentTimeMillis();
        if (now - lastCheck < 4000) return;
        lastCheck = now;
        int current = mc.options.renderDistance().get();
        if (userMax < 0 || current != lastSet) userMax = current;   // игрок поменял вручную
        int fps = mc.getFps(), target = Config.TARGET_FPS.get();
        int next = current;
        if (fps < target - 8 && current > Config.MIN_RENDER_DISTANCE.get()) next = current - 1;
        else if (fps > target + 25 && current < userMax) next = current + 1;
        if (next != current) {
            mc.options.renderDistance().set(next);
            lastSet = next;
        } else {
            lastSet = current;
        }
    }
}
