package dev.yukito.yukicore.client.boss;

import dev.yukito.yukicore.entity.boss.*;
import net.minecraft.client.model.geom.ModelPart;

import static dev.yukito.yukicore.entity.boss.YukiBoss.*;

/** Анимации боссов: ходьба/полёт, дыхание и отдельная поза для каждой атаки. */
public final class BossModels {
    private BossModels() {}

    // ======================================================================= Ледяной Колосс
    public static class Colossus extends BossModel<FrostColossus> {
        public Colossus(ModelPart root) {
            super(root, "frost_colossus");
        }

        @Override
        protected void animate(FrostColossus e, float ls, float la, float age, float yaw, float pitch, int anim, float t) {
            walk("leg_r", "leg_l", "arm_r", "arm_l", ls, la, 0.9f);
            ModelPart torso = p("torso"), head = p("head"), ar = p("arm_r"), al = p("arm_l"), hips = p("hips");
            torso.xRot += 0.08f + sin(age * 0.06f) * 0.03f;
            torso.zRot += sin(ls * 0.5f) * 0.05f * la;
            p("forearm_r").xRot -= 0.25f;
            p("forearm_l").xRot -= 0.25f;
            ar.zRot += 0.12f + sin(age * 0.06f) * 0.03f;
            al.zRot -= 0.12f + sin(age * 0.06f) * 0.03f;
            p("crystal_c").yRot += age * 0.02f;
            look(head, yaw, pitch, 0.8f);
            switch (anim) {
                case A_SWING -> {
                    float[] s = swing(t, 0, 8, 12, 22);
                    ar.xRot += -2.2f * s[0] + 0.7f * s[1];
                    ar.yRot += 0.6f * s[0] - 0.8f * s[1];
                    torso.yRot += 0.45f * s[0] - 0.5f * s[1];
                }
                case A_SLAM -> {
                    float up = ramp(t, 0, 16) * (1 - ramp(t, 18, 22)), hit = ramp(t, 18, 22) * (1 - ramp(t, 30, 40));
                    ar.xRot += -2.9f * up + 0.4f * hit;
                    al.xRot += -2.9f * up + 0.4f * hit;
                    torso.xRot += -0.25f * up + 0.55f * hit;
                    hips.y += 3 * hit;
                    head.xRot += -0.4f * up;
                }
                case A_GROUND -> {
                    float d = ramp(t, 0, 12) * (1 - ramp(t, 32, 44));
                    ar.xRot -= 1.3f * d;
                    al.xRot -= 1.3f * d;
                    torso.xRot += 0.7f * d;
                    hips.y += 2 * d;
                }
                case A_THROW -> {
                    float[] s = swing(t, 0, 14, 19, 30);
                    ar.xRot += -3.0f * s[0] - 1.0f * s[1];
                    torso.yRot += 0.4f * s[0] - 0.3f * s[1];
                    torso.xRot += -0.2f * s[0] + 0.3f * s[1];
                }
                case A_CAST_UP -> {
                    float u = ramp(t, 0, 10) * (1 - ramp(t, 48, 60));
                    ar.xRot -= 2.7f * u;
                    al.xRot -= 2.7f * u;
                    ar.zRot += 0.6f * u;
                    al.zRot -= 0.6f * u;
                    head.xRot -= 0.5f * u;
                }
                case A_ROAR -> roar(t, torso, head, ar, al);
                default -> {
                }
            }
        }

        private void roar(float t, ModelPart torso, ModelPart head, ModelPart ar, ModelPart al) {
            float u = ramp(t, 0, 8) * (1 - ramp(t, 42, 50));
            head.xRot -= 0.7f * u;
            torso.xRot -= 0.3f * u;
            ar.zRot += 1.1f * u;
            al.zRot -= 1.1f * u;
            torso.zRot += sin(t * 1.7f) * 0.04f * u;
        }
    }

    // ======================================================================= Королева Метелей
    public static class Queen extends BossModel<BlizzardQueen> {
        public Queen(ModelPart root) {
            super(root, "blizzard_queen");
        }

