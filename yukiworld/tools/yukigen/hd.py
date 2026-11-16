"""
HD-конвейер текстур YukiCore 3.0 (64×64 для предметов и блоков).

Предметы: 16×16 маска → апскейл EPX/Scale2x ×2 ×2 (гладкие диагонали) → объёмное освещение по карте
расстояний до края (фаска), тёмная обводка, блики, лёгкий шум.
Блоки: рисуются процедурно сразу в 64×64.
"""
import math
import random

from PIL import Image, ImageDraw, ImageFilter

from .pixel import hexrgb

S = 64


# ---------------------------------------------------------------- апскейл и свет
def scale2x(img: Image.Image) -> Image.Image:
    w, h = img.size
    src = img.load()
    out = Image.new("RGBA", (w * 2, h * 2))
    dst = out.load()

    def p(x, y):
        x = min(max(x, 0), w - 1)
        y = min(max(y, 0), h - 1)
        return src[x, y]

    for y in range(h):
        for x in range(w):
            P, A, B, C, D = p(x, y), p(x, y - 1), p(x + 1, y), p(x - 1, y), p(x, y + 1)
            e0 = e1 = e2 = e3 = P
            if C == A and C != D and A != B:
                e0 = A
            if A == B and A != C and B != D:
                e1 = B
            if D == C and D != B and C != A:
                e2 = C
            if B == D and B != A and D != C:
                e3 = D
            dst[2 * x, 2 * y], dst[2 * x + 1, 2 * y] = e0, e1
            dst[2 * x, 2 * y + 1], dst[2 * x + 1, 2 * y + 1] = e2, e3
    return out


def distance_map(img: Image.Image, cap=8):
    w, h = img.size
    a = img.load()
    INF = 99
    d = [[0 if a[x, y][3] == 0 else INF for x in range(w)] for y in range(h)]
    # граница холста тоже считается краем
    for _ in range(cap):
        changed = False
        for y in range(h):
            for x in range(w):
                if d[y][x] == 0:
                    continue
                m = d[y][x]
                for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                    nx, ny = x + dx, y + dy
                    nv = 0 if not (0 <= nx < w and 0 <= ny < h) else d[ny][nx]
                    if nv + 1 < m:
                        m = nv + 1
                if m != d[y][x]:
                    d[y][x] = m
                    changed = True
        if not changed:
            break
    return [[min(v, cap) for v in row] for row in d]


def shade(img: Image.Image, seed=0, outline=0.45, gloss=0.55, strength=0.32) -> Image.Image:
    rng = random.Random(seed)
    w, h = img.size
    dm = distance_map(img)
    src = img.load()
    out = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    dst = out.load()
    for y in range(h):
        for x in range(w):
            r, g, b, a = src[x, y]
            if a == 0:
                continue
            dv = dm[y][x]

            def hgt(xx, yy):
                if 0 <= xx < w and 0 <= yy < h:
                    return dm[yy][xx]
                return 0
            gx = hgt(x + 1, y) - hgt(x - 1, y)
            gy = hgt(x, y + 1) - hgt(x, y - 1)
            light = (-gx - gy) * 0.5  # свет сверху-слева
            k = 1.0 + strength * max(-1.0, min(1.0, light)) + rng.uniform(-0.035, 0.035)
            if dv == 1:
                k *= outline + 0.15
            elif dv == 2:
                k *= 0.85 + 0.1 * max(0.0, light)
            r, g, b = (min(255, int(c * k)) for c in (r, g, b))
            if 2 <= dv <= 3 and light > 0.6 and rng.random() < gloss:
                r, g, b = (int(c + (255 - c) * 0.55) for c in (r, g, b))
            dst[x, y] = (r, g, b, a)
    return out


def hd_item(img16: Image.Image, seed=0, sparkles=0, sparkle_color=0xFFFFFF) -> Image.Image:
    big = scale2x(scale2x(img16.convert("RGBA")))
    big = shade(big, seed)
    if sparkles:
        big = add_sparkles(big, sparkles, sparkle_color, seed)
    return big


def add_sparkles(img, n, color, seed):
    rng = random.Random(seed * 7 + 3)
    d = ImageDraw.Draw(img)
    px = img.load()
    pts = [(x, y) for y in range(4, S - 4) for x in range(4, S - 4) if px[x, y][3] > 0]
    for x, y in rng.sample(pts, min(n, len(pts))):
        c = hexrgb(color)
        d.line([x - 2, y, x + 2, y], fill=c)
        d.line([x, y - 2, x, y + 2], fill=c)
        d.point((x, y), fill=(255, 255, 255, 255))
    return img


