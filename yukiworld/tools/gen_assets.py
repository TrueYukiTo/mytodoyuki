#!/usr/bin/env python3
"""
Генератор ресурсов YukiCore: пиксель-арт текстуры, GUI-спрайты,
модели, blockstates, loot tables, рецепты, теги, генерация руд и локализация.

Запуск:  python3 yukiworld/tools/gen_assets.py
Нужен Pillow (pip install pillow). Результат пишется в yukicore/src/main/resources.
"""
import json
import random
from pathlib import Path

from PIL import Image, ImageDraw

MOD = "yukicore"
ROOT = Path(__file__).resolve().parent.parent / "yukicore" / "src" / "main" / "resources"
ASSETS = ROOT / "assets" / MOD
DATA = ROOT / "data"


def hexrgb(h, a=255):
    return ((h >> 16) & 255, (h >> 8) & 255, h & 255, a)


# ---------------------------------------------------------------- палитры
ICE = {"o": 0x14284A, "d": 0x2F5F9E, "m": 0x5AA8E6, "l": 0x9FE0FF, "w": 0xF2FBFF}
CRYSTAL = {"o": 0x1B2D5C, "d": 0x3A7BD5, "m": 0x6EC6FF, "l": 0xB8ECFF, "w": 0xFFE6F7}
SAKURA = {"o": 0x5A2350, "d": 0xC25FAE, "m": 0xFF9EDC, "l": 0xFFD6F2, "w": 0xFFFFFF}
RAW = {"o": 0x24324A, "d": 0x4F6F8F, "m": 0x86AFCF, "l": 0xC2DEEF, "w": 0xEEF8FF}
HANDLE = {"h": 0x4A2F1B, "H": 0x8A5A32}


def write(path: Path, data):
    path.parent.mkdir(parents=True, exist_ok=True)
    if isinstance(data, Image.Image):
        data.save(path)
    else:
        path.write_text(json.dumps(data, indent=2, ensure_ascii=False) + "\n", encoding="utf-8")


def sprite(art: str, palette: dict) -> Image.Image:
    rows = [r for r in art.strip("\n").split("\n")]
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    pal = {**HANDLE, **palette}
    for y, row in enumerate(rows[:16]):
        for x, ch in enumerate(row[:16]):
            if ch in pal:
                img.putpixel((x, y), hexrgb(pal[ch]))
    return img


# ---------------------------------------------------------------- пиксель-арт предметов
ART = {
    "frost_ingot": ("""
................
................
................
................
.......oooooo...
.....oowwwllmo..
...oowlllmmmdo..
..owllmmmmmddo..
..olmmmmmdddoo..
..odmmmmddoo....
..oddddooo......
...oooo.........
""", ICE),
    "yuki_crystal": ("""
................
.......oo.......
......owlo......
.....owllmo.....
....owllmmmo....
...owlllmmmdo...
...owllmmmddo...
..owlllmmmmddo..
..olllmmmmmddo..
...olmmmmmddo...
...ommmmmdddo...
....ommmddo.....
.....ommddo.....
......oddo......
.......oo.......
""", CRYSTAL),
    "raw_frost": ("""
................
................
......ooo.......
....oolwmoo.....
...olwlmmmdo....
..olwmmmmdddo...
..olmmmdmmddo...
.olmmmddmmmddo..
.ommdmmmmmdddo..
.odmmmmmdddddo..
..oddddmmdddo...
...oooddddoo....
......oooo......
""", RAW),
    "yuki_star": ("""
.......oo.......
.......owo......
......owwo......
......owlo......
.oooooowloooooo.
.owwwwllllmmmmo.
..owllllllmmmo..
...owlllllmmo...
....olllllmo....
....olllmllmo...
...olllo.olmmo..
...olmo...omdo..
..olo......odo..
..oo........oo..
""", SAKURA),
    "frost_apple": ("""
................
........oh......
.......oho......
....oooHooooo...
...owwllmlllmo..
..owlllllmmmmdo.
..olllllmmmmmdo.
..ollllmmmmmmdo.
..olllmmmmmmddo.
..olmmmmmmmddo..
...ommmmmmdddo..
...oommmmdddoo..
.....ooooooo....
""", ICE),
    "frost_sword": ("""
................
.............ooo
............olwo
...........olwmo
..........olwmo.
.........olwmo..
........olwmo...
...oo..olwmo....
...oloolwmo.....
....olmwmo......
....omlmo.......
...ohoomlo......
..ohHo.oo.......
.ohHo...........
.oHo............
..o.............
""", ICE),
    "frost_pickaxe": ("""
................
....ooooooo.....
...olwwwwmmo....
..olmmmmmmddo...
..oddoooohoddo..
..odo...ohHoddo.
...o...ohHo.odo.
......ohHo...odo
.....ohHo.....o.
....ohHo........
...ohHo.........
..ohHo..........
.ohHo...........
.oHo............
..o.............
""", ICE),
    "frost_axe": ("""
................
.......oooo.....
......olwwmo....
.....olwmmmdo...
.....olmmhddo...
......oohHddo...
.......ohHoddo..
......ohHo.oo...
.....ohHo.......
....ohHo........
...ohHo.........
..ohHo..........
.ohHo...........
.oHo............
..o.............
""", ICE),
    "frost_shovel": ("""
................
...........ooo..
..........olwmo.
.........olwmmdo
.........ommmddo
..........odddo.
.........ohooo..
........ohHo....
.......ohHo.....
......ohHo......
.....ohHo.......
....ohHo........
...ohHo.........
..ohHo..........
..oHo...........
...o............
""", ICE),
    "frost_hoe": ("""
................
.......oooooo...
......olwwmmdo..
.......oooohddo.
..........ohHoo.
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
""", ICE),
    "frost_helmet": ("""
................
................
................
....oooooooo....
...owwllllmmo...
..owllllmmmmdo..
..olllmmmmmmdo..
..olmoooooomdo..
..odo......odo..
..oo........oo..
""", ICE),
    "frost_chestplate": ("""
................
..ooo......ooo..
..owlo....omdo..
..owlloooomddo..
..owllllmmmmdo..
...olllmmmmdo...
...olllmmmmdo...
...ollmmmmddo...
...olmmmmmddo...
...olmmmmmddo...
...odmmmmdddo...
...oooooooooo...
""", ICE),
    "frost_leggings": ("""
................
...oooooooooo...
...owllllmmmo...
...olllmmmmdo...
...ollmooommdo..
...olmo..omdo...
...olmo..omdo...
...olmo..omdo...
...olmo..omdo...
...odmo..omdo...
...oooo..oooo...
""", ICE),
    "frost_boots": ("""
................
................
................
................
................
...oooo..oooo...
...olmo..omdo...
...olmo..omdo...
...olmo..omdo...
..oolmo..ommdoo.
.owllmo..ommmddo
.oooooo..oooooo.
""", ICE),
}

