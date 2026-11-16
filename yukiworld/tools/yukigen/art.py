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


# ================================================================ YukiCore 3.0
from . import hd  # noqa: E402

PERMAFROST = {"o": 0x0E2A40, "d": 0x2A78B0, "m": 0x7CC8F0, "l": 0xD6F2FF, "w": 0xFFFFFF}
AURORA = {"o": 0x1A1240, "d": 0x2FA88A, "m": 0x6CF0C0, "l": 0xE0B0FF, "w": 0xFFFFFF}
PYRO = {"o": 0x2A0A04, "d": 0xA0300C, "m": 0xF07A1C, "l": 0xFFD060, "w": 0xFFF6D0}
VOIDP = {"o": 0x07020F, "d": 0x2A0E4A, "m": 0x5A2A9A, "l": 0xA070F0, "w": 0xE8D8FF}
STELLAR = {"o": 0x3A2A08, "d": 0xC09020, "m": 0xFFE070, "l": 0xFFF6C8, "w": 0xFFFFFF}
ICEBLUE = {"o": 0x0A2440, "d": 0x2C6CB0, "m": 0x78C4F6, "l": 0xD0F0FF, "w": 0xFFFFFF}
ROYAL = {"o": 0x30204A, "d": 0x8A8AC8, "m": 0xD8E4FF, "l": 0xF4F8FF, "w": 0xFFFFFF}
TEAR = {"o": 0x4A1440, "d": 0xC04A9A, "m": 0xFF9EDC, "l": 0xFFE0F4, "w": 0xFFFFFF}
GOLDPINK = {"o": 0x3A1A30, "d": 0xC08A30, "m": 0xFFD060, "l": 0xFFE6F2, "w": 0xFFFFFF}

GEM = """
................
................
.....oooooo.....
....owwlllmo....
...owllllmmmo...
..owllllmmmmdo..
..olllllmmmmdo..
...olllmmmmdo...
....ollmmmdo....
.....olmmdo.....
......omdo......
.......oo.......
"""

SCALE = """
................
.......oo.......
.....oowwoo.....
....owwlllmo....
...owllllmmmo...
...olllmmmmdo...
..owllmmmmmmdo..
..olllmmmmmddo..
..ollmmmmmmddo..
...olmmmmmddo...
...ommmmmdddo...
....ommmdddo....
.....oodddo.....
.......ooo......
"""

TEAR_ART = """
.......oo.......
.......owo......
......owlo......
......owlmo.....
.....owllmo.....
.....owllmmo....
....owlllmmmo...
....owllmmmmo...
...owlllmmmmdo..
...owllmmmmmdo..
...ollmmmmmmdo..
...ollmmmmmddo..
....olmmmmddo...
.....ommmddo....
......ooooo.....
"""

CROWN = """
................
................
................
..o....oo....o..
.owo..owwo..owo.
.olo..olmo..olo.
.olmo.olmo.omdo.
.ollmoollmoomdo.
.olllmmmmmmmmdo.
.owwlwlwlwlwmdo.
.olmlmdmlmdmmdo.
.olllmmmmmmmddo.
.oddddddddddddo.
..oooooooooooo..
"""

MEDALLION = """
................
.....oooooo.....
...oowwllllmoo..
..owwllllllmmdo.
..owlloooooomdo.
.owllo.wwww.omdo
.owlo.wllllw.odo
.owlo.wlmmlw.odo
.ollo.wlmmlw.odo
.ollo.wllllw.odo
.olmmo.wwww.oddo
..olmmoooooodddo
..olmmmmmmmddddo
...oommmmmddddo.
.....oooooooo...
"""

WAND = """
............oo..
...........owwo.
..........owllmo
..........olmmdo
...........omdo.
..........ohoo..
.........ohHo...
........ohHo....
.......ohHo.....
......ohHo......
.....ohHo.......
....ohHo........
...ohHo.........
..ohHo..........
..oHo...........
...o............
"""

ORB_HOT = None  # ядро пепла — как orb() с горячей палитрой


