package dev.yukito.yukicore.compat;

import dev.yukito.yukicore.YukiCore;
import net.minecraft.world.entity.Mob;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.living.FinalizeSpawnEvent;

/**
 * Защита от краша сервера «Late invocations of Mob#setSpawnCancelled are not permitted».
 * <p>
 * При превращениях (житель → зомби-житель, свинья → зомбифицированный пиглин и т.п.) Minecraft вызывает
 * FinalizeSpawnEvent для моба, который уже добавлен в мир. The Bumblezone (7.19.x) в этом событии всегда вызывает
 * setSpawnCancelled(...), а NeoForge для уже добавленного моба бросает исключение — сервер падает.
 * <p>
 * Здесь, раньше всех, мы сами выполняем обычную настройку такого моба и отменяем событие: подписчики без
 * receiveCanceled (в том числе Bumblezone) его не получают. Обычные появления мобов не затрагиваются.
 */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class LateSpawnGuard {
    private LateSpawnGuard() {}

    @SubscribeEvent(priority = EventPriority.HIGHEST)
    public static void onFinalize(FinalizeSpawnEvent e) {
        Mob mob = e.getEntity();
        if (!mob.isAddedToLevel() || e.isCanceled()) return;
        mob.finalizeSpawn(e.getLevel(), e.getDifficulty(), e.getSpawnType(), e.getSpawnData());
        e.setCanceled(true);
    }
}
