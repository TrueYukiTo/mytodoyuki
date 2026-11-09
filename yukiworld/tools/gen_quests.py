#!/usr/bin/env python3
"""
Генератор квестов FTB Quests для YukiWorld.

Пишет yukiworld/pack/config/ftbquests/quests/ :
  data.snbt, chapter_groups.snbt, chapters/*.snbt, lang/ru_ru.snbt, lang/en_us.snbt

Каждый квест описан одной строкой Q(...) ниже. ID детерминированы (хеш ключа),
поэтому пересборка не сбрасывает прогресс игроков.

Запуск:  python3 yukiworld/tools/gen_quests.py [items.txt для проверки ID]
"""
import hashlib
import sys
from dataclasses import dataclass, field
from pathlib import Path

OUT = Path(__file__).resolve().parent.parent / "pack" / "config" / "ftbquests" / "quests"


def qid(key: str) -> str:
    h = hashlib.sha1(("yukiworld:" + key).encode()).hexdigest()[:16].upper()
    return ("1" + h[1:]) if h[0] == "0" else h


# ---------------------------------------------------------------- SNBT
def snbt(v, ind=0) -> str:
    pad, pad1 = "\t" * ind, "\t" * (ind + 1)
    if isinstance(v, bool):
        return "true" if v else "false"
    if isinstance(v, int):
        return str(v)
    if isinstance(v, float):
        return f"{v}d"
    if isinstance(v, Long):
        return f"{v.v}L"
    if isinstance(v, str):
        return '"' + v.replace("\\", "\\\\").replace('"', '\\"') + '"'
    if isinstance(v, list):
        if not v:
            return "[ ]"
        return "[\n" + "\n".join(pad1 + snbt(x, ind + 1) for x in v) + "\n" + pad + "]"
    if isinstance(v, dict):
        if not v:
            return "{ }"
        lines = []
        for k in sorted(v):
            key = k if all(c.isalnum() or c == "_" for c in k) else snbt(k)
            lines.append(f"{pad1}{key}: {snbt(v[k], ind + 1)}")
        return "{\n" + "\n".join(lines) + "\n" + pad + "}"
    raise TypeError(type(v))


@dataclass
class Long:
    v: int


# ---------------------------------------------------------------- модель квестов
@dataclass
class Q:
    key: str
    item: str                 # предмет задачи (или "checkmark")
    count: int
    ru: str
    en: str
    desc_ru: str = ""
    desc_en: str = ""
    deps: list = field(default_factory=list)
    x: float = 0.0
    y: float = 0.0
    xp: int = 100
    reward: tuple = None      # (item, count)
    shape: str = ""
    size: float = 1.0
    icon: str = None


@dataclass
class Chapter:
    key: str
    ru: str
    en: str
    sub_ru: str
    sub_en: str
    icon: str
    quests: list


Y = "yukicore:"