# Эндгейм-предметы рисуются процедурно (см. orb/heart/star ниже)
CORE_PALETTES = {
    "frost_core": ICE,
    "tech_core": {"o": 0x2A1A05, "d": 0xA0601A, "m": 0xF0A030, "l": 0xFFD27A, "w": 0xFFF6DA},
    "magic_core": {"o": 0x24093A, "d": 0x6A2BA8, "m": 0xA65CF0, "l": 0xD9B0FF, "w": 0xFBF0FF},
    "nature_core": {"o": 0x0E2A10, "d": 0x2E7D32, "m": 0x5CC060, "l": 0xA8F0A0, "w": 0xF0FFE8},
    "dimension_core": {"o": 0x061A22, "d": 0x0F6E7A, "m": 0x22C2C8, "l": 0x9AF4F0, "w": 0xF0FFFE},
}

HANDHELD = {"yuki_blade", "frost_sword", "frost_pickaxe", "frost_axe", "frost_shovel", "frost_hoe"}


# ---------------------------------------------------------------- эндгейм-спрайты
def orb(pal):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            dx, dy = x - 7.5, y - 7.5
            r = (dx * dx + dy * dy) ** 0.5
            if r <= 6.6:
                light = -(dx + dy) / 9.0 + (1 - r / 6.6) * 0.9
                k = "w" if light > 0.95 else "l" if light > 0.55 else "m" if light > 0.05 else "d"
                img.putpixel((x, y), hexrgb(pal[k]))
            elif r <= 7.6:
                img.putpixel((x, y), hexrgb(pal["o"]))
    # снежинка в центре
    for p in [(7, 5), (7, 6), (7, 9), (7, 10), (5, 7), (6, 7), (9, 7), (10, 7), (7, 7), (8, 8), (7, 8), (8, 7)]:
        img.putpixel(p, hexrgb(0xFFFFFF))
    return img


def heart():
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
    return sprite(art, SAKURA)


def rainbow_star():
    base = sprite(ART["yuki_star"][0], SAKURA)
    out = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    import colorsys
    for y in range(16):
        for x in range(16):
            r, g, b, a = base.getpixel((x, y))
            if a == 0:
                continue
            lum = (r + g + b) / 765
            h = ((x + y) / 30.0) % 1.0
            rr, gg, bb = colorsys.hsv_to_rgb(h, 0.55 if lum < 0.95 else 0.1, min(1.0, 0.35 + lum * 0.75))
            out.putpixel((x, y), (int(rr * 255), int(gg * 255), int(bb * 255), 255))
    return out


BLADE_ART = """
..............oo
.............owo
............owlo
...........owlmo
..........owlmo.
.........owlmo..
........owlmo...
.......owlmo....
..oo..owlmo.....
..owooolmo......
...owwlmo.......
...oowmo........
..ohooowo.......
.ohHo..oo.......
oHHo............
oo..............
"""


# ---------------------------------------------------------------- блоки
def noise_base(rng, base, spread, stripes=False):
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = rng.randint(-spread, spread)
            if stripes and y % 4 == 0:
                v -= 8
            c = tuple(max(0, min(255, base[i] + v)) for i in range(3)) + (255,)
            img.putpixel((x, y), c)
    return img


def add_flecks(img, rng, palette, clusters=5):
    cols = [hexrgb(palette[k]) for k in ("d", "m", "l", "w")]
    for _ in range(clusters):
        cx, cy = rng.randint(1, 13), rng.randint(1, 13)
        for dx, dy in [(0, 0), (1, 0), (0, 1), (1, 1), (2, 1), (1, 2)]:
            if rng.random() < 0.8:
                img.putpixel((cx + dx, cy + dy), cols[min(3, (dx + dy + rng.randint(0, 2)) % 4)])
        img.putpixel((cx, cy), hexrgb(palette["w"]))
    return img


def metal_block(palette):
    img = Image.new("RGBA", (16, 16), hexrgb(palette["m"]))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=hexrgb(palette["d"]))
    d.line([1, 1, 14, 1], fill=hexrgb(palette["w"]))
    d.line([1, 1, 1, 14], fill=hexrgb(palette["l"]))
    d.line([2, 14, 14, 14], fill=hexrgb(palette["d"]))
    d.line([14, 2, 14, 14], fill=hexrgb(palette["d"]))
    for i in range(3, 13, 4):
        d.line([3, i, 12, i], fill=hexrgb(palette["l"]))
    return img


