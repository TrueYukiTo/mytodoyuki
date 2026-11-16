package dev.yukito.yukicore.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.yukito.yukicore.altar.ItemHolderBlockEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;

/** Предмет парит и вращается над алтарём/пьедесталом. */
public class ItemStandRenderer implements BlockEntityRenderer<ItemHolderBlockEntity> {
    public ItemStandRenderer(BlockEntityRendererProvider.Context ctx) {}

    @Override
    public void render(ItemHolderBlockEntity be, float partialTick, PoseStack pose, MultiBufferSource buffers,
                       int light, int overlay) {
        ItemStack stack = be.getItem();
        if (stack.isEmpty() || be.getLevel() == null) return;
        float time = be.getLevel().getGameTime() + partialTick;
        pose.pushPose();
        pose.translate(0.5, 1.15 + Math.sin(time / 10.0) * 0.06, 0.5);
        pose.mulPose(Axis.YP.rotationDegrees(time * 2.5f));
        pose.scale(0.6f, 0.6f, 0.6f);
        Minecraft.getInstance().getItemRenderer().renderStatic(stack, ItemDisplayContext.FIXED,
                LightTexture.FULL_BRIGHT, overlay, pose, buffers, be.getLevel(), 0);
        pose.popPose();
    }
}
