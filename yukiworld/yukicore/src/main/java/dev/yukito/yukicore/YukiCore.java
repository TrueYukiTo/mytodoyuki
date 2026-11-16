package dev.yukito.yukicore;

import com.mojang.logging.LogUtils;
import dev.yukito.yukicore.registry.ModArmorMaterials;
import dev.yukito.yukicore.registry.ModBlocks;
import dev.yukito.yukicore.registry.ModCreativeTabs;
import dev.yukito.yukicore.registry.ModItems;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import org.slf4j.Logger;

/**
 * YukiCore — фирменный мод сборки YukiWorld.
 * Добавляет ледяные руды, инструменты, броню, Звезду Юки,
 * собственный главный экран, HUD и оптимизацию FPS.
 */
@Mod(YukiCore.MOD_ID)
public final class YukiCore {
    public static final String MOD_ID = "yukicore";
    public static final Logger LOGGER = LogUtils.getLogger();

    public YukiCore(IEventBus modBus, ModContainer container) {
        // боссы с 10 000+ здоровья: поднимаем ванильный потолок атрибута (1024)
        ((net.minecraft.world.entity.ai.attributes.RangedAttribute) net.minecraft.world.entity.ai.attributes.Attributes.MAX_HEALTH.value()).maxValue = 1_000_000;
        ModArmorMaterials.ARMOR_MATERIALS.register(modBus);
        ModBlocks.BLOCKS.register(modBus);
        ModItems.ITEMS.register(modBus);
        ModCreativeTabs.TABS.register(modBus);
        dev.yukito.yukicore.registry.ModBlockEntities.BLOCK_ENTITIES.register(modBus);
        modBus.addListener(dev.yukito.yukicore.registry.ModBlockEntities::capabilities);
        dev.yukito.yukicore.registry.ModRecipes.TYPES.register(modBus);
        dev.yukito.yukicore.registry.ModRecipes.SERIALIZERS.register(modBus);
        dev.yukito.yukicore.registry.ModEntities.register(modBus);
        dev.yukito.yukicore.registry.ModMagic.register(modBus);
        if (net.neoforged.fml.ModList.get().isLoaded("ad_astra")) {
            dev.yukito.yukicore.compat.adastra.AdAstraCompat.register(modBus);
        }

        container.registerConfig(ModConfig.Type.CLIENT, Config.CLIENT_SPEC);
        LOGGER.info("YukiCore loaded — welcome to YukiWorld ❄");
    }

    public static ResourceLocation id(String path) {
        return ResourceLocation.fromNamespaceAndPath(MOD_ID, path);
    }
}