# ---------------------------------------------------------------- шум
def value_noise(seed, scale=8, octaves=3):
    rng = random.Random(seed)
    acc = [[0.0] * S for _ in range(S)]
    amp, total = 1.0, 0.0
    for o in range(octaves):
        cells = max(2, S // (scale >> o if scale >> o else 1))
        grid = [[rng.random() for _ in range(cells + 1)] for _ in range(cells + 1)]
        step = S / cells
        for y in range(S):
            for x in range(S):
                gx, gy = x / step, y / step
                x0, y0 = int(gx) % cells, int(gy) % cells
                fx, fy = gx - int(gx), gy - int(gy)
                fx, fy = fx * fx * (3 - 2 * fx), fy * fy * (3 - 2 * fy)
                x1, y1 = (x0 + 1) % cells, (y0 + 1) % cells  # бесшовно
                v = (grid[y0][x0] * (1 - fx) + grid[y0][x1] * fx) * (1 - fy) + \
                    (grid[y1][x0] * (1 - fx) + grid[y1][x1] * fx) * fy
                acc[y][x] += v * amp
        total += amp
        amp *= 0.5
    return [[v / total for v in row] for row in acc]


def tint(base, k):
    return tuple(max(0, min(255, int(c * k))) for c in base[:3]) + (255,)


def stone(seed, base, var=0.22, cracks=0, streaks=False):
    n = value_noise(seed, 16, 4)
    img = Image.new("RGBA", (S, S))
    px = img.load()
    rng = random.Random(seed)
    for y in range(S):
        for x in range(S):
            k = 1 + (n[y][x] - 0.5) * var * 2
            if streaks and (y + int(n[y][x] * 6)) % 16 < 2:
                k *= 0.82
            if rng.random() < 0.02:
                k *= 0.8
            px[x, y] = tint(base, k)
    d = ImageDraw.Draw(img)
    for _ in range(cracks):
        x, y = rng.randint(0, S - 1), rng.randint(0, S - 1)
        for _ in range(rng.randint(6, 14)):
            nx, ny = x + rng.randint(-4, 4), y + rng.randint(-4, 4)
            d.line([x, y, nx, ny], fill=tint(base, 0.72), width=1)
            x, y = nx, ny
    return img


def crystal(d: ImageDraw.ImageDraw, cx, cy, r, pal, rng, angle=None):
    """Огранённый кристалл: тёмная тень, грани разной яркости, блик."""
    angle = rng.uniform(0, math.pi) if angle is None else angle
    pts = []
    for i in range(6):
        a = angle + i * math.pi / 3
        rr = r * (1.0 if i % 3 == 0 else 0.62)
        pts.append((cx + math.cos(a) * rr, cy + math.sin(a) * rr))
    d.polygon([(x + 1.5, y + 1.5) for x, y in pts], fill=hexrgb(pal["o"], 200))
    keys = ["l", "m", "d", "d", "m", "w"]
    for i in range(6):
        p1, p2 = pts[i], pts[(i + 1) % 6]
        d.polygon([(cx, cy), p1, p2], fill=hexrgb(pal[keys[i]]))
    d.polygon(pts, outline=hexrgb(pal["o"]))
    d.line([(cx, cy), pts[0]], fill=hexrgb(pal["w"]))


def ore(seed, base_img, pal, clusters=5, size=(5, 9)):
    rng = random.Random(seed)
    img = base_img.copy()
    d = ImageDraw.Draw(img)
    placed = []
    tries = 0
    while len(placed) < clusters and tries < 200:
        tries += 1
        cx, cy = rng.randint(8, S - 8), rng.randint(8, S - 8)
        if any((cx - x) ** 2 + (cy - y) ** 2 < 220 for x, y in placed):
            continue
        placed.append((cx, cy))
        for _ in range(rng.randint(2, 3)):
            crystal(d, cx + rng.randint(-3, 3), cy + rng.randint(-3, 3), rng.randint(*size), pal, rng)
    return img


def metal(pal, seed, emblem=True):
    img = Image.new("RGBA", (S, S), hexrgb(pal["m"]))
    d = ImageDraw.Draw(img)
    n = value_noise(seed, 8, 3)
    px = img.load()
    base = hexrgb(pal["m"])
    for y in range(S):
        for x in range(S):
            k = 0.92 + 0.12 * n[y][x] + 0.1 * (1 - (x + y) / (2 * S))
            px[x, y] = tint(base, k)
    # фаска
    for i in range(4):
        d.line([i, i, S - 1 - i, i], fill=hexrgb(pal["w"] if i == 1 else pal["l"]))
        d.line([i, i, i, S - 1 - i], fill=hexrgb(pal["l"]))
        d.line([i, S - 1 - i, S - 1 - i, S - 1 - i], fill=hexrgb(pal["d"]))
        d.line([S - 1 - i, i, S - 1 - i, S - 1 - i], fill=hexrgb(pal["d"]))
    d.rectangle([0, 0, S - 1, S - 1], outline=hexrgb(pal["o"]))
    d.rectangle([8, 8, S - 9, S - 9], outline=hexrgb(pal["d"]))
    d.rectangle([9, 9, S - 10, S - 10], outline=hexrgb(pal["l"]))
    for cx, cy in [(6, 6), (S - 7, 6), (6, S - 7), (S - 7, S - 7)]:
        d.ellipse([cx - 2, cy - 2, cx + 2, cy + 2], fill=hexrgb(pal["d"]), outline=hexrgb(pal["o"]))
        d.point((cx - 1, cy - 1), fill=hexrgb(pal["w"]))
    if emblem:
        snowflake(d, S // 2, S // 2, 14, hexrgb(pal["d"]), hexrgb(pal["w"]))
    return img


def snowflake(d, cx, cy, r, dark, light):
    for k in range(6):
        a = math.pi / 3 * k
        ex, ey = cx + r * math.cos(a), cy + r * math.sin(a)
        d.line([cx + 1, cy + 1, ex + 1, ey + 1], fill=dark, width=2)
        d.line([cx, cy, ex, ey], fill=light, width=2)
        for frac in (0.5,):
            bx, by = cx + r * frac * math.cos(a), cy + r * frac * math.sin(a)
            for side in (-1, 1):
                a2 = a + side * math.pi / 4
                d.line([bx, by, bx + 5 * math.cos(a2), by + 5 * math.sin(a2)], fill=light, width=1)
    d.ellipse([cx - 3, cy - 3, cx + 3, cy + 3], fill=light)


def facets(pal, seed, accent=None):
    rng = random.Random(seed)
    pts = [(rng.uniform(0, S), rng.uniform(0, S)) for _ in range(22)]
    img = Image.new("RGBA", (S, S))
    px = img.load()
    keys = ["d", "m", "m", "l", "l", "w"]
    cols = [hexrgb((accent if (accent and i % 5 == 0) else pal)[keys[i % 6]]) for i in range(len(pts))]
    for y in range(S):
        for x in range(S):
            best, second, bi = 1e9, 1e9, 0
            for i, (sx, sy) in enumerate(pts):
                dx = min(abs(x - sx), S - abs(x - sx))
                dy = min(abs(y - sy), S - abs(y - sy))
                dd = dx * dx + dy * dy
                if dd < best:
                    second, best, bi = best, dd, i
                elif dd < second:
                    second = dd
            c = cols[bi]
            if math.sqrt(second) - math.sqrt(best) < 1.2:
                c = hexrgb(pal["o"])
            px[x, y] = c
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, S - 1, S - 1], outline=hexrgb(pal["o"]))
    return img


