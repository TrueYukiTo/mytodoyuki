"""Новые текстуры YukiCore 2.0: металлы трёх уровней, компоненты, руды Энда, шаблоны, звёзды."""
import colorsys
import random

from PIL import Image, ImageDraw

from .pixel import ART, BLADE_ART, CRYSTAL, ICE, RAW, SAKURA, hexrgb, metal_block, orb, sprite, heart

# ---------------------------------------------------------------- палитры уровней
FROST = ICE
AURORIUM = {"o": 0x0E2B2E, "d": 0x168A80, "m": 0x35D6B4, "l": 0xA6FFE6, "w": 0xF4FFFB}
CELESTIUM = {"o": 0x3A1F5A, "d": 0xB0812A, "m": 0xF2CF55, "l": 0xFFF1A6, "w": 0xFFFFFF}
STARLIGHT = {"o": 0x1E1446, "d": 0x5A46C8, "m": 0x9C8CFF, "l": 0xD8D0FF, "w": 0xFFFFFF}
ESSENCE = {"o": 0x14284A, "d": 0x4A7BD8, "m": 0x8FD8FF, "l": 0xDDF6FF, "w": 0xFFFFFF}
CIRCUIT = {"o": 0x0B1A12, "d": 0x1E5A3A, "m": 0x2F8F5A, "l": 0x8FD8FF, "w": 0xF2FBFF}
UNSTABLE = {"o": 0x3A0A1A, "d": 0xA0203C, "m": 0xF0506E, "l": 0xFFB0C0, "w": 0xFFFFFF}

TIERS = {"frost": FROST, "aurorium": AURORIUM, "celestium": CELESTIUM}

# ---------------------------------------------------------------- маски
SHARD = """
................
................
..........oo....
.........owlo...
........owlmo...
.......owlmmo...
......owlmmdo...
.....owlmmdo....
....owlmmdo.....
...owlmmdo......
...olmmdo.......
....oddo........
.....oo.........
"""

PLATE = """
................
................
................
..oooooooooooo..
..owwwllllllmo..
..owllllllmmmo..
..olllllmmmmdo..
..ollllmmmmmdo..
..olllmmmmmmdo..
..ollmmmmmmddo..
..olmmmmmmdddo..
..ommmmmmddddo..
..oooooooooooo..
"""

ESSENCE_ART = """
................
......oooo......
......ollo......
......oddo......
.....oowwoo.....
....owwllllo....
...owllllmmmo...
...owllmmmmmo...
...olllmmmmdo...
...ollmmmmmdo...
...olmmmmmddo...
....ommmmddo....
.....oooooo.....
"""

CIRCUIT_ART = """
................
..oooooooooooo..
..odmdmdmdmdmo..
..omlllllllldo..
..odlwwwwwwlmo..
..omlwmmmmwldo..
..odlwmllmwlmo..
..omlwmllmwldo..
..odlwmmmmwlmo..
..omlwwwwwwldo..
..odlllllllldo..
..omdmdmdmdmdo..
..oooooooooooo..
"""

GEAR = """
................
.......oo.......
....oo.olo.oo...
...olloollollo..
...ollllllllmo..
....ollmoomlo...
..ooolmo..omlooo
..olllmo..omlmmo
..ommmlo..omdddo
..ooolmo..omdooo
....olmmoommdo..
...olmmmmmmmddo.
...oddoommoddo..
....oo.odo.oo...
.......oo.......
"""

TEMPLATE = """
................
...oooooooooo...
...owllllllmo...
...olooooooldo..
...olo.ww.oldo..
...olowllwoldo..
...olowllwoldo..
...olo.ww.oldo..
...olooooooldo..
...olllllllldo..
...oddddddddo...
...oooooooooo...
"""

INGOT = ART["frost_ingot"][0]
STAR = ART["yuki_star"][0]
TOOLS = ["sword", "pickaxe", "axe", "shovel", "hoe"]
ARMOR = ["helmet", "chestplate", "leggings", "boots"]


def tinted(art, pal):
    return sprite(art, pal)


def rainbow(base: Image.Image, sat=0.55, shift=0.0):
    out = Image.new("RGBA", base.size, (0, 0, 0, 0))
    for y in range(base.height):
        for x in range(base.width):
            r, g, b, a = base.getpixel((x, y))
            if a == 0:
                continue
            lum = (r + g + b) / 765
            h = ((x + y) / 30.0 + shift) % 1.0
            rr, gg, bb = colorsys.hsv_to_rgb(h, sat if lum < 0.95 else 0.1, min(1.0, 0.35 + lum * 0.75))
            out.putpixel((x, y), (int(rr * 255), int(gg * 255), int(bb * 255), 255))
    return out


def sparkle(img: Image.Image, color=0xFFFFFF, seed=1, n=3):
    rng = random.Random(seed)
    out = img.copy()
    pts = [(x, y) for y in range(16) for x in range(16) if img.getpixel((x, y))[3] > 0]
    for x, y in rng.sample(pts, min(n, len(pts))):
        out.putpixel((x, y), hexrgb(color))
    return out