def crystal_block(rng):
    img = Image.new("RGBA", (16, 16))
    keys = ["d", "m", "m", "l", "w"]
    for y in range(16):
        for x in range(16):
            k = keys[((x + y) // 3 + (x * 7 + y * 3) % 5 // 3) % len(keys)]
            pal = SAKURA if (x - y) % 11 == 0 else CRYSTAL
            img.putpixel((x, y), hexrgb(pal[k]))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=hexrgb(CRYSTAL["o"]))
    return img


def lamp_block():
    img = Image.new("RGBA", (16, 16), hexrgb(0xEAF8FF))
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, 15, 15], outline=hexrgb(CRYSTAL["d"]))
    d.rectangle([1, 1, 14, 14], outline=hexrgb(CRYSTAL["m"]))
    d.rectangle([4, 4, 11, 11], fill=hexrgb(SAKURA["l"]), outline=hexrgb(SAKURA["m"]))
    d.rectangle([6, 6, 9, 9], fill=hexrgb(0xFFFFFF))
    for p in [(2, 2), (13, 2), (2, 13), (13, 13)]:
        img.putpixel(p, hexrgb(SAKURA["w"]))
    return img


# ---------------------------------------------------------------- GUI-спрайты (весь интерфейс игры в стиле YukiWorld)
def gui_button(w, h, fill, border, top, alpha_fill=215):
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([1, 0, w - 2, h - 1], fill=hexrgb(fill, alpha_fill))
    d.rectangle([0, 1, w - 1, h - 2], fill=hexrgb(fill, alpha_fill))
    # рамка со срезанными углами
    d.line([1, 0, w - 2, 0], fill=hexrgb(border))
    d.line([1, h - 1, w - 2, h - 1], fill=hexrgb(border))
    d.line([0, 1, 0, h - 2], fill=hexrgb(border))
    d.line([w - 1, 1, w - 1, h - 2], fill=hexrgb(border))
    d.line([1, 1, w - 2, 1], fill=hexrgb(top, 140))
    return img


def gui_handle(w, h, a, b):
    img = Image.new("RGBA", (w, h), (0, 0, 0, 0))
    for y in range(h):
        t = y / (h - 1)
        c = tuple(int(hexrgb(a)[i] * (1 - t) + hexrgb(b)[i] * t) for i in range(3)) + (255,)
        for x in range(w):
            img.putpixel((x, y), c)
    d = ImageDraw.Draw(img)
    d.rectangle([0, 0, w - 1, h - 1], outline=hexrgb(0x14284A))
    return img


def nine_slice(w, h, border):
    return {"gui": {"scaling": {"type": "nine_slice", "width": w, "height": h, "border": border}}}


def logo():
    size = 128
    img = Image.new("RGBA", (size, size), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    for r in range(60, 0, -1):
        t = r / 60
        c = tuple(int(hexrgb(0x0B1020)[i] * t + hexrgb(0x3A5FA0)[i] * (1 - t)) for i in range(3)) + (255,)
        d.ellipse([64 - r, 64 - r, 64 + r, 64 + r], fill=c)
    import math
    for k in range(6):
        ang = math.pi / 3 * k
        ex, ey = 64 + 46 * math.cos(ang), 64 + 46 * math.sin(ang)
        d.line([64, 64, ex, ey], fill=hexrgb(0xBFEFFF), width=6)
        for frac in (0.45, 0.7):
            bx, by = 64 + 46 * frac * math.cos(ang), 64 + 46 * frac * math.sin(ang)
            for side in (-1, 1):
                a2 = ang + side * math.pi / 4
                d.line([bx, by, bx + 14 * math.cos(a2), by + 14 * math.sin(a2)], fill=hexrgb(0xFFB3E6), width=4)
    d.ellipse([54, 54, 74, 74], fill=hexrgb(0xFFFFFF))
    return img


def armor_layer(layer):
    rng = random.Random(42 + layer)
    img = Image.new("RGBA", (64, 32), (0, 0, 0, 0))
    regions = ([(0, 0, 32, 16), (16, 16, 40, 32), (40, 16, 56, 32), (0, 16, 16, 32)] if layer == 1
               else [(16, 16, 40, 32), (0, 16, 16, 32)])
    keys = ["d", "m", "m", "l"]
    for (x0, y0, x1, y1) in regions:
        for y in range(y0, y1):
            for x in range(x0, x1):
                k = keys[rng.randint(0, 3)]
                if (x - x0) in (0,) or (y - y0) in (0,):
                    k = "o"
                img.putpixel((x, y), hexrgb(ICE[k]))
    return img


# ---------------------------------------------------------------- данные
BLOCKS = ["yuki_crystal_ore", "deepslate_yuki_crystal_ore", "frost_ore",
          "frost_block", "yuki_crystal_block", "yuki_lamp"]


def ore_loot(block, drop, lo=1, hi=1):
    entries_drop = {"type": "minecraft:item", "name": f"{MOD}:{drop}", "functions": []}
    if hi > 1 or lo != 1:
        entries_drop["functions"].append({"function": "minecraft:set_count",
                                          "count": {"type": "minecraft:uniform", "min": lo, "max": hi}})
    entries_drop["functions"] += [
        {"function": "minecraft:apply_bonus", "enchantment": "minecraft:fortune", "formula": "minecraft:ore_drops"},
        {"function": "minecraft:explosion_decay"}]
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0, "entries": [{
        "type": "minecraft:alternatives", "children": [
            {"type": "minecraft:item", "name": f"{MOD}:{block}", "conditions": [{
                "condition": "minecraft:match_tool", "predicate": {"predicates": {"minecraft:enchantments": [
                    {"enchantments": "minecraft:silk_touch", "levels": {"min": 1}}]}}}]},
            entries_drop]}]}],
        "random_sequence": f"{MOD}:blocks/{block}"}


