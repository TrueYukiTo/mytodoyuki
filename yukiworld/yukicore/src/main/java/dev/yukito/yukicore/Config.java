package dev.yukito.yukicore;

import net.neoforged.neoforge.common.ModConfigSpec;

public final class Config {
    public static final ModConfigSpec CLIENT_SPEC;

    public static final ModConfigSpec.BooleanValue CUSTOM_TITLE_SCREEN;
    public static final ModConfigSpec.BooleanValue SNOWFALL;
    public static final ModConfigSpec.BooleanValue SHOW_HUD;
    public static final ModConfigSpec.BooleanValue HUD_SHOW_BIOME;
    public static final ModConfigSpec.BooleanValue SMART_FPS;
    public static final ModConfigSpec.IntValue UNFOCUSED_FPS;

    static {
        ModConfigSpec.Builder b = new ModConfigSpec.Builder();

        b.push("gui");
        CUSTOM_TITLE_SCREEN = b.comment("Use the YukiWorld main menu instead of the vanilla one")
                .define("customTitleScreen", true);
        SNOWFALL = b.comment("Animated snowfall in the YukiWorld main menu")
                .define("snowfall", true);
        SHOW_HUD = b.comment("Show the compact YukiWorld HUD (FPS / coordinates / biome)")
                .define("showHud", true);
        HUD_SHOW_BIOME = b.comment("Show the current biome in the HUD")
                .define("hudShowBiome", true);
        b.pop();

        b.push("performance");
        SMART_FPS = b.comment("Limit FPS while the game window is not focused (saves CPU/GPU, keeps laptops cool)")
                .define("smartFps", true);
        UNFOCUSED_FPS = b.comment("FPS limit used while the window is not focused")
                .defineInRange("unfocusedFps", 10, 1, 60);
        b.pop();

        CLIENT_SPEC = b.build();
    }

    private Config() {}
}