CHAPTERS = [
    Chapter("welcome", "❄ Добро пожаловать", "❄ Welcome", "Первые шаги в YukiWorld", "First steps in YukiWorld",
            Y + "yuki_star", [
        Q("start", "checkmark", 1, "Добро пожаловать в YukiWorld!", "Welcome to YukiWorld!",
          "Это большая сборка: техника, магия, фермы и новые измерения.\nЦель — собрать &dБесконечную звезду Юки&r, объединив предметы из всех модов.\nНажми галочку, чтобы начать.",
          "A huge pack: tech, magic, farming and new dimensions.\nYour goal is the &dYuki Infinity Star&r, crafted from items of every mod.\nClick the checkmark to begin.",
          x=0, y=0, reward=(Y + "frost_apple", 2), shape="gear", size=1.5),
        Q("frost_ore", Y + "raw_frost", 8, "Холод под ногами", "Cold underfoot",
          "Морозная руда встречается в любой пещере верхнего мира.", "Frost ore spawns in any Overworld cave.",
          ["start"], 2, -1),
        Q("frost_ingot", Y + "frost_ingot", 16, "Морозный слиток", "Frost Ingot",
          "Переплавь необработанный мороз.", "Smelt raw frost.", ["frost_ore"], 4, -1, reward=(Y + "frost_ingot", 4)),
        Q("yuki_crystal", Y + "yuki_crystal", 8, "Кристалл Юки", "Yuki Crystal",
          "Светящаяся руда глубоко под землёй (Y ниже 32). Нужна железная кирка.",
          "Glowing ore deep underground (below Y 32). Needs an iron pickaxe.", ["start"], 2, 1),
        Q("frost_pickaxe", Y + "frost_pickaxe", 1, "Морозная кирка", "Frost Pickaxe",
          "Быстрее алмазной и отлично зачаровывается.", "Faster than diamond and great to enchant.",
          ["frost_ingot"], 6, -2),
        Q("frost_armor", Y + "frost_chestplate", 1, "Ледяная броня", "Frost Armor",
          "Нагрудник даёт 8 брони и 2.5 прочности.", "The chestplate gives 8 armor and 2.5 toughness.",
          ["frost_ingot"], 6, 0, reward=(Y + "frost_helmet", 1)),
        Q("lamp", Y + "yuki_lamp", 4, "Да будет свет", "Let there be light",
          "Лампы Юки светят на 15 и красиво смотрятся на базе.", "Yuki Lamps give light 15.",
          ["yuki_crystal"], 4, 2),
        Q("backpack", "sophisticatedbackpacks:backpack", 1, "Рюкзак", "Backpack",
          "Sophisticated Backpacks: больше места и улучшения.", "More space and upgrades.", ["start"], 0, 2),
        Q("waystone", "waystones:waystone", 1, "Путевой камень", "Waystone",
          "Телепортация между путевыми камнями.", "Teleport between waystones.", ["backpack"], 0, 4),
        Q("yuki_star", Y + "yuki_star", 1, "Звезда Юки", "Yuki Star",
          "ПКМ: ночное зрение, скорость, прыгучесть и регенерация.",
          "Right-click: night vision, speed, jump boost & regeneration.", ["frost_ingot", "yuki_crystal"],
          6, 2, xp=300, shape="hexagon", size=1.25),
        Q("frost_core", Y + "frost_core", 1, "Морозное ядро", "Frost Core",
          "Основа всех четырёх ядер. Дальше открываются главы техники, магии, природы и измерений.",
          "The base of all four cores. Unlocks the Tech, Magic, Nature and Dimensions chapters.",
          ["yuki_star", "frost_pickaxe"], 8, 0, xp=500, shape="gear", size=1.75),
    ]),
    Chapter("tech", "⚙ Технологии", "⚙ Technology", "Mekanism, Create, AE2 и другие", "Mekanism, Create, AE2 and more",
            Y + "tech_core", [
        Q("t_andesite", "create:andesite_alloy", 16, "Андезитовый сплав", "Andesite Alloy",
          "Начало пути Create.", "The start of Create.", x=0, y=-3),
        Q("t_brass", "create:brass_ingot", 16, "Латунь", "Brass", "Смешай медь и цинк в чаше.",
          "Mix copper and zinc in a basin.", ["t_andesite"], 2, -3),
        Q("t_precision", "create:precision_mechanism", 1, "Точный механизм", "Precision Mechanism",
          "Сборка через последовательную сборку.", "Made by sequenced assembly.", ["t_brass"], 4, -3, xp=300),
        Q("t_infuser", "mekanism:metallurgic_infuser", 1, "Металлургический инфузор", "Metallurgic Infuser",
          "Сердце раннего Mekanism.", "Early Mekanism core machine.", x=0, y=-1),
        Q("t_steel", "mekanism:steel_casing", 4, "Стальной корпус", "Steel Casing", "", "", ["t_infuser"], 2, -1),
        Q("t_ultimate", "mekanism:ultimate_control_circuit", 1, "Абсолютная схема", "Ultimate Control Circuit",
          "", "", ["t_steel"], 4, -1, xp=300),
        Q("t_certus", "ae2:certus_quartz_crystal", 16, "Истинный кварц", "Certus Quartz", "", "", x=0, y=1),
        Q("t_controller", "ae2:controller", 1, "ME-контроллер", "ME Controller", "Своя цифровая сеть хранения.",
          "Your own digital storage network.", ["t_certus"], 2, 1),
        Q("t_singularity", "ae2:singularity", 1, "Сингулярность", "Singularity",
          "Материальная пушка + конденсатор материи. Понадобится ещё и для финала!",
          "Matter condenser. You will need more for the finale!", ["t_controller"], 4, 1, xp=500),
        Q("t_orb", "powah:energizing_orb", 1, "Заряжающая сфера", "Energizing Orb", "", "", x=0, y=3),
        Q("t_nitro", "powah:capacitor_nitro", 1, "Нитро-конденсатор", "Nitro Capacitor", "", "", ["t_orb"], 2, 3,
          xp=300),
        Q("t_ie_steel", "immersiveengineering:ingot_steel", 8, "Сталь IE", "IE Steel", "Коксовая и доменная печи.",
          "Coke oven and blast furnace.", x=0, y=5),
        Q("t_ie_adv", "immersiveengineering:component_electronic_adv", 1, "Продвинутый компонент", "Advanced Component",
          "", "", ["t_ie_steel"], 2, 5),
        Q("t_rs", "refinedstorage:construction_core", 1, "Ядро конструкции RS", "RS Construction Core", "", "",
          x=0, y=7),
        Q("t_if", "industrialforegoing:machine_frame_supreme", 1, "Верховная рама", "Supreme Machine Frame",
          "Industrial Foregoing.", "", x=2, y=7, xp=300),
        Q("t_oritech", "oritech:overcharged_crystal", 1, "Перезаряженный кристалл", "Overcharged Crystal",
          "Oritech.", "", x=4, y=7, xp=300),
        Q("tech_core", Y + "tech_core", 1, "Технологическое ядро", "Tech Core",
          "Объедини лучшее из всех технических модов вокруг Морозного ядра.",
          "Combine the best of every tech mod around a Frost Core.",
          ["t_precision", "t_ultimate", "t_singularity", "t_nitro", "t_ie_adv", "t_rs", "t_if", "t_oritech"],
          7, 2, xp=1000, shape="gear", size=2.0),
    ]),
    Chapter("magic", "✨ Магия", "✨ Magic", "Ars Nouveau, Iron's Spells, Occultism", "Ars Nouveau, Iron's Spells, Occultism",
            Y + "magic_core", [
        Q("m_book", "ars_nouveau:novice_spell_book", 1, "Книга заклинаний", "Novice Spell Book", "", "", x=0, y=-2),
        Q("m_source", "ars_nouveau:source_gem_block", 1, "Блок самоцвета источника", "Source Gem Block", "", "",
          ["m_book"], 2, -2),
        Q("m_tribute", "ars_nouveau:wilden_tribute", 1, "Дань Вилдена", "Wilden Tribute",
          "Победи Вилдена-химеру.", "Defeat the Wilden Chimera.", ["m_source"], 4, -2, xp=500),
        Q("m_arcane", "irons_spellbooks:arcane_essence", 16, "Тайная эссенция", "Arcane Essence", "", "", x=0, y=0),
        Q("m_ink", "irons_spellbooks:legendary_ink", 1, "Легендарные чернила", "Legendary Ink", "", "",
          ["m_arcane"], 2, 0, xp=300),
        Q("m_pearl", "irons_spellbooks:divine_pearl", 1, "Божественная жемчужина", "Divine Pearl", "", "",
          ["m_arcane"], 4, 0, xp=300),
        Q("m_datura", "occultism:otherstone", 8, "Иной камень", "Otherstone",
          "Раскрой иной мир с помощью демонического сна.", "Reveal the Otherworld.", x=0, y=2),
        Q("m_afrit", "occultism:afrit_essence", 1, "Эссенция африта", "Afrit Essence", "", "", ["m_datura"], 2, 2,
          xp=300),
        Q("m_spirit", "occultism:spirit_attuned_gem", 1, "Настроенный камень духа", "Spirit Attuned Gem", "", "",
          ["m_datura"], 4, 2),
        Q("m_breath", "apothic_enchanting:infused_breath", 1, "Наполненное дыхание", "Infused Breath",
          "Apotheosis: дыхание дракона на столе зачарования.", "", x=0, y=4, xp=300),
        Q("m_dark", "evilcraft:dark_gem", 4, "Тёмный камень", "Dark Gem", "EvilCraft.", "", x=2, y=4),
        Q("magic_core", Y + "magic_core", 1, "Магическое ядро", "Magic Core",
          "Пять школ магии в одном ядре.", "Five schools of magic in one core.",
          ["m_tribute", "m_ink", "m_pearl", "m_afrit", "m_spirit", "m_breath", "m_dark"], 7, 1, xp=1000,
          shape="gear", size=2.0),
    ]),
    Chapter("nature", "🌾 Природа", "🌾 Nature", "Фермы, пчёлы и еда", "Farms, bees and food", Y + "nature_core", [
        Q("n_pot", "farmersdelight:cooking_pot", 1, "Кастрюля", "Cooking Pot", "", "", x=0, y=-2),
        Q("n_soil", "farmersdelight:rich_soil", 8, "Плодородная почва", "Rich Soil", "", "", ["n_pot"], 2, -2),
        Q("n_inferium", "mysticalagriculture:inferium_essence", 64, "Эссенция инфериума", "Inferium Essence", "",
          "", x=0, y=0),
        Q("n_supremium", "mysticalagriculture:supremium_ingot", 4, "Слиток супремиума", "Supremium Ingot", "", "",
          ["n_inferium"], 2, 0),
        Q("n_awakened", "mysticalagriculture:awakened_supremium_ingot", 1, "Пробуждённый супремиум",
          "Awakened Supremium", "Ритуал пробуждения.", "The awakening ritual.", ["n_supremium"], 4, 0, xp=500),
        Q("n_wax", "productivebees:wax", 16, "Воск", "Wax", "Productive Bees.", "", x=0, y=2),
        Q("n_bees", "the_bumblezone:essence_of_the_bees", 1, "Эссенция пчёл", "Essence of the Bees",
          "Пройди испытания в Бамблзоне.", "Complete the Bumblezone trials.", ["n_wax"], 2, 2, xp=500),
        Q("n_jelly", "the_bumblezone:royal_jelly_block", 1, "Маточное молочко", "Royal Jelly Block", "", "",
          ["n_wax"], 4, 2),
        Q("n_steeleaf", "twilightforest:steeleaf_ingot", 8, "Стальной лист", "Steeleaf", "Сумеречный лес.", "",
          x=0, y=4),
        Q("n_beef", "artifacts:everlasting_beef", 1, "Вечная говядина", "Everlasting Beef",
          "Артефакт из сундуков и мимиков.", "An artifact from chests and mimics.", x=2, y=4),
        Q("n_totem", "minecraft:totem_of_undying", 1, "Тотем бессмертия", "Totem of Undying", "", "", x=4, y=4),
        Q("nature_core", Y + "nature_core", 1, "Ядро природы", "Nature Core", "Сила жизни во всех формах.",
          "The power of life in every form.",
          ["n_soil", "n_awakened", "n_bees", "n_jelly", "n_steeleaf", "n_beef", "n_totem"], 7, 1, xp=1000,
          shape="gear", size=2.0),
    ]),
    Chapter("dimensions", "🌌 Измерения", "🌌 Dimensions", "Незер, Энд, Aether и Сумеречный лес",
            "Nether, End, Aether and the Twilight Forest", Y + "dimension_core", [
        Q("d_blaze", "minecraft:blaze_rod", 8, "Стержни ифрита", "Blaze Rods", "", "", x=0, y=-3),
        Q("d_star", "minecraft:nether_star", 1, "Звезда Незера", "Nether Star", "Победи Иссушителя.",
          "Defeat the Wither.", ["d_blaze"], 2, -3, xp=500),
        Q("d_eye", "minecraft:ender_eye", 12, "Око Эндера", "Eye of Ender", "", "", x=0, y=-1),
        Q("d_breath", "minecraft:dragon_breath", 4, "Дыхание дракона", "Dragon's Breath", "", "", ["d_eye"], 2, -1,
          xp=500),
        Q("d_echo", "minecraft:echo_shard", 4, "Осколки эха", "Echo Shards", "Древние города.", "Ancient Cities.",
          x=0, y=1),
        Q("d_heavy", "minecraft:heavy_core", 1, "Тяжёлое ядро", "Heavy Core", "Испытания (Trial Chambers).",
          "Trial Chambers.", x=2, y=1),
        Q("d_ambrosium", "aether:ambrosium_shard", 8, "Амброзий", "Ambrosium",
          "Портал в Aether: рамка из светокамня + ведро воды.", "Aether portal: glowstone frame + water.", x=0,
          y=3),
        Q("d_gravitite", "aether:enchanted_gravitite", 1, "Зачарованный гравитит", "Enchanted Gravitite", "", "",
          ["d_ambrosium"], 2, 3, xp=300),
        Q("d_naga", "twilightforest:naga_scale", 1, "Чешуя наги", "Naga Scale",
          "Портал в Сумеречный лес: цветы вокруг воды + алмаз.", "Twilight portal: flowers around water + diamond.",
          x=0, y=5),
        Q("d_carminite", "twilightforest:carminite", 1, "Карминит", "Carminite", "", "", ["d_naga"], 2, 5),
        Q("d_knight", "twilightforest:knightmetal_ingot", 4, "Рыцарский металл", "Knightmetal", "", "",
          ["d_carminite"], 4, 5),
        Q("d_fiery", "twilightforest:fiery_ingot", 1, "Огненный слиток", "Fiery Ingot", "", "", ["d_knight"], 6, 5,
          xp=500),
        Q("dimension_core", Y + "dimension_core", 1, "Ядро измерений", "Dimension Core",
          "Осколки всех миров, которые ты посетил.", "Shards of every world you visited.",
          ["d_star", "d_breath", "d_echo", "d_heavy", "d_gravitite", "d_fiery"], 8, 1, xp=1000, shape="gear",
          size=2.0),
    ]),
    Chapter("finale", "⭐ Финал YukiWorld", "⭐ YukiWorld Finale", "Сердце мира и Бесконечная звезда",
            "The Heart and the Infinity Star", Y + "yuki_infinity_star", [
        Q("f_cores", "checkmark", 1, "Четыре ядра", "Four Cores",
          "Собери Технологическое, Магическое, Природное и Ядро измерений.",
          "Craft the Tech, Magic, Nature and Dimension cores.", [], 0, 0, shape="hexagon", size=1.5,
          icon=Y + "frost_core"),
        Q("f_time", "justdirethings:time_crystal", 4, "Кристаллы времени", "Time Crystals", "Just Dire Things.", "",
          ["f_cores"], 2, -2),
        Q("f_antimatter", "mekanism:pellet_antimatter", 1, "Антиматерия", "Antimatter",
          "Mekanism: SPS (суперкритический сепаратор фазы).", "Mekanism SPS.", ["f_cores"], 2, 2, xp=1000),
        Q("f_heart", Y + "yuki_heart", 1, "Сердце YukiWorld", "Heart of YukiWorld",
          "Четыре ядра + антиматерия + кристаллы времени.", "Four cores + antimatter + time crystals.",
          ["f_time", "f_antimatter"], 4, 0, xp=2000, shape="gear", size=2.0),
        Q("f_radiance", "create:refined_radiance", 4, "Очищенное сияние", "Refined Radiance",
          "Create: хроматическое соединение под светом.", "Create: chromatic compound in light.", ["f_heart"], 6,
          -2),
        Q("f_shadow", "create:shadow_steel", 4, "Теневая сталь", "Shadow Steel",
          "Create: хроматическое соединение в бездне.", "Create: chromatic compound in the void.", ["f_heart"], 6,
          2),
        Q("f_star", Y + "yuki_infinity_star", 1, "Бесконечная звезда Юки", "Yuki Infinity Star",
          "Собери на механическом крафтере Create (5x5).\nВ инвентаре даёт полёт, ночное зрение и сопротивление.\n&dТы прошёл YukiWorld!&r",
          "Craft on a Create Mechanical Crafter (5x5).\nIn inventory: flight, night vision, resistance.\n&dYou beat YukiWorld!&r",
          ["f_radiance", "f_shadow"], 8, 0, xp=5000, shape="gear", size=3.0),
        Q("f_blade", Y + "yuki_blade", 1, "Клинок Юки", "Yuki Blade", "Неразрушимый меч с уроном 30.",
          "An unbreakable sword with 30 damage.", ["f_heart"], 4, 3, xp=1000),
    ]),
]

