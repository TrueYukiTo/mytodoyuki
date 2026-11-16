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


# ================================================================ YukiCore 4.0 — Новый год
RED = {"o": 0x3A0808, "d": 0xA01818, "m": 0xE83A3A, "l": 0xFF9A8A, "w": 0xFFFFFF}
GREEN = {"o": 0x06240E, "d": 0x16702E, "m": 0x2FB04E, "l": 0x9AF0A8, "w": 0xF0FFF2}
GOLD = {"o": 0x3A2606, "d": 0xB07A10, "m": 0xF2C030, "l": 0xFFE68A, "w": 0xFFFFFF}
ORANGE = {"o": 0x3A1A02, "d": 0xC05A08, "m": 0xFF8A1C, "l": 0xFFC27A, "w": 0xFFF4E0}
BROWN = {"o": 0x2A1406, "d": 0x7A4418, "m": 0xB8722E, "l": 0xE8B070, "w": 0xFFFFFF}
COCOA = {"o": 0x24120A, "d": 0x5A2E18, "m": 0x8A4E2A, "l": 0xF4F0EA, "w": 0xFFFFFF}

MAGNET = """
................
..oooo....oooo..
..owwo....owwo..
..ommo....ommo..
..olmo....olmo..
..olmo....olmo..
..olmo....olmo..
..olmo....olmo..
..olmmo..ommdo..
..ollmmooommdo..
...olmmmmmmdo...
....ollmmmdo....
.....oooooo.....
"""

HAMMER = """
................
...oooooooooo...
..owwwllllmmmo..
..owllllmmmmdo..
..olllmmmmmddo..
..oddddhhdddddo.
...oooohHoooo...
......ohHo......
......ohHo......
......ohHo......
......ohHo......
......ohHo......
......ohHo......
......oHHo......
.......oo.......
"""

SNOWBOOTS = """
................
................
................
................
...wwww..wwww...
...owwo..owwo...
...olmo..omdo...
...olmo..omdo...
...olmo..omdo...
..oolmo..ommdoo.
.owllmo..ommmddo
.owwwwo..owwwwwo
.oooooo..oooooo.
"""

GIFT = """
................
.....oo..oo.....
....owwoowwo....
.....owwwwo.....
..oooooyyoooooo.
..omlmmmyymmmdo.
..olmmmmyymmmdo.
..oyyyyyyyyyyyo.
..oyyyyyyyyyyyo.
..ommmmmyymmmdo.
..olmmmmyymmmdo.
..olmmmmyymmmdo.
..ommmmmyymmddo.
..oddddmyyddddo.
..ooooooooooooo.
"""

CALENDAR = """
................
..oooooooooooo..
..odddddddddddo.
..odwdwdwdwdwdo.
..oooooooooooo..
..owllowllowllo.
..olmmolmmolmmo.
..oooooooooooo..
..owllowllowllo.
..olmmolmmolmmo.
..oooooooooooo..
..owllowllowllo.
..olmmolmmolmmo.
..oooooooooooo..
"""

TANGERINE = """
................
........gg......
.......gGgg.....
.....oooGooo....
....owwlllmmo...
...owwllllmmdo..
...owllllmmmdo..
...olllllmmmdo..
...ollllmmmmdo..
...olllmmmmddo..
....olmmmmddo...
.....oommddo....
.......ooo......
"""

CANDY = """
................
.....oooooo.....
....owwrrwwo....
...owrrwwrrwo...
...orwo..owrro..
...oo.....owwo..
..........orro..
..........owwo..
..........orro..
..........owwo..
..........orro..
..........owwo..
..........orro..
..........oooo..
"""

GINGER = """
................
......oooo......
.....ommmmo.....
.....owmwmo.....
.....ommmmo.....
..oooomwwmoooo..
..ommmmmmmmmmo..
..oooommrmmooo..
.....ommmmo.....
.....ommrmo.....
....ommoommo....
....omo..omo....
....oo....oo....
"""

