package dev.yukito.yukicore.magic;

import dev.yukito.yukicore.YukiCore;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.tick.LevelTickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/** Отложенные действия на сервере: метеоры, метели, ледяные шипы — всё, что срабатывает «через N тиков». */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class Scheduler {
    private static final Map<ServerLevel, List<Task>> TASKS = new WeakHashMap<>();

    private record Task(int[] left, Runnable action) {}

    private Scheduler() {}

    public static void later(ServerLevel level, int ticks, Runnable action) {
        TASKS.computeIfAbsent(level, l -> new ArrayList<>()).add(new Task(new int[]{Math.max(1, ticks)}, action));
    }

    @SubscribeEvent
    public static void onTick(LevelTickEvent.Post e) {
        if (!(e.getLevel() instanceof ServerLevel level)) return;
        List<Task> list = TASKS.get(level);
        if (list == null || list.isEmpty()) return;
        List<Task> due = new ArrayList<>();
        for (Iterator<Task> it = list.iterator(); it.hasNext(); ) {
            Task t = it.next();
            if (--t.left()[0] <= 0) {
                due.add(t);
                it.remove();
            }
        }
        for (Task t : due) {
            try {
                t.action().run();
            } catch (Exception ex) {
                YukiCore.LOGGER.error("YukiCore scheduled task failed", ex);
            }
        }
    }
}