        @Override
        protected void animate(BlizzardQueen e, float ls, float la, float age, float yaw, float pitch, int anim, float t) {
            ModelPart body = p("body"), head = p("head"), ar = p("arm_r"), al = p("arm_l"), staff = p("staff");
            body.y += sin(age * 0.1f) * 1.2f;
            p("skirt").xRot += sin(age * 0.08f) * 0.05f + la * 0.2f;
            p("skirt2").xRot += sin(age * 0.08f + 0.7f) * 0.07f;
            p("skirt3").xRot += sin(age * 0.08f + 1.4f) * 0.09f;
            p("cape").xRot += 0.15f + sin(age * 0.1f) * 0.06f + la * 0.4f;
            ar.xRot -= 0.5f;
            al.zRot -= 0.15f + sin(age * 0.07f) * 0.06f;
            look(head, yaw, pitch, 0.9f);
            for (int i = 0; i < 4; i++) {
                ModelPart s = p("shard_" + i);
                float a = age * 0.08f + i * PI / 2;
                s.x = cos(a) * 29;
                s.z = sin(a) * 29;
                s.y = sin(age * 0.1f + i) * 2;
                s.yRot = -a;
                s.zRot = 0.3f;
            }
            switch (anim) {
                case A_CAST -> {
                    float u = pulse(t, 0, 10, 24);
                    al.xRot -= 1.6f * u;
                    al.zRot += 0.15f * u;
                    ar.xRot -= 0.4f * u;
                }
                case A_CAST_UP -> {
                    float u = ramp(t, 0, 10) * (1 - ramp(t, 40, 50));
                    ar.xRot -= 2.3f * u;
                    al.xRot -= 2.6f * u;
                    al.zRot -= 0.3f * u;
                    head.xRot -= 0.5f * u;
                }
                case A_SPIN -> {
                    body.yRot += t * 0.7f;
                    ar.zRot += 1.2f;
                    al.zRot -= 1.2f;
                }
                case A_ROAR -> {
                    float u = ramp(t, 0, 8) * (1 - ramp(t, 42, 50));
                    head.xRot -= 0.6f * u;
                    ar.zRot += 1.3f * u;
                    al.zRot -= 1.3f * u;
                }
                default -> {
                }
            }
            staff.xRot -= ar.xRot; // посох всегда смотрит вверх
        }
    }

    // ======================================================================= Повелитель Пепла
    public static class Ash extends BossModel<AshLord> {
        public Ash(ModelPart root) {
            super(root, "ash_lord");
        }

        @Override
        protected void animate(AshLord e, float ls, float la, float age, float yaw, float pitch, int anim, float t) {
            walk("leg_r", "leg_l", "arm_r", "arm_l", ls, la, 0.8f);
            ModelPart body = p("body"), head = p("head"), ar = p("arm_r"), al = p("arm_l");
            body.xRot += 0.1f + sin(age * 0.07f) * 0.03f;
            p("forearm_r").xRot -= 0.3f;
            p("forearm_l").xRot -= 0.3f;
            p("flame_crown").yScale = 1 + sin(age * 0.5f) * 0.18f;
            p("tail").yRot += sin(age * 0.1f) * 0.3f;
            p("tail2").yRot += sin(age * 0.1f + 0.8f) * 0.4f;
            look(head, yaw, pitch, 0.8f);
            float ringR = 22;
            if (anim == A_ROAR) ringR += 12 * ramp(t, 0, 20);
            for (int i = 0; i < 6; i++) {
                ModelPart r = p("rock_" + i);
                float a = age * 0.05f + i * PI / 3;
                r.x = cos(a) * ringR;
                r.z = sin(a) * ringR;
                r.y = -6 + sin(age * 0.1f + i) * 3;
                r.xRot = age * 0.1f + i;
                r.yRot = age * 0.07f;
            }
            switch (anim) {
                case A_SWING -> {
                    float[] s = swing(t, 0, 8, 12, 22);
                    ar.xRot += -2.4f * s[0] + 0.6f * s[1];
                    ar.yRot += 0.5f * s[0] - 0.9f * s[1];
                    body.yRot += 0.4f * s[0] - 0.5f * s[1];
                }
                case A_CAST -> {
                    float k = (t % 5) / 5f;
                    float u = ramp(t, 4, 10) * (1 - ramp(t, 24, 30));
                    ar.xRot -= (1.5f + 0.3f * k) * u;
                    al.xRot -= (1.2f + 0.3f * (1 - k)) * u;
                }
                case A_CAST_UP -> {
                    float u = ramp(t, 0, 10) * (1 - ramp(t, 48, 60));
                    ar.xRot -= 2.8f * u;
                    al.xRot -= 2.8f * u;
                    head.xRot -= 0.5f * u;
                }
                case A_SLAM -> {
                    float up = ramp(t, 0, 16) * (1 - ramp(t, 18, 22)), hit = ramp(t, 18, 22) * (1 - ramp(t, 30, 40));
                    ar.xRot += -2.9f * up + 0.5f * hit;
                    al.xRot += -2.9f * up + 0.5f * hit;
                    body.xRot += 0.6f * hit;
                }
                case A_DASH -> {
                    float u = ramp(t, 0, 8) * (1 - ramp(t, 22, 30));
                    body.xRot += 0.7f * u;
                    ar.xRot += 1.0f * u;
                    al.xRot += 1.0f * u;
                }
                case A_GROUND -> {
                    float d = ramp(t, 0, 10) * (1 - ramp(t, 40, 50));
                    ar.xRot -= 1.4f * d;
                    al.xRot -= 1.4f * d;
                    body.xRot += 0.8f * d;
                }
                case A_ROAR -> {
                    float u = ramp(t, 0, 8) * (1 - ramp(t, 70, 80));
                    head.xRot -= 0.7f * u;
                    ar.zRot += 1.2f * u;
                    al.zRot -= 1.2f * u;
                    body.zRot += sin(t * 1.9f) * 0.03f * u;
                }
                default -> {
                }
            }
        }
    }