OLIVIER = """
................
................
....oyygyryo....
...oyrgyyygyo...
..oyygyrygyyyo..
..oooooooooooo..
..owllllllllmo..
..olllllllmmdo..
...ollllmmmdo...
....oommmddo....
......oooo......
"""

COCOA_ART = """
................
.....w..w.......
......w..w......
.....w..w.......
...oooooooooo...
...occccccccc...
...olmmmmmmmoooo
...olmmmmmmmo..o
...olmmmmmmmo..o
...olmmmmmmmoooo
...olmmmmmmdo...
....ommmmmdo....
.....oooooo.....
"""


def _sprite_extra(art, pal, extra):
    return sprite(art, {**pal, **extra})


def hd_textures_v4():
    yellow = {"y": GOLD["m"]}
    leaf = {"g": GREEN["d"], "G": GREEN["m"]}
    new = {
        "frost_magnet": (sprite(MAGNET, ICEBLUE), False, 2),
        "celestial_magnet": (sprite(MAGNET, CELESTIUM), False, 4),
        "aurorium_hammer": (sprite(HAMMER, AURORIUM), True, 2),
        "celestium_hammer": (sprite(HAMMER, CELESTIUM), True, 4),
        "snow_boots": (sprite(SNOWBOOTS, RED), False, 1),
        "gift": (_sprite_extra(GIFT, RED, yellow), False, 2),
        "grand_gift": (_sprite_extra(GIFT, VOIDP, yellow), False, 5),
        "advent_calendar": (sprite(CALENDAR, RED), False, 2),
        "tangerine": (_sprite_extra(TANGERINE, ORANGE, leaf), False, 0),
        "candy_cane": (_sprite_extra(CANDY, {"o": 0x3A0808, "w": 0xFFFFFF}, {"r": RED["m"]}), False, 0),
        "gingerbread": (_sprite_extra(GINGER, BROWN, {"r": RED["m"]}), False, 0),
        "olivier": (_sprite_extra(OLIVIER, {"o": 0x2A2A3A, "w": 0xFFFFFF, "l": 0xE8EEF8, "m": 0xC8D2E8, "d": 0x8A94B0},
                                   {"y": 0xF2E08A, "g": GREEN["m"], "r": RED["m"]}), False, 0),
        "hot_cocoa": (_sprite_extra(COCOA_ART, {"o": 0x3A0808, "l": RED["l"], "m": RED["m"], "d": RED["d"],
                                                "w": 0xFFFFFF}, {"c": COCOA["m"]}), False, 0),
    }
    return {name: (hd.hd_item(img, seed=300 + i, sparkles=spark), handheld)
            for i, (name, (img, handheld, spark)) in enumerate(new.items())}


