"""YukiCore 7.2: Казино Синтары — только игровые фишки (никаких реальных денег)."""
import math

from PIL import Image, ImageDraw

from . import hd
from .pixel import sprite
from .recipes import I, shaped, shapeless

GOLDP = {"o": 0x3A1A04, "d": 0xB0700C, "m": 0xF2C030, "l": 0xFFE68A, "w": 0xFFFFFF}
REDP = {"o": 0x3A0606, "d": 0xA0140C, "m": 0xE8401A, "l": 0xFF9A5A, "w": 0xFFF4D0}

CHIP = """
................
.....oooooo.....
...oommmmmmoo...
..odmwmmmmwmdo..
..omllllllllmo..
.omlmmwwwwmmlmo.
.omlmwmllmwmlmo.
.omlmwlmmlwmlmo.
.omlmwlmmlwmlmo.
.omlmwmllmwmlmo.
.omlmmwwwwmmlmo.
..omllllllllmo..
..odmwmmmmwmdo..
...oommmmmmoo...
.....oooooo.....
"""

DICE = """
................
................
..oooooooooo....
..owwwwwwwwlo...
..owdwwwwwwlo...
..owwwwwwwwlo...
..owwwwdwwwlo...
..owwwwwwwwlo...
..owwwwwwdwlo...
..owwwwwwwwlo...
..olllllllllo...
..oooooooooo....
"""

BOX = """
................
................
..oooooooooooo..
..ommmmmmmmmmo..
..odddddddddo...
..ollmmllmmllo..
..oooooddoooooo.
..olmmolloommlo.
..olmmolloommlo.
..olmmmoommmmlo.
..olmmmmmmmmmlo.
..oddddddddddo..
..oooooooooooo..
"""

SEVEN = """
................
..oooooooooooo..
..owwwwwwwwwlo..
..olllllllllmo..
..oooooooolmo...
.......olmo.....
......olmo......
.....olmo.......
.....olmo.......
....olmo........
....olmo........
....oddo........
....oooo........
"""


def item_textures():
    new = {
        "sintara_chip": (sprite(CHIP, REDP), False, 1),
        "lucky_dice": (sprite(DICE, {"o": 0x2A0A04, "w": 0xFFF4E8, "l": 0xD8C8B8, "d": 0xC0140C}), False, 0),
        "fortune_box": (sprite(BOX, REDP), False, 4),
        "lucky_seven": (sprite(SEVEN, GOLDP), False, 6),
    }
    return {n: (hd.hd_item(img, seed=800 + i, sparkles=sp), hh) for i, (n, (img, hh, sp)) in enumerate(new.items())}


# ------------------------------------------------------------------ символы барабанов (32×32)
def symbols():
    out = {}

    def base():
        img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
        return img, ImageDraw.Draw(img)
    img, d = base()
    d.ellipse([5, 7, 27, 29], fill=(255, 140, 30, 255), outline=(150, 60, 10, 255))
    d.ellipse([9, 10, 15, 15], fill=(255, 210, 140, 255))
    d.polygon([(16, 7), (22, 2), (24, 6)], fill=(60, 160, 60, 255))
    out["tangerine"] = img
    img, d = base()
    for k in range(6):
        a = k * math.pi / 3
        d.line([16, 16, 16 + math.cos(a) * 13, 16 + math.sin(a) * 13], fill=(60, 150, 230, 255), width=3)
    d.ellipse([12, 12, 20, 20], fill=(170, 230, 255, 255))
    out["snowflake"] = img
    img, d = base()
    d.polygon([(16, 2), (26, 16), (24, 26), (16, 30), (8, 26), (6, 16)], fill=(240, 80, 20, 255))
    d.polygon([(16, 12), (21, 20), (16, 28), (11, 20)], fill=(255, 220, 90, 255))
    out["flame"] = img
    img, d = base()
    d.polygon([(16, 2), (27, 12), (16, 30), (5, 12)], fill=(110, 200, 255, 255), outline=(30, 80, 170, 255))
    d.polygon([(16, 2), (21, 12), (16, 30)], fill=(200, 240, 255, 255))
    out["crystal"] = img
    img, d = base()
    pts = [(16 + math.cos(-math.pi / 2 + k * math.pi / 5) * (14 if k % 2 == 0 else 6),
            17 + math.sin(-math.pi / 2 + k * math.pi / 5) * (14 if k % 2 == 0 else 6)) for k in range(10)]
    d.polygon(pts, fill=(255, 210, 50, 255), outline=(170, 110, 10, 255))
    out["star"] = img
    img, d = base()
    d.polygon([(6, 4), (27, 4), (27, 9), (16, 29), (10, 29), (20, 10), (6, 10)], fill=(220, 20, 20, 255),
              outline=(255, 210, 60, 255))
    out["seven"] = img
    return out