    // ======================================================================= Пустотный Змей
    public static class Wyrm extends BossModel<VoidWyrm> {
        public Wyrm(ModelPart root) {
            super(root, "void_wyrm");
        }

        @Override
        protected void animate(VoidWyrm e, float ls, float la, float age, float yaw, float pitch, int anim, float t) {
            float amp = anim == A_SPIN ? 0.6f * pulse(t, 0, 15, 30) + 0.25f : 0.25f;
            for (int i = 2; i <= 7; i++) {
                ModelPart s = p("seg" + i);
                s.yRot += sin(age * 0.15f - i * 0.6f) * amp;
                s.xRot += sin(age * 0.1f - i * 0.5f) * 0.1f;
            }
            p("tail").yRot += sin(age * 0.15f - 4.8f) * 0.35f;
            p("seg1").yRot += sin(age * 0.15f) * 0.1f;
            ModelPart head = p("head"), jaw = p("jaw");
            head.yRot += sin(age * 0.15f + 0.6f) * 0.12f;
            look(head, yaw, pitch, 0.5f);
            float f = sin(age * 0.25f);
            p("wing_r").zRot += f * 0.6f;
            p("wing_r2").zRot += f * 0.4f;
            p("wing_l").zRot -= f * 0.6f;
            p("wing_l2").zRot -= f * 0.4f;
            jaw.xRot += 0.1f + sin(age * 0.05f) * 0.05f;
            switch (anim) {
                case A_BITE -> {
                    jaw.xRot += 0.9f * pulse(t, 8, 14, 30);
                    head.xRot -= 0.3f * ramp(t, 0, 12) * (1 - ramp(t, 12, 16));
                    p("wing_r").zRot += -0.8f * ramp(t, 10, 14) * (1 - ramp(t, 30, 40));
                    p("wing_l").zRot += 0.8f * ramp(t, 10, 14) * (1 - ramp(t, 30, 40));
                }
                case A_BREATH -> {
                    float u = ramp(t, 8, 15) * (1 - ramp(t, 45, 50));
                    jaw.xRot += 1.0f * u;
                    head.xRot += 0.15f * u;
                }
                case A_CAST, A_CAST_UP -> {
                    float u = pulse(t, 0, 12, 30);
                    jaw.xRot += 0.6f * u;
                    head.xRot -= 0.4f * u;
                }
                case A_ROAR -> {
                    float u = ramp(t, 0, 8) * (1 - ramp(t, 32, 40));
                    jaw.xRot += 1.1f * u;
                    head.xRot -= 0.6f * u;
                }
                default -> {
                }
            }
        }

        @Override
        protected void death(VoidWyrm e, float d) {
            super.death(e, d);
            for (int i = 2; i <= 7; i++) p("seg" + i).yRot += d * 0.5f;
        }
    }

