package dev.yukito.yukicore;

import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.server.ServerStartedEvent;

/** Пишет в лог, сколько рецептов YukiCore загрузилось — удобно проверять сборку. */
@EventBusSubscriber(modid = YukiCore.MOD_ID)
public final class RecipeReport {
    private RecipeReport() {}

    @SubscribeEvent
    public static void onServerStarted(ServerStartedEvent event) {
        var recipes = event.getServer().getRecipeManager().getRecipes().stream()
                .filter(r -> r.id().getNamespace().equals(YukiCore.MOD_ID))
                .map(r -> r.id().getPath()).sorted().toList();
        YukiCore.LOGGER.info("YukiCore: {} recipes loaded: {}", recipes.size(), String.join(", ", recipes));
    }
}
