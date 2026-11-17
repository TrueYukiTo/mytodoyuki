package dev.yukito.yukicore.client.magic;

import com.mojang.blaze3d.platform.InputConstants;
import dev.yukito.yukicore.magic.Magic;
import dev.yukito.yukicore.magic.MagicData;
import dev.yukito.yukicore.magic.School;
import dev.yukito.yukicore.magic.Skill;
import dev.yukito.yukicore.magic.net.MagicNet;
import net.minecraft.ChatFormatting;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.util.RandomSource;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import org.lwjgl.glfw.GLFW;

/** Клавиши магии (R — сотворить, Z — следующая ячейка, Shift+Z — предыдущая, K — Гримуар) и тряска камеры. */
public final class ClientMagic {
    public static final String CATEGORY = "key.categories.yukicore";
    public static final KeyMapping CAST = new KeyMapping("key.yukicore.cast", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, CATEGORY);
    public static final KeyMapping NEXT = new KeyMapping("key.yukicore.cycle_spell", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_UNKNOWN, CATEGORY);
    public static final KeyMapping GRIMOIRE = new KeyMapping("key.yukicore.grimoire", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_K, CATEGORY);

    private static float shake;
    private static int shakeTicks, shakeTotal;
    private static final RandomSource RNG = RandomSource.create();

    private ClientMagic() {}

    public static void keys(RegisterKeyMappingsEvent e) {
        e.register(CAST);
        e.register(NEXT);
        e.register(GRIMOIRE);
        MagicNet.shakeHandler = (s, t) -> {
            if (s >= shake * shakeTicks / Math.max(1f, shakeTotal)) {
                shake = Math.min(4f, s);
                shakeTicks = shakeTotal = Math.max(1, t);
            }
        };
    }

    public static void tick(ClientTickEvent.Post e) {
        if (shakeTicks > 0) shakeTicks--;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.screen != null) return;
        // 7.3: Shift+R — следующее заклинание (своей клавиши по умолчанию нет: Z, Y и прочие заняты другими модами)
        while (CAST.consumeClick()) {
            if (net.minecraft.client.gui.screens.Screen.hasShiftDown()) cycle(mc, 1);
            else PacketDistributor.sendToServer(new MagicNet.Cast(-1));
        }
        while (NEXT.consumeClick()) cycle(mc, net.minecraft.client.gui.screens.Screen.hasShiftDown() ? -1 : 1);
        while (GRIMOIRE.consumeClick()) openGrimoire();
    }

    private static void cycle(Minecraft mc, int dir) {
        {
            MagicData d = Magic.get(mc.player);
            int slot = d.activeSlot;
            for (int i = 0; i < MagicData.SLOTS; i++) {
                slot = Math.floorMod(slot + dir, MagicData.SLOTS);
                if (d.slotSkill(slot) != null) break;
            }
            d.activeSlot = slot;
            PacketDistributor.sendToServer(new MagicNet.Assign(slot, -1));
            Skill s = d.slotSkill(slot);
            if (s != null) {
                mc.player.displayClientMessage(Component.translatable("spell.yukicore." + s.id())
                        .withStyle(s.school == School.ICE ? ChatFormatting.AQUA : ChatFormatting.GOLD), true);
            }
        }
    }

    public static void camera(ViewportEvent.ComputeCameraAngles e) {
        if (shakeTicks <= 0) return;
        float k = shake * (shakeTicks - (float) e.getPartialTick()) / shakeTotal;
        e.setPitch(e.getPitch() + (RNG.nextFloat() - 0.5f) * k * 2);
        e.setYaw(e.getYaw() + (RNG.nextFloat() - 0.5f) * k * 2);
        e.setRoll(e.getRoll() + (RNG.nextFloat() - 0.5f) * k * 1.5f);
    }

    public static void openGrimoire() {
        Minecraft.getInstance().setScreen(new GrimoireScreen());
    }
}