    // ======================================================================= Тень Юки
    public static class Shadow extends BossModel<ShadowOfYuki> {
        public Shadow(ModelPart root) {
            super(root, "shadow_of_yuki");
        }

        @Override
        protected void animate(ShadowOfYuki e, float ls, float la, float age, float yaw, float pitch, int anim, float t) {
            ModelPart body = p("body"), head = p("head"), ar = p("arm_r"), al = p("arm_l");
            body.y += sin(age * 0.1f) * 1.2f;
            p("halo").zRot += age * 0.03f + 0.785f;
            p("cloak").xRot += 0.12f + sin(age * 0.09f) * 0.06f + la * 0.5f;
            p("robe").xRot += sin(age * 0.08f) * 0.05f;
            p("robe2").xRot += sin(age * 0.08f + 0.8f) * 0.08f;
            ar.zRot += 0.1f + sin(age * 0.07f) * 0.05f;
            al.zRot -= 0.1f + sin(age * 0.07f) * 0.05f;
            look(head, yaw, pitch, 0.9f);
            float rise = anim == A_CAST_UP ? ramp(t, 0, 12) * (1 - ramp(t, 40, 50)) : 0;
            float front = anim == A_DASH ? pulse(t, 4, 10, 20) : 0;
            for (int i = 0; i < 4; i++) {
                ModelPart s = p("sword_" + i);
                float a = age * 0.06f + i * PI / 2;
                float r = 18 * (1 - front) + 6 * front;
                s.x = cos(a) * r * (1 - front) + (i - 1.5f) * 5 * front;
                s.z = sin(a) * r * (1 - front) - 14 * front;
                s.y = -2 + sin(age * 0.1f + i) * 2 - 22 * rise;
                s.yRot = -a * (1 - front);
                s.zRot = 0.25f * (1 - rise);
                s.xRot = PI * rise + 1.4f * front;
            }
            switch (anim) {
                case A_CAST -> {
                    float u = pulse(t, 0, 8, 36);
                    ar.xRot -= 1.6f * u;
                    al.xRot -= 1.6f * u * (sin(t * 0.5f) > 0 ? 1 : 0.6f);
                }
                case A_CAST_UP -> {
                    ar.xRot -= 2.8f * rise;
                    al.xRot -= 2.8f * rise;
                    head.xRot -= 0.4f * rise;
                }
                case A_DASH -> {
                    float[] s = swing(t, 0, 8, 12, 24);
                    ar.xRot += -2.4f * s[0] + 0.5f * s[1];
                    body.yRot += 0.5f * s[0] - 0.6f * s[1];
                }
                case A_SPIN -> body.yRot += t * 0.8f;
                case A_SLAM -> {
                    float up = ramp(t, 0, 14) * (1 - ramp(t, 16, 20)), hit = ramp(t, 16, 20) * (1 - ramp(t, 30, 40));
                    ar.xRot += -2.8f * up + 0.4f * hit;
                    al.xRot += -2.8f * up + 0.4f * hit;
                }
                case A_ROAR -> {
                    float u = ramp(t, 0, 8) * (1 - ramp(t, 32, 40));
                    head.xRot -= 0.6f * u;
                    ar.zRot += 1.4f * u;
                    al.zRot -= 1.4f * u;
                }
                default -> {
                }
            }
        }
    }

    // ======================================================================= Ледяной Архонт
    public static class Archon extends BossModel<GlacialArchon> {
        public Archon(ModelPart root) {
            super(root, "glacial_archon");
        }