# ------------------------------------------------------------------ автомат (блок)
def machine_textures():
    S = 64
    front = Image.new("RGBA", (S, S), (150, 20, 10, 255))
    d = ImageDraw.Draw(front)
    d.rectangle([0, 0, S - 1, S - 1], outline=(255, 200, 60, 255), width=3)
    d.rectangle([6, 8, 57, 30], fill=(40, 6, 4, 255), outline=(255, 210, 80, 255), width=2)
    for i, col in enumerate([(220, 20, 20), (255, 210, 50), (220, 20, 20)]):
        x = 10 + i * 16
        d.rectangle([x, 11, x + 12, 27], fill=(250, 240, 220, 255))
        d.text((x + 3, 12), "7", fill=col + (255,))
    for i in range(7):
        d.ellipse([5 + i * 8, 2, 8 + i * 8, 5], fill=(255, 230, 120, 255) if i % 2 == 0 else (255, 120, 40, 255))
    d.rectangle([22, 36, 41, 40], fill=(30, 10, 6, 255))
    d.rectangle([10, 46, 53, 58], fill=(200, 140, 30, 255), outline=(255, 230, 120, 255))
    side = Image.new("RGBA", (S, S), (130, 16, 8, 255))
    d = ImageDraw.Draw(side)
    for y in range(4, S, 12):
        d.rectangle([0, y, S - 1, y + 3], fill=(240, 180, 40, 255))
    d.rectangle([0, 0, S - 1, S - 1], outline=(255, 200, 60, 255), width=3)
    top = Image.new("RGBA", (S, S), (240, 180, 40, 255))
    d = ImageDraw.Draw(top)
    d.rectangle([0, 0, S - 1, S - 1], outline=(150, 20, 10, 255), width=4)
    return {"slot_machine_front": front, "slot_machine_side": side, "slot_machine_top": top}


def machine_model():
    B = "yukicore:block/"
    faces_body = {"north": {"texture": "#front"}, "south": {"texture": "#side"}, "east": {"texture": "#side"},
                  "west": {"texture": "#side"}, "up": {"texture": "#top"}, "down": {"texture": "#top"}}
    return {"parent": "minecraft:block/block", "textures": {"front": B + "slot_machine_front", "side": B + "slot_machine_side",
                                                             "top": B + "slot_machine_top", "particle": B + "slot_machine_side"},
            "elements": [
                {"from": [1, 0, 1], "to": [15, 16, 15], "faces": faces_body},
                {"from": [15, 6, 7], "to": [16.5, 8, 9], "faces": {d: {"texture": "#top"} for d in
                                                                   ("north", "south", "east", "west", "up", "down")}},
                {"from": [16, 8, 7.5], "to": [17, 14, 8.5], "faces": {d: {"texture": "#side"} for d in
                                                                     ("north", "south", "east", "west", "up", "down")}},
                {"from": [15.5, 14, 7], "to": [17.5, 16, 9], "faces": {d: {"texture": "#front"} for d in
                                                                      ("north", "south", "east", "west", "up", "down")}}]}


def blockstate():
    return {"variants": {f"facing={f}": {"model": "yukicore:block/slot_machine", "y": y}
                         for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}}


# ------------------------------------------------------------------ рецепты и лут
def recipes():
    return {
        "sintara_chip": shapeless(["minecraft:gold_ingot", I("flame_essence")], I("sintara_chip"), 8),
        "sintara_chip_from_emerald": shapeless(["minecraft:emerald", "minecraft:redstone"], I("sintara_chip"), 2),
        "slot_machine": shaped(["GCG", "RXR", "GLG"], {"G": "minecraft:gold_block", "C": I("sintara_chip"),
                                                       "R": "minecraft:redstone_block", "X": "minecraft:glass",
                                                       "L": "minecraft:lever"}, I("slot_machine")),
        "lucky_dice": shaped([" C ", "CBC", " C "], {"C": I("sintara_chip"), "B": "minecraft:bone_block"}, I("lucky_dice")),
        "fortune_box": shaped(["CCC", "CXC", "CCC"], {"C": I("sintara_chip"), "X": "minecraft:chest"}, I("fortune_box")),
    }