def self_loot(block):
    return {"type": "minecraft:block", "pools": [{"rolls": 1, "bonus_rolls": 0,
                                                  "entries": [{"type": "minecraft:item", "name": f"{MOD}:{block}"}],
                                                  "conditions": [{"condition": "minecraft:survives_explosion"}]}],
            "random_sequence": f"{MOD}:blocks/{block}"}


def shaped(pattern, key, result, count=1, category="misc"):
    return {"type": "minecraft:crafting_shaped", "category": category, "pattern": pattern,
            "key": {k: {"item": v} if ":" in v and not v.startswith("#") else {"tag": v[1:]}
                    for k, v in key.items()},
            "result": {"id": f"{MOD}:{result}", "count": count}}


def shapeless(ingredients, result, count=1):
    return {"type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [{"item": i} for i in ingredients],
            "result": {"id": f"{MOD}:{result}", "count": count}}


def cooking(kind, ingredient, result, xp, time):
    return {"type": f"minecraft:{kind}", "category": "misc", "ingredient": {"item": ingredient},
            "result": {"id": f"{MOD}:{result}"}, "experience": xp, "cookingtime": time}


I = lambda n: f"{MOD}:{n}"  # noqa: E731

RECIPES = {
    "frost_ingot_from_smelting": cooking("smelting", I("raw_frost"), "frost_ingot", 0.7, 200),
    "frost_ingot_from_blasting": cooking("blasting", I("raw_frost"), "frost_ingot", 0.7, 100),
    "frost_ingot_from_ore_smelting": cooking("smelting", I("frost_ore"), "frost_ingot", 0.7, 200),
    "yuki_crystal_from_ore_smelting": cooking("smelting", I("yuki_crystal_ore"), "yuki_crystal", 1.0, 200),
    "yuki_crystal_from_deepslate_ore_smelting": cooking("smelting", I("deepslate_yuki_crystal_ore"),
                                                        "yuki_crystal", 1.0, 200),
    "frost_block": shaped(["III", "III", "III"], {"I": I("frost_ingot")}, "frost_block", category="building"),
    "frost_ingot_from_block": shapeless([I("frost_block")], "frost_ingot", 9),
    "yuki_crystal_block": shaped(["CCC", "CCC", "CCC"], {"C": I("yuki_crystal")}, "yuki_crystal_block",
                                 category="building"),
    "yuki_crystal_from_block": shapeless([I("yuki_crystal_block")], "yuki_crystal", 9),
    "yuki_lamp": shaped(["GCG", "CLC", "GCG"], {"G": "minecraft:glass", "C": I("yuki_crystal"),
                                                "L": "minecraft:glowstone"}, "yuki_lamp", 4, "building"),
    "yuki_star": shaped(["CIC", "IDI", "CIC"], {"C": I("yuki_crystal"), "I": I("frost_ingot"),
                                                "D": "minecraft:diamond"}, "yuki_star"),
    "frost_apple": shaped([" C ", "CAC", " C "], {"C": I("yuki_crystal"), "A": "minecraft:apple"},
                          "frost_apple"),
    "frost_sword": shaped(["I", "I", "S"], {"I": I("frost_ingot"), "S": "minecraft:stick"},
                          "frost_sword", category="equipment"),
    "frost_pickaxe": shaped(["III", " S ", " S "], {"I": I("frost_ingot"), "S": "minecraft:stick"},
                            "frost_pickaxe", category="equipment"),
    "frost_axe": shaped(["II", "IS", " S"], {"I": I("frost_ingot"), "S": "minecraft:stick"},
                        "frost_axe", category="equipment"),
    "frost_shovel": shaped(["I", "S", "S"], {"I": I("frost_ingot"), "S": "minecraft:stick"},
                           "frost_shovel", category="equipment"),
    "frost_hoe": shaped(["II", " S", " S"], {"I": I("frost_ingot"), "S": "minecraft:stick"},
                        "frost_hoe", category="equipment"),
    "frost_helmet": shaped(["III", "I I"], {"I": I("frost_ingot")}, "frost_helmet", category="equipment"),
    "frost_chestplate": shaped(["I I", "III", "III"], {"I": I("frost_ingot")}, "frost_chestplate",
                               category="equipment"),
    "frost_leggings": shaped(["III", "I I", "I I"], {"I": I("frost_ingot")}, "frost_leggings",
                             category="equipment"),
    "frost_boots": shaped(["I I", "I I"], {"I": I("frost_ingot")}, "frost_boots", category="equipment"),
}

def mods_loaded(mods):
    return [{"type": "neoforge:mod_loaded", "modid": m} for m in mods]


def when(recipe, mods):
    """Рецепт работает, только если все моды загружены."""
    return {"neoforge:conditions": mods_loaded(mods), **recipe}


def unless(recipe, mods):
    """Запасной рецепт, если хотя бы одного мода нет."""
    return {"neoforge:conditions": [{"type": "neoforge:not", "value": {"type": "neoforge:and",
                                                                       "values": mods_loaded(mods)}}],
            **recipe}


TECH_MODS = ["mekanism", "ae2", "create", "powah", "immersiveengineering", "refinedstorage", "oritech",
             "industrialforegoing"]
MAGIC_MODS = ["ars_nouveau", "irons_spellbooks", "occultism", "apothic_enchanting", "evilcraft"]
NATURE_MODS = ["mysticalagriculture", "the_bumblezone", "farmersdelight", "productivebees", "twilightforest",
               "artifacts"]
DIMENSION_MODS = ["aether", "twilightforest"]
HEART_MODS = ["mekanism", "justdirethings"]
STAR_MODS = ["create", "ae2"]