        @Override
        protected void animate(GlacialArchon e, float ls, float la, float age, float yaw, float pitch, int anim, float t) {
            walk("leg_r", "leg_l", null, null, ls, la, 0.9f);
            ModelPart body = p("body"), head = p("head"), ar = p("arm_r"), al = p("arm_l"), sword = p("sword");
            body.xRot += 0.04f + sin(age * 0.06f) * 0.02f;
            ar.xRot -= 0.3f + cos(ls * 0.5f) * 0.2f * la;
            sword.xRot += 0.8f;
            al.xRot -= 0.6f;
            al.yRot -= 0.3f;
            p("cape").xRot += 0.1f + la * 0.6f + sin(age * 0.08f) * 0.04f;
            look(head, yaw, pitch, 0.9f);
            switch (anim) {
                case A_SWING -> {
                    float[] s = swing(t, 0, 7, 11, 20);
                    ar.xRot += -2.6f * s[0] + 1.2f * s[1];
                    ar.yRot += 0.3f * s[0] - 0.6f * s[1];
                    sword.xRot -= 0.8f * s[0];
                    body.yRot += 0.4f * s[0] - 0.5f * s[1];
                }
                case A_SLAM -> {
                    float up = ramp(t, 0, 12) * (1 - ramp(t, 13, 17)), hit = ramp(t, 13, 17) * (1 - ramp(t, 24, 34));
                    ar.xRot += -2.9f * up + 0.9f * hit;
                    al.xRot += -2.4f * up + 0.6f * hit;
                    sword.xRot += -0.6f * up + 0.4f * hit;
                    body.xRot += -0.2f * up + 0.5f * hit;
                }
                case A_DASH -> {
                    float u = ramp(t, 0, 6) * (1 - ramp(t, 18, 26));
                    body.xRot += 0.4f * u;
                    al.xRot -= 1.0f * u;
                    al.yRot += 0.3f * u;
                    ar.xRot += 0.6f * u;
                }
                case A_GROUND -> {
                    float d = ramp(t, 0, 10) * (1 - ramp(t, 30, 40));
                    ar.xRot -= 0.6f * d;
                    sword.xRot += 0.8f * d;
                    body.xRot += 0.45f * d;
                }
                case A_CAST -> {
                    float u = pulse(t, 0, 12, 30);
                    al.xRot -= 1.0f * u;
                    head.xRot -= 0.2f * u;
                }
                case A_CAST_UP -> {
                    float u = ramp(t, 0, 10) * (1 - ramp(t, 50, 60));
                    ar.xRot -= 3.0f * u;
                    sword.xRot -= 0.8f * u;
                    head.xRot -= 0.4f * u;
                }
                case A_ROAR -> {
                    float u = ramp(t, 0, 8) * (1 - ramp(t, 42, 50));
                    head.xRot -= 0.6f * u;
                    ar.zRot += 1.0f * u;
                    al.zRot -= 1.0f * u;
                }
                default -> {
                }
            }
        }
    }

    // ======================================================================= Пламенный Феникс
    public static class Phoenix extends BossModel<InfernalPhoenix> {
        public Phoenix(ModelPart root) {
            super(root, "infernal_phoenix");
        }

        @Override
        protected void animate(InfernalPhoenix e, float ls, float la, float age, float yaw, float pitch, int anim, float t) {
            ModelPart body = p("body"), head = p("head"), wr = p("wing_r"), wl = p("wing_l");
            float speed = anim == A_ROAR ? 0.6f : 0.3f;
            float f = sin(age * speed);
            wr.zRot += f * 0.7f;
            p("wing_r2").zRot += f * 0.5f + 0.1f;
            wl.zRot -= f * 0.7f;
            p("wing_l2").zRot -= f * 0.5f + 0.1f;
            body.y -= f * 1.5f;
            p("tail").xRot += sin(age * 0.1f) * 0.1f;
            p("tail_r").yRot += sin(age * 0.12f) * 0.06f;
            p("tail_l").yRot -= sin(age * 0.12f) * 0.06f;
            p("crest").xRot += sin(age * 0.2f) * 0.1f;
            look(head, yaw, pitch, 0.6f);
            switch (anim) {
                case A_DASH -> {
                    float u = ramp(t, 8, 12) * (1 - ramp(t, 30, 40));
                    body.xRot += 0.5f * u;
                    wr.yRot += 1.1f * u;
                    wl.yRot -= 1.1f * u;
                    wr.zRot -= f * 0.6f * u;
                    wl.zRot += f * 0.6f * u;
                }
                case A_CAST -> {
                    float u = pulse(t, 0, 12, 24);
                    wr.yRot -= 0.6f * u;
                    wl.yRot += 0.6f * u;
                    head.xRot -= 0.2f * u;
                }
                case A_SPIN -> body.yRot += t * 0.5f;
                case A_CAST_UP -> {
                    float u = ramp(t, 0, 10) * (1 - ramp(t, 50, 60));
                    wr.zRot -= 0.9f * u;
                    wl.zRot += 0.9f * u;
                    head.xRot -= 0.5f * u;
                }
                case A_ROAR -> head.xRot -= 0.6f * pulse(t, 0, 10, 30);
                default -> {
                }
            }
        }
    }
}