def _table(rolls, pool):
    return {"type": "minecraft:gift", "pools": [{"rolls": rolls, "bonus_rolls": 0, "entries": [
        {"type": "minecraft:item", "name": it, "weight": w, "functions": [
            {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": a, "max": b}}]}
        for it, w, a, b in pool]}]}


LOOT = {
    "fortune_common": _table(2, [("minecraft:iron_ingot", 20, 4, 12), ("minecraft:gold_ingot", 15, 3, 8),
                                 ("minecraft:diamond", 8, 1, 3), ("minecraft:emerald", 12, 2, 6), (I("sintara_chip"), 20, 4, 12),
                                 (I("tangerine"), 10, 3, 8), (I("mana_potion"), 10, 1, 3), ("minecraft:experience_bottle", 10, 4, 10)]),
    "fortune_rare": _table(3, [("minecraft:diamond", 15, 3, 6), ("minecraft:netherite_scrap", 6, 1, 2),
                               (I("frost_essence"), 12, 4, 8), (I("flame_essence"), 12, 4, 8), (I("sintara_chip"), 15, 12, 24),
                               (I("greater_mana_potion"), 8, 1, 3), (I("yuki_crystal"), 10, 4, 10), ("minecraft:golden_apple", 6, 1, 3)]),
    "fortune_epic": _table(3, [(I("mana_crystal"), 10, 1, 2), (I("star_shard"), 6, 1, 1), (I("greater_flame_essence"), 10, 2, 4),
                               (I("greater_frost_essence"), 10, 2, 4), (I("sintara_chip"), 12, 32, 64),
                               ("minecraft:netherite_ingot", 5, 1, 1), ("minecraft:enchanted_golden_apple", 4, 1, 1),
                               (I("grand_gift"), 8, 1, 2)]),
    "fortune_legendary": _table(2, [(I("lucky_seven"), 10, 1, 1), (I("tome_of_flame"), 10, 1, 1), (I("tome_of_winter"), 8, 1, 1),
                                    ("minecraft:totem_of_undying", 8, 1, 1), ("minecraft:elytra", 4, 1, 1),
                                    (I("star_shard"), 10, 2, 4), (I("sintara_chip"), 10, 64, 64), ("minecraft:nether_star", 4, 1, 1)]),
}

BOSS_EXTRA = {b: [("sintara_chip", 16, 48)] for b in ("frost_colossus", "blizzard_queen", "ash_lord", "void_wyrm",
                                                      "shadow_of_yuki", "glacial_archon", "infernal_phoenix")}

LANG = {
    "item.yukicore.sintara_chip": ("Фишка Синтары", "Sintara Chip"),
    "item.yukicore.sintara_chip.desc": ("Игровая фишка казино Синтары. Ставь в Огненный автомат или Кости",
                                        "Casino chip for the Fire Slot Machine and Lucky Dice"),
    "item.yukicore.lucky_dice": ("Кости Синтары", "Sintara's Lucky Dice"),
    "item.yukicore.lucky_dice.desc": ("ПКМ: ставка 1 фишка. 7 или 11 — ×2, дубль — возврат, 6+6 — ×10",
                                      "Use: bet 1 chip. 7 or 11 — ×2, doubles — refund, 6+6 — ×10"),
    "item.yukicore.fortune_box": ("Огненный сундук удачи", "Fiery Fortune Box"),
    "item.yukicore.fortune_box.desc": ("ПКМ: обычное, редкое (25%), эпическое (12%) или легендарное (3%)",
                                       "Use: common, rare (25%), epic (12%) or legendary (3%)"),
    "item.yukicore.lucky_seven": ("Огненная семёрка Синтары", "Sintara's Lucky Seven"),
    "item.yukicore.lucky_seven.desc": ("Трофей джекпота 777. В инвентаре: +15% к огненной магии",
                                       "Jackpot 777 trophy. In inventory: +15% fire power"),
    "block.yukicore.slot_machine": ("Огненный автомат Синтары", "Sintara's Fire Slot Machine"),
    "block.yukicore.slot_machine.desc": ("ПКМ — играть на фишки. Джекпот 777 — ×200!", "Use to play for chips. Jackpot 777 — ×200!"),
    "casino.yukicore.title": ("Огненный автомат Синтары", "Sintara's Fire Slot Machine"),
    "casino.yukicore.spin": ("КРУТИТЬ", "SPIN"),
    "casino.yukicore.bet": ("Ставка: %s фиш.", "Bet: %s chips"),
    "casino.yukicore.chips": ("Фишки: %s", "Chips: %s"),
    "casino.yukicore.win": ("Выигрыш: %s фишек!", "You win %s chips!"),
    "casino.yukicore.lose": ("Мимо… ещё разок?", "No luck… again?"),
    "casino.yukicore.jackpot": ("★ ДЖЕКПОТ 777! +%s фишек ★", "★ JACKPOT 777! +%s chips ★"),
    "casino.yukicore.jackpot_all": ("★ %s сорвал ДЖЕКПОТ у Синтары: %s фишек!", "★ %s hit Sintara's JACKPOT: %s chips!"),
    "casino.yukicore.table1": ("Три в ряд: 777 ×200 • звёзды ×50 + сундук удачи • кристаллы ×25",
                               "Three in a row: 777 ×200 • stars ×50 + fortune box • crystals ×25"),
    "casino.yukicore.table1b": ("пламя ×12 • снежинки ×8 • мандарины ×5", "flames ×12 • snowflakes ×8 • tangerines ×5"),
    "casino.yukicore.table2": ("Пара: 77 ×10 • звёзды ×3 • кристаллы ×2 • пламя/снежинки ×1", "Pair: 77 ×10 • stars ×3 • crystals ×2 • flames/snowflakes ×1"),
    "casino.yukicore.fair": ("Только игровые фишки • возврат ~93–99% в зависимости от игры",
                             "In-game chips only • ~93–99% return depending on the game"),
    "casino.yukicore.no_chips": ("Не хватает фишек Синтары", "Not enough Sintara chips"),
    "casino.yukicore.dice": ("Кости: %s и %s — выигрыш: %s фиш.", "Dice: %s and %s — win: %s chips"),
    "casino.yukicore.box.common": ("Сундук удачи: обычная награда", "Fortune box: common"),
    "casino.yukicore.box.rare": ("Сундук удачи: ✦ редкая награда!", "Fortune box: ✦ rare!"),
    "casino.yukicore.box.epic": ("Сундук удачи: ✦✦ ЭПИЧЕСКАЯ награда!", "Fortune box: ✦✦ EPIC!"),
    "casino.yukicore.box.legendary": ("Сундук удачи: ★ ЛЕГЕНДАРНАЯ награда! ★", "Fortune box: ★ LEGENDARY! ★"),
}


def lang():
    ru, en = {}, {}
    for k, (r, e) in LANG.items():
        ru[k], en[k] = r, e
    return ru, en


ENTRIES = [
    ("sintara", "casino", "Казино Синтары", "yukicore:slot_machine", [
        "$(o)Синтара обожает азарт. «Огонь — это всегда немного удачи», — говорит она.$()$(br2)Казино работает только "
        "на $(l)игровые фишки$(): их дают за квесты, с боссов и из золота с эссенцией пламени.",
        ("spot", "yukicore:slot_machine", "$(l)Огненный автомат$(): пять игр — слоты, рулетка, «выше/ниже», «21» против "
                                          "Синтары и бонусное колесо. Ставки от 1 до 100 фишек. 777 — джекпот ×200."),
        "$(l)Бонусы$(): 100 фишек при первом визите, Колесо удачи раз в сутки (до 500 фишек, +10% за каждый день "
        "серии), три кристалла в слотах — 5 бесплатных прокруток с двойным выигрышем.",
        ("spot", "yukicore:lucky_dice", "$(l)Кости$(): 1 фишка. 7 или 11 — ×2, дубль — возврат, две шестёрки — ×10."),
        ("spot", "yukicore:fortune_box", "$(l)Сундук удачи$(): 8 фишек + сундук. Внутри обычная, редкая, эпическая или "
                                         "легендарная награда (элитры, тотем, тома знаний, Огненная семёрка)."),
    ]),
]
