package dev.yukito.yukicore.client;

import dev.yukito.yukicore.gear.GearItems;
import dev.yukito.yukicore.gear.Trinkets;
import dev.yukito.yukicore.magic.net.MagicNet;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

/** Клиент 7.4: двойной прыжок кольца облаков, рывок ботинок (двойное нажатие направления), анимация лука. */
public final class GearClient {
    private GearClient() {}

    private static boolean jumpWasDown, airJumpUsed;
    private static final int[] lastTap = new int[4];
    private static final boolean[] wasDown = new boolean[4];
    private static int ticks, dashCooldown;

    public static void tick(ClientTickEvent.Post e) {
        Minecraft mc = Minecraft.getInstance();
        LocalPlayer p = mc.player;
        if (p == null || mc.screen != null) return;
        ticks++;
        if (dashCooldown > 0) dashCooldown--;

        // двойной прыжок
        boolean jump = mc.options.keyJump.isDown();
        if (p.onGround() || p.isInWater() || p.onClimbable()) airJumpUsed = false;
        else if (jump && !jumpWasDown && !airJumpUsed && !p.getAbilities().flying && p.getVehicle() == null
                && Trinkets.has(p, Trinkets.Kind.DOUBLE_JUMP)) {
            airJumpUsed = true;
            Vec3 v = p.getDeltaMovement();
            p.setDeltaMovement(v.x, 0.58, v.z);
            p.resetFallDistance();
            PacketDistributor.sendToServer(new MagicNet.Ability(0));
        }
        jumpWasDown = jump;

        // рывок: двойное нажатие вперёд/назад/влево/вправо
        boolean[] down = {mc.options.keyUp.isDown(), mc.options.keyDown.isDown(), mc.options.keyLeft.isDown(), mc.options.keyRight.isDown()};
        for (int i = 0; i < 4; i++) {
            if (down[i] && !wasDown[i]) {
                if (ticks - lastTap[i] <= 6 && dashCooldown == 0 && p.getVehicle() == null && Trinkets.wearing(p, false)) dash(p, i);
                lastTap[i] = ticks;
            }
            wasDown[i] = down[i];
        }
    }

    private static void dash(LocalPlayer p, int dir) {
        Vec3 f = Vec3.directionFromRotation(0, p.getYRot());
        Vec3 side = new Vec3(-f.z, 0, f.x);
        Vec3 d = switch (dir) {
            case 0 -> f;
            case 1 -> f.scale(-1);
            case 2 -> side.scale(-1);
            default -> side;
        };
        p.setDeltaMovement(d.x * 1.5, Math.max(0.15, p.getDeltaMovement().y), d.z * 1.5);
        dashCooldown = 20;
        PacketDistributor.sendToServer(new MagicNet.Ability(1));
    }

    /** Анимация натяжения ледяного лука (как у ванильного). */
    public static void bowProperties() {
        ItemProperties.register(GearItems.YUKI_BOW.get(), ResourceLocation.withDefaultNamespace("pull"), (st, lvl, ent, seed) ->
                ent == null || ent.getUseItem() != st ? 0f : (st.getUseDuration(ent) - ent.getUseItemRemainingTicks()) / 20f);
        ItemProperties.register(GearItems.YUKI_BOW.get(), ResourceLocation.withDefaultNamespace("pulling"), (st, lvl, ent, seed) ->
                ent != null && ent.isUsingItem() && ent.getUseItem() == st ? 1f : 0f);
    }
}