def hd_blocks_v4():
    import math as _m
    from PIL import ImageDraw as _D
    S = 64
    t = {}
    # Часы Юки: целестиевый корпус + циферблат
    img = hd.metal(CELESTIUM, 40, emblem=False)
    d = _D.Draw(img)
    d.ellipse([12, 12, 51, 51], fill=hexrgb(0xF4F8FF), outline=hexrgb(CELESTIUM["o"]), width=2)
    for k in range(12):
        a = k * _m.pi / 6
        r1, r2 = (14, 18) if k % 3 else (11, 18)
        d.line([32 + r1 * _m.cos(a), 32 + r1 * _m.sin(a), 32 + r2 * _m.cos(a), 32 + r2 * _m.sin(a)],
               fill=hexrgb(CRYSTAL["d"]), width=2 if k % 3 == 0 else 1)
    d.line([32, 32, 32, 18], fill=hexrgb(0x203050), width=2)
    d.line([32, 32, 42, 36], fill=hexrgb(SAKURA["d"]), width=2)
    d.ellipse([29, 29, 35, 35], fill=hexrgb(SAKURA["m"]))
    t["yuki_chrono"] = img
    # Криогенератор: стальной корпус, решётки, ледяное ядро
    img = hd.metal(RAW, 41, emblem=False)
    d = _D.Draw(img)
    d.rectangle([16, 16, 47, 47], fill=hexrgb(0x0A1A30), outline=hexrgb(CRYSTAL["o"]), width=2)
    rng = random.Random(7)
    hd.crystal(d, 32, 32, 12, CRYSTAL, rng, angle=_m.pi / 2)
    for y in (6, 52):
        for x in range(14, 50, 6):
            d.rectangle([x, y, x + 3, y + 5], fill=hexrgb(0x1A2A40))
    t["cryo_generator"] = img
    # Гирлянда (вид сверху, прозрачный фон)
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = _D.Draw(img)
    colors = [0xFF4A4A, 0xFFD24A, 0x4AE07A, 0x4AB4FF, 0xFF8FE0]
    for row, y0 in enumerate((14, 46)):
        pts = [(x, y0 + 5 * _m.sin(x / 9 + row)) for x in range(0, S + 1, 2)]
        d.line(pts, fill=hexrgb(0x1E4A2A), width=2)
        for i, x in enumerate(range(4, S, 12)):
            y = y0 + 5 * _m.sin(x / 9 + row)
            c = colors[(i + row * 2) % len(colors)]
            d.ellipse([x - 4, y - 1, x + 4, y + 8], fill=hexrgb(c), outline=hexrgb(0x202020))
            d.ellipse([x - 2, y + 1, x, y + 3], fill=hexrgb(0xFFFFFF))
    t["festive_garland"] = img
    # Ёлочная игрушка: красный шар с бликом и золотым поясом
    img = Image.new("RGBA", (S, S), hexrgb(RED["d"]))
    px = img.load()
    for y in range(S):
        for x in range(S):
            dist = _m.hypot(x - 22, y - 22) / 60
            k = max(0.45, 1.25 - dist)
            px[x, y] = tuple(min(255, int(c * k)) for c in hexrgb(RED["m"])[:3]) + (255,)
    d = _D.Draw(img)
    d.rectangle([0, 28, S - 1, 35], fill=hexrgb(GOLD["m"]))
    d.line([0, 28, S - 1, 28], fill=hexrgb(GOLD["l"]))
    for x in range(4, S, 10):
        d.ellipse([x - 2, 30, x + 2, 33], fill=hexrgb(0xFFFFFF))
    d.ellipse([12, 8, 22, 18], fill=hexrgb(0xFFFFFF, 200))
    t["christmas_ornament"] = img
    # Ёлочка (крест-модель, прозрачный фон)
    img = Image.new("RGBA", (S, S), (0, 0, 0, 0))
    d = _D.Draw(img)
    d.rectangle([28, 52, 35, 63], fill=hexrgb(BROWN["d"]))
    for i, (top, half) in enumerate([(6, 10), (18, 16), (30, 22), (40, 28)]):
        d.polygon([(32, top), (32 - half, top + 16), (32 + half, top + 16)], fill=hexrgb(GREEN["d"] if i % 2 else GREEN["m"]),
                  outline=hexrgb(GREEN["o"]))
    rng = random.Random(11)
    for _ in range(14):
        x, y = rng.randint(14, 50), rng.randint(16, 54)
        if img.getpixel((x, y))[3] > 0:
            c = colors[rng.randint(0, len(colors) - 1)]
            d.ellipse([x - 2, y - 2, x + 2, y + 2], fill=hexrgb(c), outline=hexrgb(0xFFFFFF))
    d.polygon([(32, 0), (35, 6), (41, 6), (36, 10), (38, 16), (32, 12), (26, 16), (28, 10), (23, 6), (29, 6)],
              fill=hexrgb(GOLD["m"]), outline=hexrgb(GOLD["o"]))
    t["yuki_tree"] = img
    # Снежный шар: голубое стекло + основание
    img = Image.new("RGBA", (S, S), (190, 230, 255, 90))
    d = _D.Draw(img)
    d.rectangle([0, 0, S - 1, S - 1], outline=(255, 255, 255, 200), width=2)
    d.polygon([(32, 18), (20, 46), (44, 46)], fill=hexrgb(GREEN["m"]))
    d.rectangle([30, 46, 34, 52], fill=hexrgb(BROWN["d"]))
    rng = random.Random(5)
    for _ in range(30):
        x, y = rng.randint(4, 60), rng.randint(4, 60)
        d.point((x, y), fill=(255, 255, 255, 255))
    d.ellipse([8, 6, 18, 14], fill=(255, 255, 255, 160))
    t["snow_globe"] = img
    t["snow_globe_base"] = hd.metal(RED, 42, emblem=False)
    return t


