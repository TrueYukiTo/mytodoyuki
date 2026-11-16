package dev.yukito.yukicore.client.boss;

import dev.yukito.yukicore.entity.boss.YukiBoss;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;

import java.util.HashMap;
import java.util.Map;

/** Основа 3D-моделей боссов: части по имени, плавные функции для анимаций, общая анимация смерти. */
public abstract class BossModel<T extends YukiBoss> extends HierarchicalModel<T> {
    protected static final float DEG = Mth.DEG_TO_RAD;
    protected static final float PI = Mth.PI;
    private final ModelPart root;
    private final Map<String, ModelPart> parts = new HashMap<>();

    protected BossModel(ModelPart root, String boss) {
        this.root = root;
        BossLayers.PATHS.get(boss).forEach((name, path) -> {
            ModelPart p = root;
            for (String s : path) p = p.getChild(s);
            parts.put(name, p);
        });
    }

    @Override
    public ModelPart root() {
        return root;
    }

    protected ModelPart p(String name) {
        return parts.get(name);
    }

    @Override
    public final void setupAnim(T e, float limbSwing, float limbAmount, float age, float headYaw, float headPitch) {
        root.getAllParts().forEach(ModelPart::resetPose);
        animate(e, limbSwing, limbAmount, age, headYaw, headPitch, e.anim(), e.animTime(age));
        if (e.deathTime > 0) death(e, (e.deathTime + (age - e.tickCount)) / YukiBoss.DEATH_TICKS);
    }

    protected abstract void animate(T e, float ls, float la, float age, float yaw, float pitch, int anim, float t);

    /** Смерть: босс оседает, клонится и рассыпается. d — от 0 до 1. */
    protected void death(T e, float d) {
        d = Math.min(1, d);
        root.y += d * 10;
        root.zRot += d * 0.35f;
        root.xRot += d * 0.2f;
        float s = 1 - ramp(d, 0.65f, 1f) * 0.85f;
        root.xScale = root.yScale = root.zScale = s;
    }

    // ------------------------------------------------------------------ помощники
    /** Плавный рост от 0 до 1 на отрезке [a, b]. */
    protected static float ramp(float t, float a, float b) {
        float x = Mth.clamp((t - a) / (b - a), 0, 1);
        return x * x * (3 - 2 * x);
    }

    /** 0 → 1 (к пику) → 0 (к концу). */
    protected static float pulse(float t, float a, float peak, float b) {
        return t < peak ? ramp(t, a, peak) : 1 - ramp(t, peak, b);
    }

    /** Замах (a→b) и удар (b→c), затем возврат (c→d): значения от 0 до 1 для замаха и удара. */
    protected static float[] swing(float t, float a, float b, float c, float d) {
        float wind = ramp(t, a, b) * (1 - ramp(t, b, c));
        float strike = ramp(t, b, c) * (1 - ramp(t, c + 2, d));
        return new float[]{wind, strike};
    }

    protected static float sin(float x) {
        return Mth.sin(x);
    }

    protected static float cos(float x) {
        return Mth.cos(x);
    }

    protected void look(ModelPart head, float yaw, float pitch, float k) {
        head.yRot += yaw * DEG * k;
        head.xRot += pitch * DEG * k;
    }

    protected void walk(String legR, String legL, String armR, String armL, float ls, float la, float stride) {
        float w = cos(ls * 0.6662f * 0.75f) * stride * la;
        p(legR).xRot += w;
        p(legL).xRot -= w;
        if (armR != null) p(armR).xRot -= w * 0.7f;
        if (armL != null) p(armL).xRot += w * 0.7f;
    }
}
