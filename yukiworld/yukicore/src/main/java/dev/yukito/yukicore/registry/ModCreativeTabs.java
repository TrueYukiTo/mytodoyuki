package dev.yukito.yukicore.registry;

import dev.yukito.yukicore.YukiCore;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public final class ModCreativeTabs {
    public static final DeferredRegister<CreativeModeTab> TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, YukiCore.MOD_ID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> YUKIWORLD = TABS.register("yukiworld",
            () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.yukicore"))
                    .icon(() -> new ItemStack(ModItems.YUKI_STAR.get()))
                    .displayItems((params, output) ->
                            ModItems.ITEMS.getEntries().forEach(e -> output.accept(e.get())))
                    .build());

    private ModCreativeTabs() {}
}
