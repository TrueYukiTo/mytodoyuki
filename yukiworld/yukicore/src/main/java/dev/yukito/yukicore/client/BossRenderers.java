package dev.yukito.yukicore.client;

import dev.yukito.yukicore.YukiCore;
import dev.yukito.yukicore.entity.*;
import net.minecraft.client.renderer.entity.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.monster.Blaze;
import net.minecraft.world.entity.monster.Illusioner;
import net.minecraft.world.entity.monster.Phantom;

/** Рендеры боссов: ванильные модели + собственные HD-текстуры (×4). */
public final class BossRenderers {
    private BossRenderers() {}

    static ResourceLocation tex(String name) {
        return YukiCore.id("textures/entity/" + name + ".png");
    }

    public static class Colossus extends IronGolemRenderer {
        public Colossus(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public ResourceLocation getTextureLocation(IronGolem e) {
            return tex("frost_colossus");
        }
    }

    public static class Queen extends SkeletonRenderer<BlizzardQueen> {
        public Queen(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public ResourceLocation getTextureLocation(BlizzardQueen e) {
            return tex("blizzard_queen");
        }
    }

    public static class Ash extends BlazeRenderer {
        public Ash(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public ResourceLocation getTextureLocation(Blaze e) {
            return tex("ash_lord");
        }
    }

    public static class Wyrm extends PhantomRenderer {
        public Wyrm(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public ResourceLocation getTextureLocation(Phantom e) {
            return tex("void_wyrm");
        }
    }

    public static class Shadow extends IllusionerRenderer {
        public Shadow(EntityRendererProvider.Context ctx) {
            super(ctx);
        }

        @Override
        public ResourceLocation getTextureLocation(Illusioner e) {
            return tex("shadow_of_yuki");
        }
    }
}
