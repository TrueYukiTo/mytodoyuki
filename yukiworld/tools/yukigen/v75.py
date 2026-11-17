"""YukiCore 7.5 «Охота и вторжения»: элитные мобы, Ночь Пустоты, Доска Синтары (задания и лавка)."""
from PIL import Image, ImageDraw

from . import hd
from .recipes import I, shaped

PURPLE, PURPLE_L, DARK, GOLD, WHITE = (120, 50, 200), (210, 160, 255), (30, 15, 40), (255, 205, 60), (255, 255, 255)


def _c(rgb):
    return tuple(rgb) + (255,)


def item_textures():
    out = {}
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.polygon([(8, 1), (13, 6), (11, 15), (5, 15), (3, 6)], fill=_c((200, 40, 40)), outline=_c((80, 0, 0)))
    d.polygon([(8, 1), (10, 6), (8, 15)], fill=_c((255, 140, 120)))
    d.line([4, 8, 12, 8], fill=_c(GOLD))
    out["champion_shard"] = (hd.hd_item(img, seed=950, sparkles=3), False)
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    d = ImageDraw.Draw(img)
    d.rectangle([4, 12, 11, 14], fill=_c(DARK))
    d.rectangle([6, 9, 9, 12], fill=_c(GOLD))
    d.polygon([(2, 2), (13, 2), (12, 6), (8, 9), (3, 6)], fill=_c(GOLD), outline=_c((140, 90, 10)))
    d.ellipse([5, 2, 10, 7], fill=_c(PURPLE), outline=_c(PURPLE_L))
    out["void_night_trophy"] = (hd.hd_item(img, seed=951, sparkles=4), False)
    return out


def board_textures():
    S = 64
    front = Image.new("RGBA", (S, S), (120, 80, 40, 255))
    d = ImageDraw.Draw(front)
    for y in range(0, S, 8):
        d.line([0, y, S, y], fill=(95, 60, 28, 255))
    d.rectangle([0, 0, S - 1, S - 1], outline=(70, 40, 15, 255), width=3)
    for i, (x, y, col) in enumerate(((6, 8, (240, 230, 200)), (34, 6, (250, 220, 200)), (10, 36, (230, 240, 255)))):
        d.rectangle([x, y, x + 22, y + 20], fill=col + (255,))
        for k in range(3):
            d.line([x + 3, y + 5 + k * 5, x + 18, y + 5 + k * 5], fill=(90, 70, 60, 255))
        d.ellipse([x + 9, y - 2, x + 13, y + 2], fill=(200, 30, 30, 255))
    d.ellipse([40, 36, 56, 52], fill=(200, 40, 20, 255), outline=(255, 200, 60, 255))
    d.text((45, 38), "$", fill=(255, 230, 120, 255))
    side = Image.new("RGBA", (S, S), (100, 65, 30, 255))
    ImageDraw.Draw(side).rectangle([0, 0, S - 1, S - 1], outline=(70, 40, 15, 255), width=4)
    return {"sintara_board_front": front, "sintara_board_side": side}


def board_model():
    B = "yukicore:block/"
    side = {"texture": "#side"}
    return {"parent": "minecraft:block/block", "textures": {"front": B + "sintara_board_front", "side": B + "sintara_board_side",
                                                             "particle": B + "sintara_board_side"},
            "elements": [{"from": [0, 0, 12], "to": [16, 16, 16],
                          "faces": {"north": {"texture": "#front"}, "south": side, "east": side, "west": side, "up": side, "down": side}}]}


def blockstate():
    return {"variants": {f"facing={f}": {"model": "yukicore:block/sintara_board", "y": y}
                         for f, y in (("north", 0), ("east", 90), ("south", 180), ("west", 270))}}


def recipes():
    return {
        "sintara_board": shaped(["PCP", "PXP", "PPP"], {"P": "#minecraft:planks", "C": I("sintara_chip"), "X": "minecraft:paper"},
                                I("sintara_board")),
    }


AFFIX = {
    "fiery": ("Огненный", "Fiery"), "frozen": ("Ледяной", "Frozen"), "vampiric": ("Вампирский", "Vampiric"),
    "swift": ("Быстрый", "Swift"), "giant": ("Гигантский", "Giant"), "armored": ("Бронированный", "Armored"),
    "explosive": ("Взрывной", "Explosive"), "summoner": ("Призывающий", "Summoning"), "blinking": ("Мерцающий", "Blinking"),
}

TYPES = {
    # тип: (заголовок ru, en, цель ru, en)
    "monsters": ("Охота", "Hunt", "Убей монстров: %s", "Kill monsters: %s"),
    "elites": ("Охота на чемпионов", "Champion hunt", "Убей элитных мобов: %s", "Kill elite mobs: %s"),
    "ores": ("Шахтёр", "Miner", "Добудь руды: %s", "Mine ores: %s"),
    "spells": ("Практика магии", "Magic practice", "Сотвори заклинаний: %s", "Cast spells: %s"),
    "fish": ("Рыбалка", "Fishing", "Поймай рыбы: %s", "Catch fish: %s"),
}

