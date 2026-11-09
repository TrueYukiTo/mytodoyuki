package dev.yukito.yukicore.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;

import java.util.ArrayList;
import java.util.List;

/** Лёгкий снегопад для главного меню (несколько сотен fill-вызовов, без текстур). */
public final class Snowfall {
    private static final class Flake {
        float x, y, speed, size, phase, drift;
    }

    private final List<Flake> flakes = new ArrayList<>();
    private final RandomSource random = RandomSource.create();
    private int width, height;
    private long lastNanos = System.nanoTime();
    private float time;

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;
        int target = Mth.clamp(width * height / 2200, 40, 220);
        flakes.clear();
        for (int i = 0; i < target; i++) {
            Flake f = new Flake();
            reset(f);
            f.y = random.nextFloat() * height;
            flakes.add(f);
        }
    }

    private void reset(Flake f) {
        f.x = random.nextFloat() * width;
        f.y = -4;
        f.size = 1 + random.nextInt(3);
        f.speed = 8 + f.size * 9 + random.nextFloat() * 10;
        f.phase = random.nextFloat() * Mth.TWO_PI;
        f.drift = 4 + random.nextFloat() * 10;
    }

    public void render(GuiGraphics g) {
        long now = System.nanoTime();
        float dt = Math.min((now - lastNanos) / 1_000_000_000f, 0.1f);
        lastNanos = now;
        time += dt;

        for (Flake f : flakes) {
            f.y += f.speed * dt;
            float x = f.x + Mth.sin(time * 0.8f + f.phase) * f.drift;
            if (f.y > height + 4) reset(f);
            int s = (int) f.size;
            int alpha = 90 + s * 50;
            int px = (int) x, py = (int) f.y;
            g.fill(px, py, px + s, py + s, YukiTheme.withAlpha(YukiTheme.SNOW, alpha));
        }
    }

    public float time() {
        return time;
    }
}