# Связи между главами: финальные квесты зависят от квестов других глав
CROSS_DEPS = {
    "t_andesite": ["frost_core"], "t_infuser": ["frost_core"], "t_certus": ["frost_core"], "t_orb": ["frost_core"],
    "t_ie_steel": ["frost_core"], "t_rs": ["frost_core"], "t_if": ["frost_core"], "t_oritech": ["frost_core"],
    "m_book": ["frost_core"], "m_arcane": ["frost_core"], "m_datura": ["frost_core"], "m_breath": ["frost_core"],
    "m_dark": ["frost_core"],
    "n_pot": ["frost_core"], "n_inferium": ["frost_core"], "n_wax": ["frost_core"], "n_steeleaf": ["frost_core"],
    "n_beef": ["frost_core"], "n_totem": ["frost_core"],
    "d_blaze": ["frost_core"], "d_eye": ["frost_core"], "d_echo": ["frost_core"], "d_heavy": ["frost_core"],
    "d_ambrosium": ["frost_core"], "d_naga": ["frost_core"],
    "f_cores": ["tech_core", "magic_core", "nature_core", "dimension_core"],
}

VANILLA_OK = {"minecraft:blaze_rod", "minecraft:nether_star", "minecraft:ender_eye", "minecraft:dragon_breath",
              "minecraft:echo_shard", "minecraft:heavy_core", "minecraft:totem_of_undying"}