ENDGAME_RECIPES = {
    "frost_core": shaped(["BCB", "CSC", "BCB"], {"B": I("frost_block"), "C": I("yuki_crystal_block"),
                                                 "S": I("yuki_star")}, "frost_core"),
    # --- Технологическое ядро
    "tech_core": when(shaped(["MAP", "EFI", "RON"], {
        "M": "mekanism:ultimate_control_circuit", "A": "ae2:singularity", "P": "create:precision_mechanism",
        "E": "powah:capacitor_nitro", "F": I("frost_core"), "I": "immersiveengineering:component_electronic_adv",
        "R": "refinedstorage:construction_core", "O": "oritech:overcharged_crystal",
        "N": "industrialforegoing:machine_frame_supreme"}, "tech_core"), TECH_MODS),
    "tech_core_fallback": unless(shaped(["RCR", "PFP", "RCR"], {
        "R": "minecraft:redstone_block", "C": "minecraft:comparator", "P": "minecraft:sticky_piston",
        "F": I("frost_core")}, "tech_core"), TECH_MODS),
    # --- Магическое ядро
    "magic_core": when(shaped(["WAD", "OFS", "GEB"], {
        "W": "ars_nouveau:wilden_tribute", "A": "irons_spellbooks:legendary_ink",
        "D": "irons_spellbooks:divine_pearl", "O": "occultism:afrit_essence", "F": I("frost_core"),
        "S": "occultism:spirit_attuned_gem", "G": "apothic_enchanting:infused_breath",
        "E": "evilcraft:dark_gem", "B": "ars_nouveau:source_gem_block"}, "magic_core"), MAGIC_MODS),
    "magic_core_fallback": unless(shaped(["EAE", "LFL", "EAE"], {
        "E": "minecraft:ender_eye", "A": "minecraft:amethyst_block", "L": "minecraft:lapis_block",
        "F": I("frost_core")}, "magic_core"), MAGIC_MODS),
    # --- Ядро природы
    "nature_core": when(shaped(["SBR", "HFJ", "TEW"], {
        "S": "mysticalagriculture:awakened_supremium_ingot", "B": "the_bumblezone:essence_of_the_bees",
        "R": "farmersdelight:rich_soil", "H": "productivebees:wax", "F": I("frost_core"),
        "J": "the_bumblezone:royal_jelly_block", "T": "twilightforest:steeleaf_ingot",
        "E": "artifacts:everlasting_beef", "W": "minecraft:totem_of_undying"}, "nature_core"), NATURE_MODS),
    "nature_core_fallback": unless(shaped(["HGH", "AFA", "HGH"], {
        "H": "minecraft:honey_block", "G": "minecraft:golden_carrot", "A": "minecraft:golden_apple",
        "F": I("frost_core")}, "nature_core"), NATURE_MODS),
    # --- Ядро измерений
    "dimension_core": when(shaped(["NGK", "TFC", "EHW"], {
        "N": "minecraft:nether_star", "G": "aether:enchanted_gravitite", "K": "twilightforest:knightmetal_ingot",
        "T": "twilightforest:fiery_ingot", "F": I("frost_core"), "C": "twilightforest:carminite",
        "E": "minecraft:echo_shard", "H": "minecraft:heavy_core", "W": "minecraft:dragon_breath"},
        "dimension_core"), DIMENSION_MODS),
    "dimension_core_fallback": unless(shaped(["NOE", "HFH", "EON"], {
        "N": "minecraft:nether_star", "O": "minecraft:crying_obsidian", "E": "minecraft:echo_shard",
        "H": "minecraft:heavy_core", "F": I("frost_core")}, "dimension_core"), DIMENSION_MODS),
    # --- Сердце YukiWorld
    "yuki_heart": when(shaped(["UTU", "MAN", "UDU"], {
        "U": "justdirethings:time_crystal", "T": I("tech_core"), "M": I("magic_core"), "N": I("nature_core"),
        "D": I("dimension_core"), "A": "mekanism:pellet_antimatter"}, "yuki_heart"), HEART_MODS),
    "yuki_heart_fallback": unless(shaped(["UTU", "MAN", "UDU"], {
        "U": "minecraft:netherite_ingot", "T": I("tech_core"), "M": I("magic_core"), "N": I("nature_core"),
        "D": I("dimension_core"), "A": "minecraft:nether_star"}, "yuki_heart"), HEART_MODS),
    # --- Бесконечная звезда Юки (механический крафтер Create 5x5)
    "yuki_infinity_star": when({
        "type": "create:mechanical_crafting", "accept_mirrored": False, "category": "misc",
        "key": {"R": {"item": "create:refined_radiance"}, "S": {"item": "create:shadow_steel"},
                "X": {"item": I("yuki_star")}, "Q": {"item": "ae2:singularity"}, "H": {"item": I("yuki_heart")}},
        "pattern": ["  R  ", " SXS ", "RQHQR", " SXS ", "  R  "],
        "result": {"count": 1, "id": I("yuki_infinity_star")}, "show_notification": True}, STAR_MODS),
    "yuki_infinity_star_fallback": unless(shaped(["SNS", "NHN", "SNS"], {
        "S": I("yuki_star"), "N": "minecraft:nether_star", "H": I("yuki_heart")}, "yuki_infinity_star"),
        STAR_MODS),
    "yuki_blade": shaped([" H ", "HSH", " N "], {"H": I("yuki_heart"), "S": I("frost_sword"),
                                                 "N": "minecraft:netherite_sword"}, "yuki_blade",
                         category="equipment"),
}
RECIPES.update(ENDGAME_RECIPES)