def starfield(pal, seed):
    rng = random.Random(seed)
    n = value_noise(seed, 16, 3)
    img = Image.new("RGBA", (S, S))
    px = img.load()
    for y in range(S):
        for x in range(S):
            t = n[y][x]
            c1, c2 = hexrgb(pal["o"]), hexrgb(pal["d"])
            px[x, y] = tuple(int(c1[i] * (1 - t) + c2[i] * t) for i in range(3)) + (255,)
    d = ImageDraw.Draw(img)
    for _ in range(26):
        x, y = rng.randint(1, S - 2), rng.randint(1, S - 2)
        c = hexrgb(pal["w"] if rng.random() < 0.5 else pal["l"])
        if rng.random() < 0.3:
            d.line([x - 2, y, x + 2, y], fill=c)
            d.line([x, y - 2, x, y + 2], fill=c)
        d.point((x, y), fill=(255, 255, 255, 255))
    d.rectangle([0, 0, S - 1, S - 1], outline=hexrgb(pal["o"]))
    return img


def lamp(pal_frame, pal_glow):
    img = Image.new("RGBA", (S, S))
    px = img.load()
    for y in range(S):
        for x in range(S):
            dd = math.hypot(x - S / 2 + 0.5, y - S / 2 + 0.5) / (S / 2)
            t = max(0.0, 1 - dd)
            c1, c2 = hexrgb(pal_glow["m"]), hexrgb(0xFFFFFF)
            px[x, y] = tuple(int(c1[i] * (1 - t) + c2[i] * t) for i in range(3)) + (255,)
    d = ImageDraw.Draw(img)
    for i in range(5):
        d.rectangle([i, i, S - 1 - i, S - 1 - i], outline=hexrgb(pal_frame["d" if i in (0, 4) else "m"]))
    for o in (16, 32, 48):
        d.line([o, 5, o, S - 6], fill=hexrgb(pal_frame["d"], 120))
        d.line([5, o, S - 6, o], fill=hexrgb(pal_frame["d"], 120))
    snowflake(d, S // 2, S // 2, 10, hexrgb(pal_glow["d"]), hexrgb(0xFFFFFF))
    return img


def altar_textures(pal_stone, pal_glow):
    """Верх (руна), бок (резьба со светящимися линиями), низ."""
    base = stone(77, (232, 236, 240), var=0.08)
    top = base.copy()
    d = ImageDraw.Draw(top)
    d.ellipse([6, 6, S - 7, S - 7], outline=hexrgb(pal_glow["m"]), width=2)
    d.ellipse([14, 14, S - 15, S - 15], outline=hexrgb(pal_glow["l"]), width=1)
    snowflake(d, S // 2, S // 2, 16, hexrgb(pal_glow["d"]), hexrgb(pal_glow["l"]))
    for k in range(8):
        a = k * math.pi / 4
        x, y = S / 2 + 25 * math.cos(a), S / 2 + 25 * math.sin(a)
        d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=hexrgb(pal_glow["w"]))
    side = base.copy()
    d = ImageDraw.Draw(side)
    d.rectangle([0, 0, S - 1, 9], fill=hexrgb(pal_stone["l"]))
    d.rectangle([0, S - 10, S - 1, S - 1], fill=hexrgb(pal_stone["l"]))
    d.line([0, 10, S - 1, 10], fill=hexrgb(pal_stone["d"]))
    d.line([0, S - 11, S - 1, S - 11], fill=hexrgb(pal_stone["d"]))
    for x in range(8, S, 16):
        d.line([x, 16, x, S - 17], fill=hexrgb(pal_glow["m"]), width=2)
        d.ellipse([x - 3, S // 2 - 3, x + 3, S // 2 + 3], fill=hexrgb(pal_glow["l"]))
    bottom = stone(78, (200, 205, 212), var=0.1)
    return top, side, bottom


def armor_layer_hd(layer, pal, seed, scale=4):
    rng = random.Random(seed + layer)
    w, h = 64 * scale, 32 * scale
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    regions = ([(0, 0, 32, 16), (16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32)] if layer == 1
               else [(16, 16, 40, 32), (0, 16, 16, 32)])
    n = value_noise(seed, 8, 3)
    px = img.load()
    for (x0, y0, x1, y1) in regions:
        for y in range(y0 * scale, y1 * scale):
            for x in range(x0 * scale, x1 * scale):
                lx, ly = x - x0 * scale, y - y0 * scale
                t = n[y % S][x % S]
                key = "l" if t > 0.62 else "m" if t > 0.38 else "d"
                c = hexrgb(pal[key])
                # швы по краям каждой грани модели (8×8 текселей)
                if lx % (8 * scale) < 1 or ly % (8 * scale) < 1:
                    c = hexrgb(pal["o"])
                elif lx % (8 * scale) < 2 or ly % (8 * scale) < 2:
                    c = hexrgb(pal["w"])
                elif rng.random() < 0.004:
                    c = hexrgb(0xFFFFFF)
                px[x, y] = c
    return img


# ---------------------------------------------------------------- мобы
def recolor_entity(src: Image.Image, stops, scale=4, glow=None, seed=1) -> Image.Image:
    """Перекраска ванильной текстуры по яркости в градиент stops, апскейл ×scale с деталями."""
    rng = random.Random(seed)
    src = src.convert("RGBA")
    w, h = src.size
    out = Image.new("RGBA", (w * scale, h * scale), (0, 0, 0, 0))
    sp, op = src.load(), out.load()
    stops = [hexrgb(c) for c in stops]

    def grad(t):
        t = max(0.0, min(1.0, t)) * (len(stops) - 1)
        i = min(int(t), len(stops) - 2)
        f = t - i
        return tuple(int(stops[i][k] * (1 - f) + stops[i + 1][k] * f) for k in range(3))

    for y in range(h):
        for x in range(w):
            r, g, b, a = sp[x, y]
            if a == 0:
                continue
            lum = (0.3 * r + 0.59 * g + 0.11 * b) / 255
            for sy in range(scale):
                for sx in range(scale):
                    jitter = rng.uniform(-0.04, 0.04)
                    edge = 0.9 if (sx == 0 or sy == 0) else 1.0
                    c = grad(lum + jitter)
                    c = tuple(min(255, int(v * edge)) for v in c)
                    if glow and lum > glow:
                        c = tuple(min(255, int(v * 1.25)) for v in c)
                    op[x * scale + sx, y * scale + sy] = c + (a,)
    return out