def hd_textures():
    """name -> (Image64, handheld) — все предметы YukiCore в HD."""
    base = item_textures()
    out = {}
    for i, (name, (img, handheld)) in enumerate(sorted(base.items())):
        spark = 3 if any(k in name for k in ("celestium", "star", "starlight", "infinity", "heart", "essence")) else 0
        out[name] = (hd.hd_item(img, seed=i, sparkles=spark), handheld)
    new = {
        "frost_wand": (sprite(WAND, CRYSTAL), True, 2),
        "permafrost_crystal": (sprite(GEM, PERMAFROST), False, 2),
        "aurora_shard": (sprite(SHARD, AURORA), False, 3),
        "raw_pyrolite": (sprite(ART["raw_frost"][0], PYRO), False, 0),
        "pyrolite_ingot": (sprite(INGOT, PYRO), False, 1),
        "void_shard": (sprite(SHARD, VOIDP), False, 2),
        "stellar_shard": (sprite(STAR, STELLAR), False, 4),
        "colossus_heart": (heart_pal(ICEBLUE), False, 3),
        "blizzard_crown": (sprite(CROWN, ROYAL), False, 3),
        "ash_core": (orb(PYRO), False, 2),
        "void_scale": (sprite(SCALE, VOIDP), False, 2),
        "yuki_tear": (sprite(TEAR_ART, TEAR), False, 3),
        "sigil_colossus": (sprite(MEDALLION, ICEBLUE), False, 2),
        "sigil_queen": (sprite(MEDALLION, ROYAL), False, 2),
        "sigil_ash_lord": (sprite(MEDALLION, PYRO), False, 2),
        "sigil_wyrm": (sprite(MEDALLION, VOIDP), False, 2),
        "sigil_shadow": (sprite(MEDALLION, TEAR), False, 3),
        "crown_of_yuki": (sprite(CROWN, GOLDPINK), False, 4),
        "eternal_winter_blade": (rainbow(sprite(BLADE_ART, ICEBLUE), 0.35, 0.55), True, 4),
    }
    for i, (name, (img, handheld, spark)) in enumerate(new.items()):
        out[name] = (hd.hd_item(img, seed=100 + i, sparkles=spark), handheld)
    return out


def heart_pal(pal):
    art = """
................
................
..oooo....oooo..
.owwllo..olmmdo.
owwlllmoolmmmddo
owlllmmmmmmmmddo
olllmmmmmmmmmddo
olllmmmmmmmmdddo
.ollmmmmmmmmddo.
..olmmmmmmmddo..
...olmmmmmddo...
....olmmmddo....
.....olmddo.....
......oddo......
.......oo.......
"""
    return sprite(art, pal)


PLANET_STONE = {
    "permafrost": ((176, 214, 240), 0.12, 6, False),   # спрессованный лёд
    "aurora": ((150, 104, 210), 0.18, 0, False),        # аметист
    "pyrolite": ((44, 38, 44), 0.25, 3, False),         # чернокамень
    "void": ((74, 74, 80), 0.18, 0, True),              # глубинный сланец
    "stellar": ((226, 222, 214), 0.08, 2, False),       # кальцит
}
PLANET_PAL = {"permafrost": PERMAFROST, "aurora": AURORA, "pyrolite": PYRO, "void": VOIDP, "stellar": STELLAR}


def hd_blocks():
    rng = random.Random(4242)
    t = {}
    es = (221, 223, 165)
    t["frost_ore"] = hd.ore(1, hd.stone(1, es, 0.16), RAW, 5)
    t["yuki_crystal_ore"] = hd.ore(2, hd.stone(2, es, 0.16), CRYSTAL, 4)
    t["starlight_ore"] = hd.ore(3, hd.stone(3, es, 0.16), STARLIGHT, 3, (6, 10))
    t["frost_block"] = hd.metal(FROST, 4)
    t["aurorium_block"] = hd.metal(AURORIUM, 5)
    t["celestium_block"] = hd.metal(CELESTIUM, 6)
    t["pyrolite_block"] = hd.metal(PYRO, 7)
    t["yuki_crystal_block"] = hd.facets(CRYSTAL, 8, SAKURA)
    t["permafrost_block"] = hd.facets(PERMAFROST, 9)
    t["aurora_block"] = hd.facets(AURORA, 10, SAKURA)
    t["starlight_block"] = hd.starfield(STARLIGHT, 11)
    t["void_block"] = hd.starfield(VOIDP, 12)
    t["stellar_block"] = hd.facets(STELLAR, 13)
    t["yuki_lamp"] = hd.lamp(CRYSTAL, SAKURA)
    for i, (name, (base, var, cracks, streaks)) in enumerate(PLANET_STONE.items()):
        stone = hd.stone(20 + i, base, var, cracks, streaks)
        t[f"{name}_ore"] = hd.ore(30 + i, stone, PLANET_PAL[name], 5, (5, 9))
    return t