LANG_EN = {
    "item.yukicore.frost_core": "Frost Core",
    "item.yukicore.frost_core.desc": "The heart of every YukiWorld core",
    "item.yukicore.tech_core": "Tech Core",
    "item.yukicore.tech_core.desc": "Mekanism, AE2, Create, Powah, IE, RS, Oritech, Industrial Foregoing",
    "item.yukicore.magic_core": "Magic Core",
    "item.yukicore.magic_core.desc": "Ars Nouveau, Iron's Spells, Occultism, Apotheosis, EvilCraft",
    "item.yukicore.nature_core": "Nature Core",
    "item.yukicore.nature_core.desc": "Mystical Agriculture, Bumblezone, Farmer's Delight, Productive Bees, Twilight Forest",
    "item.yukicore.dimension_core": "Dimension Core",
    "item.yukicore.dimension_core.desc": "Nether, End, Ancient City, Trial Chambers, Aether, Twilight Forest",
    "item.yukicore.yuki_heart": "Heart of YukiWorld",
    "item.yukicore.yuki_heart.desc": "All four cores fused with antimatter and time",
    "item.yukicore.yuki_infinity_star": "Yuki Infinity Star",
    "item.yukicore.yuki_infinity_star.desc": "The final item of YukiWorld",
    "item.yukicore.yuki_infinity_star.desc2": "In inventory: flight, night vision, resistance, saturation",
    "item.yukicore.yuki_blade": "Yuki Blade",
    "itemGroup.yukicore": "YukiWorld",
    "item.yukicore.yuki_crystal": "Yuki Crystal",
    "item.yukicore.raw_frost": "Raw Frost",
    "item.yukicore.frost_ingot": "Frost Ingot",
    "item.yukicore.yuki_star": "Yuki Star",
    "item.yukicore.yuki_star.desc": "Right-click: night vision, speed, jump boost & regeneration",
    "item.yukicore.yuki_star.desc2": "Cooldown: 2 minutes",
    "item.yukicore.frost_apple": "Frost Apple",
    "item.yukicore.frost_sword": "Frost Sword",
    "item.yukicore.frost_sword.desc": "Slows enemies on hit",
    "item.yukicore.frost_pickaxe": "Frost Pickaxe",
    "item.yukicore.frost_axe": "Frost Axe",
    "item.yukicore.frost_shovel": "Frost Shovel",
    "item.yukicore.frost_hoe": "Frost Hoe",
    "item.yukicore.frost_helmet": "Frost Helmet",
    "item.yukicore.frost_chestplate": "Frost Chestplate",
    "item.yukicore.frost_leggings": "Frost Leggings",
    "item.yukicore.frost_boots": "Frost Boots",
    "block.yukicore.yuki_crystal_ore": "Yuki Crystal Ore",
    "block.yukicore.deepslate_yuki_crystal_ore": "Deepslate Yuki Crystal Ore",
    "block.yukicore.frost_ore": "Frost Ore",
    "block.yukicore.frost_block": "Block of Frost",
    "block.yukicore.yuki_crystal_block": "Block of Yuki Crystal",
    "block.yukicore.yuki_lamp": "Yuki Lamp",
    "hud.yukicore.biome": "Biome",
    "hud.yukicore.day": "Day %s",
    "menu.yukicore.mods_loaded": "%s mods loaded",
    "menu.yukicore.subtitle": "a cozy kitchen-sink modpack by YukiTo",
    "yukicore.configuration.title": "YukiCore Settings",
    "yukicore.configuration.gui": "Interface",
    "yukicore.configuration.performance": "Performance",
    "yukicore.configuration.customTitleScreen": "YukiWorld main menu",
    "yukicore.configuration.snowfall": "Menu snowfall",
    "yukicore.configuration.showHud": "Show HUD",
    "yukicore.configuration.hudShowBiome": "Show biome in HUD",
    "yukicore.configuration.smartFps": "Smart FPS (limit in background)",
    "yukicore.configuration.unfocusedFps": "Background FPS limit",
}