def item_textures():
    """name -> (Image, handheld)"""
    t = {}
    for tier, pal in TIERS.items():
        for tool in TOOLS:
            t[f"{tier}_{tool}"] = (tinted(ART[f"frost_{tool}"][0], pal), True)
        for piece in ARMOR:
            t[f"{tier}_{piece}"] = (tinted(ART[f"frost_{piece}"][0], pal), False)
        t[f"{tier}_upgrade_template"] = (tinted(TEMPLATE, pal), False)
    t["frost_ingot"] = (tinted(INGOT, FROST), False)
    t["aurorium_ingot"] = (sparkle(tinted(INGOT, AURORIUM), 0xFFD6F2, 3), False)
    t["celestium_ingot"] = (sparkle(tinted(INGOT, CELESTIUM), 0xFFFFFF, 4, 4), False)
    t["raw_frost"] = (tinted(ART["raw_frost"][0], RAW), False)
    t["yuki_crystal"] = (tinted(ART["yuki_crystal"][0], CRYSTAL), False)
    t["starlight_shard"] = (sparkle(tinted(SHARD, STARLIGHT), 0xFFFFFF, 5), False)
    t["star_shard"] = (rainbow(tinted(SHARD, SAKURA), 0.6, 0.3), False)
    t["frost_plate"] = (tinted(PLATE, FROST), False)
    t["yuki_essence"] = (tinted(ESSENCE_ART, ESSENCE), False)
    t["cryo_circuit"] = (tinted(CIRCUIT_ART, CIRCUIT), False)
    t["frost_mechanism"] = (tinted(GEAR, CRYSTAL), False)
    t["incomplete_frost_mechanism"] = (tinted(GEAR, RAW), False)
    t["yuki_star"] = (tinted(STAR, SAKURA), False)
    t["frost_apple"] = (tinted(ART["frost_apple"][0], ICE), False)
    t["frost_core"] = (orb(FROST), False)
    t["tech_core"] = (orb({"o": 0x2A1A05, "d": 0xA0601A, "m": 0xF0A030, "l": 0xFFD27A, "w": 0xFFF6DA}), False)
    t["magic_core"] = (orb({"o": 0x24093A, "d": 0x6A2BA8, "m": 0xA65CF0, "l": 0xD9B0FF, "w": 0xFBF0FF}), False)
    t["nature_core"] = (orb({"o": 0x0E2A10, "d": 0x2E7D32, "m": 0x5CC060, "l": 0xA8F0A0, "w": 0xF0FFE8}), False)
    t["dimension_core"] = (orb({"o": 0x061A22, "d": 0x0F6E7A, "m": 0x22C2C8, "l": 0x9AF4F0, "w": 0xF0FFFE}), False)
    t["yuki_heart"] = (heart(), False)
    t["unstable_infinity_star"] = (tinted(STAR, UNSTABLE), False)
    t["yuki_infinity_star"] = (rainbow(tinted(STAR, SAKURA)), False)
    t["yuki_blade"] = (sprite(BLADE_ART, SAKURA), True)
    return t


# ---------------------------------------------------------------- блоки
END_STONE = (221, 223, 165)


def end_stone(rng):
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = rng.randint(-14, 10)
            if rng.random() < 0.08:
                v -= 25
            c = tuple(max(0, min(255, END_STONE[i] + v)) for i in range(3)) + (255,)
            img.putpixel((x, y), c)
    return img


def end_ore(rng, pal, clusters, glow=False):
    img = end_stone(rng)
    cols = [hexrgb(pal[k]) for k in ("o", "d", "m", "l", "w")]
    for _ in range(clusters):
        cx, cy = rng.randint(1, 12), rng.randint(1, 12)
        shape = [(0, 0), (1, 0), (0, 1), (1, 1), (2, 1), (1, 2), (2, 2)]
        for dx, dy in shape:
            img.putpixel((cx + dx, cy + dy), cols[1 + (dx + dy) % 4])
        img.putpixel((cx, cy), cols[4])
        if glow:
            img.putpixel((cx + 2, cy), cols[4])
    return img


def starlight_block(rng):
    img = Image.new("RGBA", (16, 16), hexrgb(STARLIGHT["o"]))
    for y in range(16):
        for x in range(16):
            k = "d" if (x * 3 + y * 5) % 7 < 3 else "m"
            img.putpixel((x, y), hexrgb(STARLIGHT[k]))
    for _ in range(10):
        x, y = rng.randint(1, 14), rng.randint(1, 14)
        img.putpixel((x, y), hexrgb(0xFFFFFF))
    ImageDraw.Draw(img).rectangle([0, 0, 15, 15], outline=hexrgb(STARLIGHT["o"]))
    return img


def block_textures(rng):
    from .pixel import crystal_block, lamp_block
    return {
        "frost_ore": end_ore(rng, RAW, 5),
        "yuki_crystal_ore": end_ore(rng, CRYSTAL, 4, True),
        "starlight_ore": end_ore(rng, STARLIGHT, 3, True),
        "frost_block": metal_block(FROST),
        "aurorium_block": metal_block(AURORIUM),
        "celestium_block": metal_block(CELESTIUM),
        "yuki_crystal_block": crystal_block(rng),
        "starlight_block": starlight_block(rng),
        "yuki_lamp": lamp_block(),
    }


def armor_layer(layer, pal, seed):
    rng = random.Random(seed + layer)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    regions = ([(0, 0, 32, 16), (16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32)] if layer == 1
               else [(16, 16, 40, 32), (0, 16, 16, 32)])
    keys = ["d", "m", "m", "l"]
    for (x0, y0, x1, y1) in regions:
        for y in range(y0, y1):
            for x in range(x0, x1):
                k = keys[rng.randint(0, 3)]
                if x == x0 or y == y0:
                    k = "o"
                elif rng.random() < 0.03:
                    k = "w"
                img.putpixel((x, y), hexrgb(pal[k]))
    return img
