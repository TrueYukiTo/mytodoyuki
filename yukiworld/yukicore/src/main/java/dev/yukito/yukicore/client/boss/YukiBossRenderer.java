package dev.yukito.yukicore.client.boss;

import com.mojang.blaze3d.vertex.PoseStack;
import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.entity.boss.YukiBoss;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.resources.ResourceLocation;

/** Рендер боссов: 3D-модель, HD-текстура (×4) и светящийся слой (лёд, лава, глаза, звёзды). */
public class YukiBossRenderer<T extends YukiBoss> extends MobRenderer<T, BossModel<T>> {
    private final ResourceLocation texture;
    private final float scale;

    public YukiBossRenderer(EntityRendererProvider.Context ctx, BossModel<T> model, String name, float scale, float shadow) {
        super(ctx, model, shadow);
        this.texture = YukiCore.id("textures/entity/boss/" + name + ".png");
        this.scale = scale;
        RenderType glow = RenderType.eyes(YukiCore.id("textures/entity/boss/" + name + "_glow.png"));
        addLayer(new EyesLayer<>(this) {
            @Override
            public RenderType renderType() {
                return glow;
            }
        });
    }

    @Override
    protected void scale(T e, PoseStack pose, float partialTick) {
        pose.scale(scale, scale, scale);
    }

    @Override
    protected float getFlipDegrees(T e) {
        return 0; // своя анимация смерти вместо ванильного «падения набок»
    }

    @Override
    public ResourceLocation getTextureLocation(T e) {
        return texture;
    }
}