LANG_RU = {
    "item.yukicore.frost_core": "Морозное ядро",
    "item.yukicore.frost_core.desc": "Основа всех ядер YukiWorld",
    "item.yukicore.tech_core": "Технологическое ядро",
    "item.yukicore.tech_core.desc": "Mekanism, AE2, Create, Powah, IE, RS, Oritech, Industrial Foregoing",
    "item.yukicore.magic_core": "Магическое ядро",
    "item.yukicore.magic_core.desc": "Ars Nouveau, Iron's Spells, Occultism, Apotheosis, EvilCraft",
    "item.yukicore.nature_core": "Ядро природы",
    "item.yukicore.nature_core.desc": "Mystical Agriculture, Bumblezone, Farmer's Delight, Productive Bees, Twilight Forest",
    "item.yukicore.dimension_core": "Ядро измерений",
    "item.yukicore.dimension_core.desc": "Незер, Энд, Древний город, Испытания, Aether, Сумеречный лес",
    "item.yukicore.yuki_heart": "Сердце YukiWorld",
    "item.yukicore.yuki_heart.desc": "Четыре ядра, сплавленные антиматерией и временем",
    "item.yukicore.yuki_infinity_star": "Бесконечная звезда Юки",
    "item.yukicore.yuki_infinity_star.desc": "Финальный предмет YukiWorld",
    "item.yukicore.yuki_infinity_star.desc2": "В инвентаре: полёт, ночное зрение, сопротивление, насыщение",
    "item.yukicore.yuki_blade": "Клинок Юки",
    "itemGroup.yukicore": "YukiWorld",
    "item.yukicore.yuki_crystal": "Кристалл Юки",
    "item.yukicore.raw_frost": "Необработанный мороз",
    "item.yukicore.frost_ingot": "Морозный слиток",
    "item.yukicore.yuki_star": "Звезда Юки",
    "item.yukicore.yuki_star.desc": "ПКМ: ночное зрение, скорость, прыгучесть и регенерация",
    "item.yukicore.yuki_star.desc2": "Перезарядка: 2 минуты",
    "item.yukicore.frost_apple": "Морозное яблоко",
    "item.yukicore.frost_sword": "Морозный меч",
    "item.yukicore.frost_sword.desc": "Замедляет врагов при ударе",
    "item.yukicore.frost_pickaxe": "Морозная кирка",
    "item.yukicore.frost_axe": "Морозный топор",
    "item.yukicore.frost_shovel": "Морозная лопата",
    "item.yukicore.frost_hoe": "Морозная мотыга",
    "item.yukicore.frost_helmet": "Морозный шлем",
    "item.yukicore.frost_chestplate": "Морозный нагрудник",
    "item.yukicore.frost_leggings": "Морозные поножи",
    "item.yukicore.frost_boots": "Морозные ботинки",
    "block.yukicore.yuki_crystal_ore": "Руда кристалла Юки",
    "block.yukicore.deepslate_yuki_crystal_ore": "Глубинная руда кристалла Юки",
    "block.yukicore.frost_ore": "Морозная руда",
    "block.yukicore.frost_block": "Морозный блок",
    "block.yukicore.yuki_crystal_block": "Блок кристалла Юки",
    "block.yukicore.yuki_lamp": "Лампа Юки",
    "hud.yukicore.biome": "Биом",
    "hud.yukicore.day": "День %s",
    "menu.yukicore.mods_loaded": "Загружено модов: %s",
    "menu.yukicore.subtitle": "уютная сборка от YukiTo",
    "yukicore.configuration.title": "Настройки YukiCore",
    "yukicore.configuration.gui": "Интерфейс",
    "yukicore.configuration.performance": "Производительность",
    "yukicore.configuration.customTitleScreen": "Меню YukiWorld",
    "yukicore.configuration.snowfall": "Снегопад в меню",
    "yukicore.configuration.showHud": "Показывать HUD",
    "yukicore.configuration.hudShowBiome": "Биом в HUD",
    "yukicore.configuration.smartFps": "Smart FPS (лимит в фоне)",
    "yukicore.configuration.unfocusedFps": "Лимит FPS в фоне",
}


def ore_feature(name, targets, size):
    return {"type": "minecraft:ore", "config": {"size": size, "discard_chance_on_air_exposure": 0.0,
                                                "targets": [{"target": {"predicate_type": "minecraft:tag_match",
                                                                        "tag": tag},
                                                             "state": {"Name": f"{MOD}:{block}"}}
                                                            for tag, block in targets]}}


def placed(feature, count, lo, hi, shape="trapezoid"):
    return {"feature": f"{MOD}:{feature}", "placement": [
        {"type": "minecraft:count", "count": count},
        {"type": "minecraft:in_square"},
        {"type": "minecraft:height_range", "height": {"type": f"minecraft:{shape}",
                                                       "min_inclusive": {"absolute": lo},
                                                       "max_inclusive": {"absolute": hi}}},
        {"type": "minecraft:biome"}]}


