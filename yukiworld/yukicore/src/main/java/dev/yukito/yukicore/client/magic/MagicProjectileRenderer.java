package dev.yukito.yukicore.client.magic;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.yukito.yukicore.magic.MagicProjectile;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlas;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;

/** Снаряд рисуется как светящийся вращающийся спрайт нужного размера. */
public class MagicProjectileRenderer extends EntityRenderer<MagicProjectile> {
    private final ItemRenderer items;

    public MagicProjectileRenderer(EntityRendererProvider.Context ctx) {
        super(ctx);
        this.items = ctx.getItemRenderer();
    }

    @Override
    public void render(MagicProjectile e, float yaw, float partial, PoseStack pose, MultiBufferSource buf, int light) {
        if (e.tickCount < 2 && entityRenderDispatcher.camera.getEntity().distanceToSqr(e) < 9) return;
        pose.pushPose();
        float s = e.size() * 1.8f;
        pose.scale(s, s, s);
        pose.mulPose(entityRenderDispatcher.cameraOrientation());
        pose.mulPose(Axis.YP.rotationDegrees(180f));
        pose.mulPose(Axis.ZP.rotationDegrees((e.tickCount + partial) * 18f));
        items.renderStatic(e.getItem(), ItemDisplayContext.GROUND, 0xF000F0, OverlayTexture.NO_OVERLAY, pose, buf, e.level(), e.getId());
        pose.popPose();
        super.render(e, yaw, partial, pose, buf, light);
    }

    @Override
    public ResourceLocation getTextureLocation(MagicProjectile e) {
        return TextureAtlas.LOCATION_BLOCKS;
    }
}