LANG = {
    "item.yukicore.champion_shard": ("Осколок чемпиона", "Champion Shard"),
    "item.yukicore.champion_shard.desc": ("Падает с элитных мобов. Квесты и рецепты 7.5", "Dropped by elite mobs"),
    "item.yukicore.void_night_trophy": ("Трофей Ночи Пустоты", "Void Night Trophy"),
    "item.yukicore.void_night_trophy.desc": ("ПКМ: эпическая или легендарная награда и 20 фишек",
                                             "Use: an epic or legendary reward and 20 chips"),
    "block.yukicore.sintara_board": ("Доска Синтары", "Sintara's Board"),
    "block.yukicore.sintara_board.desc": ("ПКМ: 3 задания дня и Лавка за фишки", "Use: 3 daily contracts and a chip shop"),
    "board.yukicore.title": ("Доска Синтары", "Sintara's Board"),
    "board.yukicore.tab.contracts": ("Задания дня", "Daily contracts"),
    "board.yukicore.tab.shop": ("Лавка", "Shop"),
    "board.yukicore.claim": ("Забрать", "Claim"),
    "board.yukicore.reroll": ("Сменить (%s)", "Reroll (%s)"),
    "board.yukicore.reward": ("Награда: %s фишек", "Reward: %s chips"),
    "board.yukicore.buy": ("%s ф.", "%s c."),
    "board.yukicore.bonus": ("Выполни все 3 — ещё %s фишек и сундук удачи!", "Finish all 3 — %s more chips and a fortune box!"),
    "board.yukicore.bonus_got": ("Все задания дня выполнены. Новые — завтра!", "All contracts done. New ones tomorrow!"),
    "board.yukicore.done": ("Задание выполнено: %s — забери награду на Доске", "Contract done: %s — claim it at the Board"),
    "board.yukicore.all_done": ("✦ Все задания дня! +сундук удачи ✦", "✦ All daily contracts! +fortune box ✦"),
    "elite.yukicore.name": ("Элита", "Elite"),
    "invasion.yukicore.bar": ("Ночь Пустоты — продержись до рассвета", "Void Night — survive until dawn"),
    "invasion.yukicore.start": ("НОЧЬ ПУСТОТЫ", "VOID NIGHT"),
    "invasion.yukicore.start.sub": ("Из разломов идут монстры. Убей 5 и доживи до рассвета", "Monsters pour from rifts. Kill 5 and survive"),
    "invasion.yukicore.wave": ("Новая волна захватчиков!", "A new wave of invaders!"),
    "invasion.yukicore.leader": ("Вожак Ночи Пустоты", "Void Night Warlord"),
    "invasion.yukicore.leader_title": ("ВОЖАК ПРИШЁЛ", "THE WARLORD COMES"),
    "invasion.yukicore.leader_title.sub": ("Полночь. Победи Вожака Ночи Пустоты", "Midnight. Defeat the Void Night Warlord"),
    "invasion.yukicore.leader_dead": ("✦ %s победил Вожака Ночи Пустоты!", "✦ %s defeated the Void Night Warlord!"),
    "invasion.yukicore.victory": ("РАССВЕТ", "DAWN"),
    "invasion.yukicore.victory.sub": ("Ночь Пустоты отбита!", "The Void Night is over!"),
    "invasion.yukicore.reward": ("Ночь Пустоты: убито захватчиков — %s. Получен Трофей Ночи Пустоты!",
                                 "Void Night: %s invaders slain. You got a Void Night Trophy!"),
    "invasion.yukicore.over": ("Рассвет. Ночь Пустоты закончилась", "Dawn. The Void Night is over"),
}


def lang():
    ru, en = {}, {}
    for k, (r, e) in LANG.items():
        ru[k], en[k] = r, e
    for k, (r, e) in AFFIX.items():
        ru[f"elite.yukicore.{k}"], en[f"elite.yukicore.{k}"] = r, e
    for k, (tr, te, gr, ge) in TYPES.items():
        ru[f"board.yukicore.type.{k}"], en[f"board.yukicore.type.{k}"] = tr, te
        ru[f"board.yukicore.goal.{k}"], en[f"board.yukicore.goal.{k}"] = gr, ge
    return ru, en


ENTRIES = [
    ("sintara", "hunt75", "Охота и вторжения", "yukicore:sintara_board", [
        "$(l)Элитные мобы$(): изредка монстр рождается чемпионом — имя над головой выдаёт его способности: огненный, "
        "ледяной, вампирский, быстрый, гигантский, бронированный, взрывной, призывающий, мерцающий. Сильнее, но с "
        "фишками, камнями, сундуками и $(l)Осколками чемпиона$().",
        "$(l)Ночь Пустоты$(): примерно раз в пять ночей из разломов лезут волны захватчиков. В полночь приходит "
        "$(l)Вожак$(). Убей 5 захватчиков и доживи до рассвета — получишь $(l)Трофей Ночи Пустоты$().",
        ("spot", "yukicore:sintara_board", "$(l)Доска Синтары$(): каждый день 3 задания (охота, элита, руда, магия, "
                                           "рыбалка). Выполнил все — бонус. Во вкладке Лавка фишки меняются на вещи."),
    ]),
]