def main():
    rng = random.Random(1337)

    # Предметы
    for name, (art, pal) in ART.items():
        write(ASSETS / "textures" / "item" / f"{name}.png", sprite(art, pal))
        parent = "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated"
        write(ASSETS / "models" / "item" / f"{name}.json",
              {"parent": parent, "textures": {"layer0": f"{MOD}:item/{name}"}})

    # Эндгейм
    endgame = {name: orb(pal) for name, pal in CORE_PALETTES.items()}
    endgame["yuki_heart"] = heart()
    endgame["yuki_infinity_star"] = rainbow_star()
    endgame["yuki_blade"] = sprite(BLADE_ART, SAKURA)
    for name, img in endgame.items():
        write(ASSETS / "textures" / "item" / f"{name}.png", img)
        parent = "minecraft:item/handheld" if name in HANDHELD else "minecraft:item/generated"
        write(ASSETS / "models" / "item" / f"{name}.json",
              {"parent": parent, "textures": {"layer0": f"{MOD}:item/{name}"}})

    # Блоки
    stone, deepslate = (125, 125, 125), (72, 72, 80)
    tex = {
        "yuki_crystal_ore": add_flecks(noise_base(rng, stone, 10), rng, CRYSTAL, 4),
        "deepslate_yuki_crystal_ore": add_flecks(noise_base(rng, deepslate, 8, True), rng, CRYSTAL, 4),
        "frost_ore": add_flecks(noise_base(rng, stone, 10), rng, RAW, 5),
        "frost_block": metal_block(ICE),
        "yuki_crystal_block": crystal_block(rng),
        "yuki_lamp": lamp_block(),
    }
    for name in BLOCKS:
        write(ASSETS / "textures" / "block" / f"{name}.png", tex[name])
        write(ASSETS / "blockstates" / f"{name}.json", {"variants": {"": {"model": f"{MOD}:block/{name}"}}})
        write(ASSETS / "models" / "block" / f"{name}.json",
              {"parent": "minecraft:block/cube_all", "textures": {"all": f"{MOD}:block/{name}"}})
        write(ASSETS / "models" / "item" / f"{name}.json", {"parent": f"{MOD}:block/{name}"})

    # Броня на игроке
    write(ASSETS / "textures" / "models" / "armor" / "frost_layer_1.png", armor_layer(1))
    write(ASSETS / "textures" / "models" / "armor" / "frost_layer_2.png", armor_layer(2))

    # GUI: перекрашиваем ванильные виджеты во всей игре
    mc_gui = ROOT / "assets" / "minecraft" / "textures" / "gui" / "sprites" / "widget"
    for fname, img, meta in [
        ("button", gui_button(200, 20, 0x111A2E, 0x4F7FB8, 0x8FD8FF), nine_slice(200, 20, 3)),
        ("button_highlighted", gui_button(200, 20, 0x22385E, 0xBFEFFF, 0xFFB3E6, 235), nine_slice(200, 20, 3)),
        ("button_disabled", gui_button(200, 20, 0x15181F, 0x30343C, 0x30343C, 180), nine_slice(200, 20, 3)),
        ("slider", gui_button(200, 20, 0x0C1222, 0x3A5A86, 0x3A5A86), nine_slice(200, 20, 3)),
        ("slider_highlighted", gui_button(200, 20, 0x14203A, 0x8FD8FF, 0x8FD8FF), nine_slice(200, 20, 3)),
        ("slider_handle", gui_handle(8, 20, 0x8FD8FF, 0x3A7BD5), nine_slice(8, 20, 2)),
        ("slider_handle_highlighted", gui_handle(8, 20, 0xFFE6F7, 0xFF9EDC), nine_slice(8, 20, 2)),
        ("text_field", gui_button(200, 20, 0x05080F, 0x3A5A86, 0x05080F, 235), nine_slice(200, 20, 1)),
        ("text_field_highlighted", gui_button(200, 20, 0x05080F, 0x8FD8FF, 0x05080F, 235), nine_slice(200, 20, 1)),
    ]:
        write(mc_gui / f"{fname}.png", img)
        write(mc_gui / f"{fname}.png.mcmeta", meta)

    write(ROOT / "yukicore_logo.png", logo())

    # Локализация
    write(ASSETS / "lang" / "en_us.json", LANG_EN)
    write(ASSETS / "lang" / "ru_ru.json", LANG_RU)

    # Loot tables
    lt = DATA / MOD / "loot_table" / "blocks"
    write(lt / "yuki_crystal_ore.json", ore_loot("yuki_crystal_ore", "yuki_crystal"))
    write(lt / "deepslate_yuki_crystal_ore.json", ore_loot("deepslate_yuki_crystal_ore", "yuki_crystal"))
    write(lt / "frost_ore.json", ore_loot("frost_ore", "raw_frost"))
    for b in ("frost_block", "yuki_crystal_block", "yuki_lamp"):
        write(lt / f"{b}.json", self_loot(b))

    # Рецепты
    for name, recipe in RECIPES.items():
        write(DATA / MOD / "recipe" / f"{name}.json", recipe)

    # Теги блоков
    tags = DATA / "minecraft" / "tags"
    write(tags / "block" / "mineable" / "pickaxe.json", {"values": [I(b) for b in BLOCKS]})
    write(tags / "block" / "needs_iron_tool.json",
          {"values": [I("yuki_crystal_ore"), I("deepslate_yuki_crystal_ore"), I("yuki_crystal_block")]})
    write(tags / "block" / "needs_stone_tool.json", {"values": [I("frost_ore"), I("frost_block")]})
    write(tags / "block" / "beacon_base_blocks.json", {"values": [I("frost_block"), I("yuki_crystal_block")]})

    # Теги предметов (зачарования, совместимость)
    for tag, items in {"swords": ["frost_sword", "yuki_blade"], "pickaxes": ["frost_pickaxe"], "axes": ["frost_axe"],
                       "shovels": ["frost_shovel"], "hoes": ["frost_hoe"], "head_armor": ["frost_helmet"],
                       "chest_armor": ["frost_chestplate"], "leg_armor": ["frost_leggings"],
                       "foot_armor": ["frost_boots"], "beacon_payment_items": ["frost_ingot"]}.items():
        write(tags / "item" / f"{tag}.json", {"values": [I(i) for i in items]})

    common = DATA / "c" / "tags"
    write(common / "item" / "ingots.json", {"values": [I("frost_ingot")]})
    write(common / "item" / "ingots" / "frost.json", {"values": [I("frost_ingot")]})
    write(common / "item" / "gems.json", {"values": [I("yuki_crystal")]})
    write(common / "item" / "gems" / "yuki_crystal.json", {"values": [I("yuki_crystal")]})
    write(common / "item" / "raw_materials.json", {"values": [I("raw_frost")]})
    ores = [I("yuki_crystal_ore"), I("deepslate_yuki_crystal_ore"), I("frost_ore")]
    write(common / "block" / "ores.json", {"values": ores})
    write(common / "item" / "ores.json", {"values": ores})

    # Генерация мира
    wg = DATA / MOD / "worldgen"
    write(wg / "configured_feature" / "ore_yuki_crystal.json", ore_feature("ore_yuki_crystal", [
        ("minecraft:stone_ore_replaceables", "yuki_crystal_ore"),
        ("minecraft:deepslate_ore_replaceables", "deepslate_yuki_crystal_ore")], 5))
    write(wg / "configured_feature" / "ore_frost.json", ore_feature("ore_frost", [
        ("minecraft:stone_ore_replaceables", "frost_ore"),
        ("minecraft:deepslate_ore_replaceables", "frost_ore")], 9))
    write(wg / "placed_feature" / "ore_yuki_crystal.json", placed("ore_yuki_crystal", 5, -64, 32))
    write(wg / "placed_feature" / "ore_frost.json", placed("ore_frost", 12, -32, 96))
    bm = DATA / MOD / "neoforge" / "biome_modifier"
    for f in ("ore_yuki_crystal", "ore_frost"):
        write(bm / f"{f}.json", {"type": "neoforge:add_features", "biomes": "#minecraft:is_overworld",
                                 "features": f"{MOD}:{f}", "step": "underground_ores"})

    write(ROOT / "pack.mcmeta", {"pack": {"description": "YukiCore resources", "pack_format": 34}})
    print("Assets generated in", ROOT)


if __name__ == "__main__":
    main()