# ================================================================ YukiCore 5.0
CRYOSTEEL = {"o": 0x101828, "d": 0x3A4E6A, "m": 0x7C98BC, "l": 0xC8DCF0, "w": 0xFFFFFF}
ALLOY = {"o": 0x2A0A3A, "d": 0x8A3AB0, "m": 0xE070D0, "l": 0xFFC8F0, "w": 0xFFFFFF}
SINTARA = {"o": 0x1A0A3A, "d": 0x6A3AC8, "m": 0xB080FF, "l": 0x8FF0D0, "w": 0xFFFFFF}

STAFF = """
...........oooo.
..........owllmo
..........olwlmo
.........owlmmdo
..........oddoo.
.........ohoo...
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

AMULET = """
.....oooooo.....
....o......o....
...o........o...
...o........o...
....o......o....
.....o....o.....
......oooo......
.....owwlmo.....
....owllmmdo....
....olllmmdo....
....ollmmmdo....
.....olmmdo.....
......oddo......
.......oo.......
"""


def hd_textures_v5():
    new = {
        "cryosteel_ingot": (sprite(INGOT, CRYOSTEEL), False, 1),
        "stellar_alloy": (rainbow(sprite(INGOT, ALLOY), 0.45, 0.8), False, 5),
        "sintara_staff": (sprite(STAFF, SINTARA), True, 5),
        "sintara_amulet": (sprite(AMULET, SINTARA), False, 4),
    }
    return {n: (hd.hd_item(img, seed=500 + i, sparkles=sp), hh) for i, (n, (img, hh, sp)) in enumerate(new.items())}


def hd_blocks_v5():
    from PIL import ImageDraw as _D
    t = {}
    t["frost_casing"] = hd.metal(CRYOSTEEL, 50, emblem=False)
    d = _D.Draw(t["frost_casing"])
    for k in range(4):
        d.line([12 + k * 12, 12, 12 + k * 12, 51], fill=hexrgb(CRYOSTEEL["d"]), width=2)
    t["cryosteel_block"] = hd.metal(CRYOSTEEL, 51)
    t["reactor_casing"] = hd.metal(VOIDP, 52, emblem=False)
    d = _D.Draw(t["reactor_casing"])
    d.rectangle([14, 14, 49, 49], outline=hexrgb(STARLIGHT["l"]), width=2)
    d.line([14, 14, 49, 49], fill=hexrgb(STARLIGHT["m"]), width=2)
    d.line([49, 14, 14, 49], fill=hexrgb(STARLIGHT["m"]), width=2)
    # контроллеры: лицевая панель
    t["frost_forge"] = hd.metal(CRYOSTEEL, 53, emblem=False)
    d = _D.Draw(t["frost_forge"])
    d.rectangle([14, 18, 49, 47], fill=hexrgb(0x1A0A04), outline=hexrgb(CRYOSTEEL["o"]), width=2)
    for i, c in enumerate([0xFF8A1C, 0xFFD060, 0x8FD8FF]):
        d.rectangle([18 + i * 10, 34, 25 + i * 10, 44], fill=hexrgb(c))
    d.rectangle([18, 22, 45, 28], fill=hexrgb(0x8FD8FF))
    t["star_reactor"] = hd.starfield(STARLIGHT, 54)
    d = _D.Draw(t["star_reactor"])
    d.ellipse([16, 16, 47, 47], outline=hexrgb(CELESTIUM["m"]), width=3)
    d.ellipse([26, 26, 37, 37], fill=hexrgb(0xFFFFFF))
    t["aurora_lamp"] = hd.lamp(SINTARA, AURORA)
    return t