def main():
    known = None
    if len(sys.argv) > 1:
        known = set(Path(sys.argv[1]).read_text().split())
    yuki = {p.stem for p in (Path(__file__).resolve().parent.parent / "yukicore" / "src" / "main" / "resources"
                             / "assets" / "yukicore" / "models" / "item").glob("*.json")}

    lang = {"ru_ru": {}, "en_us": {}}
    missing = []
    (OUT / "chapters").mkdir(parents=True, exist_ok=True)
    for old in (OUT / "chapters").glob("*.snbt"):
        old.unlink()

    for order, ch in enumerate(CHAPTERS):
        cid = qid("chapter/" + ch.key)
        lang["ru_ru"][f"chapter.{cid}.title"] = ch.ru
        lang["en_us"][f"chapter.{cid}.title"] = ch.en
        lang["ru_ru"][f"chapter.{cid}.chapter_subtitle"] = [ch.sub_ru]
        lang["en_us"][f"chapter.{cid}.chapter_subtitle"] = [ch.sub_en]
        quests = []
        for q in ch.quests:
            items = [q.item, q.icon] if q.icon else [q.item]
            for it in items:
                if it == "checkmark":
                    continue
                ns, path = it.split(":")
                ok = (ns == "yukicore" and path in yuki) or it in VANILLA_OK or (known is None or it in known)
                if not ok:
                    missing.append(it)
            id_ = qid(q.key)
            task = {"id": qid(q.key + "/task"), "type": "checkmark"} if q.item == "checkmark" else {
                "id": qid(q.key + "/task"), "type": "item", "item": {"id": q.item, "count": 1},
                "count": Long(q.count)}
            rewards = [{"id": qid(q.key + "/xp"), "type": "xp", "xp": q.xp}]
            if q.reward:
                rewards.append({"id": qid(q.key + "/item"), "type": "item",
                                "item": {"id": q.reward[0], "count": 1}, "count": q.reward[1]})
            quest = {"id": id_, "x": float(q.x), "y": float(q.y), "tasks": [task], "rewards": rewards,
                     "dependencies": [qid(d) for d in q.deps + CROSS_DEPS.get(q.key, [])]}
            if q.shape:
                quest["shape"] = q.shape
            if q.size != 1.0:
                quest["size"] = float(q.size)
            icon = q.icon or (None if q.item == "checkmark" else q.item)
            if q.item == "checkmark" and not q.icon:
                icon = Y + "yuki_star"
            quest["icon"] = {"id": icon, "count": 1}
            quests.append(quest)
            lang["ru_ru"][f"quest.{id_}.title"] = q.ru
            lang["en_us"][f"quest.{id_}.title"] = q.en
            if q.desc_ru:
                lang["ru_ru"][f"quest.{id_}.quest_desc"] = q.desc_ru.split("\n")
            if q.desc_en or q.desc_ru:
                lang["en_us"][f"quest.{id_}.quest_desc"] = (q.desc_en or q.desc_ru).split("\n")

        chapter = {"id": cid, "filename": ch.key, "group": "", "order_index": order,
                   "icon": {"id": ch.icon, "count": 1}, "default_quest_shape": "", "default_hide_dependency_lines": False,
                   "quest_links": [], "images": [], "quests": quests}
        (OUT / "chapters" / f"{ch.key}.snbt").write_text(snbt(chapter) + "\n", encoding="utf-8")

    data = {"version": 13, "default_reward_team": False, "default_consume_items": False,
            "default_autoclaim_rewards": "disabled", "default_quest_shape": "circle",
            "default_quest_disable_jei": False, "emergency_items_cooldown": 300, "drop_loot_crates": False,
            "disable_gui": False, "grid_scale": 0.5, "pause_game": False, "lock_message": "",
            "progression_mode": "linear", "detection_delay": 20, "fallback_locale": "en_us",
            "icon": {"id": Y + "yuki_infinity_star", "count": 1}}
    (OUT / "data.snbt").write_text(snbt(data) + "\n", encoding="utf-8")
    (OUT / "chapter_groups.snbt").write_text(snbt({"chapter_groups": []}) + "\n", encoding="utf-8")
    (OUT / "lang").mkdir(exist_ok=True)
    for loc, table in lang.items():
        (OUT / "lang" / f"{loc}.snbt").write_text(snbt(table) + "\n", encoding="utf-8")

    n = sum(len(c.quests) for c in CHAPTERS)
    print(f"{len(CHAPTERS)} chapters, {n} quests -> {OUT}")
    if missing:
        print("UNKNOWN ITEMS:", sorted(set(missing)))
        sys.exit(1)


if __name__ == "__main__":
    main()
