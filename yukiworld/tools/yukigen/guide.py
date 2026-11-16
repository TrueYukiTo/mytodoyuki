"""
Подсказки «Как получить» для квестов: рецепты из самих модов сборки (верстак, машины Create/Mekanism/AE2/…)
с русскими названиями предметов, плюс ручные гайды для хитрых вещей, боссов и измерений.
"""
import json
import re
import zipfile
from pathlib import Path

from .guide_text import DIM_GUIDE, ENTITY_GUIDE, ITEM_GUIDE

# тип рецепта → где делать
MACHINES = {
    "minecraft:crafting_shaped": "Верстак", "minecraft:crafting_shapeless": "Верстак (без формы)",
    "minecraft:smelting": "Печь", "minecraft:blasting": "Доменная печь", "minecraft:smoking": "Коптильня",
    "minecraft:campfire_cooking": "Костёр", "minecraft:stonecutting": "Камнерез",
    "minecraft:smithing_transform": "Кузнечный стол",
    "create:mixing": "Create: механический миксер над чашей", "create:compacting": "Create: пресс над чашей",
    "create:pressing": "Create: механический пресс", "create:crushing": "Create: дробильные колёса",
    "create:milling": "Create: жернова", "create:cutting": "Create: механическая пила",
    "create:mechanical_crafting": "Create: механические крафтеры", "create:sequenced_assembly": "Create: последовательная сборка",
    "create:filling": "Create: заправщик", "create:emptying": "Create: опорожнитель", "create:deploying": "Create: развёртыватель",
    "create:haunting": "Create: промывка в огне душ (вентилятор)", "create:splashing": "Create: промывка водой (вентилятор)",
    "create:item_application": "Create: ПКМ предметом по блоку",
    "mekanism:enriching": "Mekanism: обогатительная камера", "mekanism:crushing": "Mekanism: дробилка",
    "mekanism:smelting": "Mekanism: энергетическая печь", "mekanism:combining": "Mekanism: комбайнер",
    "mekanism:metallurgic_infusing": "Mekanism: металлургический наполнитель",
    "mekanism:reaction": "Mekanism: химическая реакционная камера (PRC)", "mekanism:compressing": "Mekanism: осмиевый компрессор",
    "mekanism:purifying": "Mekanism: очистительная камера", "mekanism:injecting": "Mekanism: химическая инъекционная камера",
    "mekanism:nucleosynthesizing": "Mekanism: антипротонный нуклеосинтезатор", "mekanism:sawing": "Mekanism: лесопилка",
    "mekanism:chemical_infusing": "Mekanism: химический наполнитель", "mekanism:crystallizing": "Mekanism: химический кристаллизатор",
    "mekanism:activating": "Mekanism: солнечный нейтронный активатор", "mekanism:centrifuging": "Mekanism: изотопная центрифуга",
    "mekanism:mek_data": "Верстак", "mekanism:painting": "Mekanism: покрасочная машина",
    "ae2:inscriber": "AE2: высекатель (пресс + материал)", "ae2:charger": "AE2: зарядник", "ae2:transform": "AE2: бросить в воду/взрыв",
    "ae2:entropy": "AE2: энтропийный манипулятор",
    "ars_nouveau:enchanting_apparatus": "Ars Nouveau: зачаровывающий аппарат на пьедесталах",
    "ars_nouveau:imbuement": "Ars Nouveau: камера наполнения (источник)", "ars_nouveau:glyph": "Ars Nouveau: стол писаря",
    "ars_nouveau:crush": "Ars Nouveau: глиф «Дробление»",
    "irons_spellbooks:alchemist_cauldron_brew": "Iron's Spells: котёл алхимика",
    "powah:energizing": "Powah: энергизирующий орб + стержни",
    "occultism:ritual": "Occultism: ритуал (пентакль)", "occultism:spirit_fire": "Occultism: бросить в духовный огонь",
    "occultism:crushing": "Occultism: дух-дробильщик", "occultism:miner": "Occultism: дух-шахтёр",
    "occultism:spirit_trade": "Occultism: обмен с духом",
    "evilcraft:blood_infuser": "EvilCraft: кровяной инфузор", "evilcraft:environmental_accumulator": "EvilCraft: накопитель среды",
    "mysticalagriculture:infusion": "Mystical Agriculture: инфузионный алтарь", "mysticalagriculture:awakening": "Mystical Agriculture: алтарь пробуждения",
    "mysticalagriculture:reprocessor": "Mystical Agriculture: переработчик семян",
    "yukicore:altar": "Алтарь Юки", "yukicore:frost_forging": "Морозная кузня",
    "industrialforegoing:dissolution_chamber": "Industrial Foregoing: камера растворения",
    "industrialforegoing:fluid_extractor": "Industrial Foregoing: экстрактор", "industrialforegoing:laser_drill_ore": "Industrial Foregoing: лазерный бур",
    "justdirethings:goospread": "Just Dire Things: слизь (Goo) нужного уровня превращает блок",
    "immersiveengineering:alloy": "IE: печь для сплавов", "immersiveengineering:blast_furnace": "IE: доменная печь",
    "immersiveengineering:metal_press": "IE: металлопресс", "immersiveengineering:crusher": "IE: дробилка",
    "immersiveengineering:arc_furnace": "IE: дуговая печь", "immersiveengineering:blueprint": "IE: инженерный стол (чертёж)",
    "immersiveengineering:squeezer": "IE: пресс-выжималка", "immersiveengineering:fermenter": "IE: ферментер",
    "immersiveengineering:bottling_machine": "IE: разливочная машина", "immersiveengineering:mixer": "IE: миксер",
    "immersiveengineering:refinery": "IE: нефтеперегонка",
    "actuallyadditions:empowering": "Actually Additions: расширитель (Empowerer)",
    "actuallyadditions:laser": "Actually Additions: лазер атомного реконструктора",
    "oritech:assembler": "Oritech: сборщик", "oritech:foundry": "Oritech: литейная", "oritech:pulverizer": "Oritech: измельчитель",
    "oritech:grinder": "Oritech: дробилка", "oritech:centrifuge": "Oritech: центрифуга",
    "oritech:atomic_forge": "Oritech: атомная кузня", "oritech:particle_collision": "Oritech: ускоритель частиц",
    "ad_astra:nasa_workbench": "Ad Astra: верстак NASA", "ad_astra:alloying": "Ad Astra: печь для сплавов",
    "ad_astra:compressing": "Ad Astra: компрессор", "ad_astra:refining": "Ad Astra: нефтеперерабатывающий завод",
    "ad_astra:cryo_freezing": "Ad Astra: криозаморозка", "ad_astra:oxygen_loading": "Ad Astra: загрузчик кислорода",
    "theurgy:calcination": "Theurgy: печь кальцинации", "theurgy:liquefaction": "Theurgy: котёл разжижения",
    "theurgy:incubation": "Theurgy: инкубатор", "theurgy:distillation": "Theurgy: дистиллятор",
    "theurgy:accumulation": "Theurgy: аккумулятор солей", "theurgy:fermentation": "Theurgy: ферментационный чан",
    "theurgy:digestion": "Theurgy: варочный чан", "theurgy:reformation": "Theurgy: реформационный массив",
    "malum:spirit_infusion": "Malum: алтарь духов", "malum:spirit_focusing": "Malum: обелиск фокусировки",
    "malum:spirit_transmutation": "Malum: транcмутация духов", "malum:void_favor": "Malum: колодец пустоты",
    "naturesaura:altar": "Nature's Aura: алтарь природы", "naturesaura:tree_ritual": "Nature's Aura: ритуал дерева (саженец + предметы на стойках)",
    "naturesaura:offering": "Nature's Aura: алтарь подношений", "naturesaura:animal_spawner": "Nature's Aura: призыватель",
    "forbidden_arcanus:apply_modifier": "Forbidden & Arcanus: кузнечный стол",
    "goety:brazier": "Goety: жаровня некроманта", "goety:ritual": "Goety: тёмный алтарь (ритуал)",
    "goety:cursed_infuser": "Goety: проклятый инфузор",
    "hexerei:mixingcauldron": "Hexerei: котёл ведьмы", "hexerei:pestle_and_mortar": "Hexerei: ступка",
    "hexerei:dipper": "Hexerei: свечной макатель", "hexerei:woodcutter": "Hexerei: дровокол",
    "reliquary:alkahestry_crafting": "Reliquary: том алхимии", "reliquary:cauldron": "Reliquary: котёл",
    "silentgear:compound": "Silent Gear: компаундер", "silentgear:fabric": "Silent Gear",
    "productivebees:centrifuge": "Productive Bees: центрифуга", "productivebees:bee_conversion": "Productive Bees: превращение пчёл",
    "farmersdelight:cooking": "Farmer's Delight: кастрюля на огне", "farmersdelight:cutting": "Farmer's Delight: разделочная доска",
    "twilightforest:uncrafting": "Twilight Forest: верстак разборки",
    "eternal_starlight:starlight_crafting": "Eternal Starlight", "cataclysm:weapon_fusion": "Cataclysm: наковальня слияния",
    "cataclysm:amethyst_bless": "Cataclysm: благословение аметиста",
}

SKIP_TYPES = ("recycling", "uncrafting", "deconstruct", "unpacking")
_RES_KEYS = ("result", "results", "output", "outputs", "item_output", "output_item", "itemOutput", "result_item")


def _rl(v):
    return isinstance(v, str) and re.fullmatch(r"#?[a-z0-9_.\-]+:[a-z0-9_./\-]+", v) is not None


def _results(r):
    out = []
    for k in _RES_KEYS:
        if k not in r:
            continue
        v = r[k]
        vs = v if isinstance(v, list) else [v]
        for x in vs:
            if isinstance(x, str) and _rl(x):
                out.append(x)
            elif isinstance(x, dict):
                for kk in ("id", "item", "Name"):
                    if _rl(x.get(kk)):
                        out.append(x[kk])
                        break
                for kk in ("stack", "output", "item"):
                    if isinstance(x.get(kk), dict):
                        out += [x[kk][k2] for k2 in ("id", "item") if _rl(x[kk].get(k2))]
    return out


def _ingredients(r):
    """{id или #тег: кол-во}"""
    found = {}

    def add(k, n=1):
        found[k] = found.get(k, 0) + n

    def walk(v, mult=1):
        if isinstance(v, dict):
            if "item" in v and _rl(v["item"]) and "count" not in v:
                add(v["item"], mult)
                return
            if "item" in v and _rl(v["item"]):
                add(v["item"], mult * int(v.get("count", 1)))
                return
            if "tag" in v and _rl(v["tag"]):
                add("#" + v["tag"], mult * int(v.get("count", v.get("amount", 1)) if isinstance(v.get("count", 1), int) else 1))
                return
            if v.get("type") == "neoforge:tag" and _rl(v.get("tag")):
                add("#" + v["tag"], 1)
                return
            for k, x in v.items():
                if k in _RES_KEYS or k in ("type", "conditions", "neoforge:conditions", "category", "group", "pattern",
                                           "processingTime", "energy", "time", "heat_requirement", "id"):
                    continue
                walk(x, mult)
        elif isinstance(v, list):
            for x in v:
                walk(x, mult)
        elif isinstance(v, str) and _rl(v) and not v.startswith("#"):
            add(v, mult)

    if "pattern" in r and isinstance(r.get("key"), dict):
        counts = {}
        for row in r["pattern"]:
            for ch in row:
                if ch != " ":
                    counts[ch] = counts.get(ch, 0) + 1
        for ch, ing in r["key"].items():
            n = counts.get(ch, 0)
            if not n:
                continue
            ings = ing if isinstance(ing, list) else [ing]
            first = ings[0] if ings else {}
            if isinstance(first, dict):
                if _rl(first.get("item")):
                    add(first["item"], n)
                elif _rl(first.get("tag")):
                    add("#" + first["tag"], n)
            elif isinstance(first, str) and _rl(first):
                add(first, n)
        return found
    walk({k: v for k, v in r.items() if k not in _RES_KEYS})
    return found


VANILLA_RU = Path("/root/.gradle/caches/neoformruntime/assets/objects/93/934028cb5d735a650a4544b6cf496752f5323967")


def build_index(jars, extra_dirs=()):
    """→ (recipes_by_output, names) — names: id → русское (или английское) имя."""
    by_out, names_ru, names_en = {}, {}, {}

    def consider(rel, data):
        try:
            r = json.loads(data)
        except Exception:
            return
        if not isinstance(r, dict):
            return
        t = r.get("type", "")
        if not isinstance(t, str) or any(s in t for s in SKIP_TYPES) or "neoforge:conditions" in r and any(
                c.get("type") == "neoforge:false" for c in r["neoforge:conditions"] if isinstance(c, dict)):
            return
        for out in _results(r):
            by_out.setdefault(out, []).append((rel, r))

    def lang(data, target):
        try:
            d = json.loads(data)
        except Exception:
            return
        for k, v in d.items():
            p = k.split(".")
            if len(p) == 3 and p[0] in ("item", "block"):
                target.setdefault(f"{p[1]}:{p[2]}", v)

    for j in jars:
        try:
            z = zipfile.ZipFile(j)
        except Exception:
            continue
        for n in z.namelist():
            if re.match(r"data/[^/]+/recipes?/.+\.json$", n):
                consider(n, z.read(n))
            elif n.endswith("/lang/ru_ru.json"):
                lang(z.read(n), names_ru)
            elif n.endswith("/lang/en_us.json"):
                lang(z.read(n), names_en)
    for d in extra_dirs:
        for f in Path(d).rglob("*.json"):
            s = str(f)
            if re.search(r"/data/[^/]+/recipes?/", s):
                consider(s, f.read_bytes())
            elif s.endswith("lang/ru_ru.json"):
                lang(f.read_bytes(), names_ru)
            elif s.endswith("lang/en_us.json"):
                lang(f.read_bytes(), names_en)
    if VANILLA_RU.exists():
        lang(VANILLA_RU.read_bytes(), names_ru)
    names = {**names_en, **names_ru}
    return by_out, names


def _name(names, x):
    if x.startswith("#"):
        tag = x[1:].split(":", 1)[1]
        return "любой «" + tag.replace("/", " / ").replace("_", " ") + "»"
    return names.get(x) or x.split(":", 1)[1].replace("_", " ")


CHEM = {"mekanism:antimatter": "антиматерия (химикат)", "mekanism:hydrogen": "водород", "mekanism:oxygen": "кислород",
        "mekanism:sulfuric_acid": "серная кислота", "mekanism:hydrogen_chloride": "хлороводород",
        "mekanism:polonium": "полоний", "mekanism:plutonium": "плутоний", "minecraft:water": "вода", "minecraft:lava": "лава"}


def _score(rel, r, item=None):
    t = r.get("type", "")
    s = 0
    if item:
        own = item.split(":")[0]
        m = re.search(r"data/([^/]+)/recipes?/", rel)
        if m and m.group(1) != own:
            s += 3
        ing = _ingredients(r)
        if len(ing) == 1:
            only, n = next(iter(ing.items()))
            if "block" in only or "storage_blocks" in only:
                s += 6
            if n in (4, 9) and ("nugget" in only or "shard" in only):
                s += 5
        if item in ing:
            s += 6
    if any(w in t for w in ("dye", "cutting", "upgrade", "copy", "clear", "productivebees", "centrifuge")):
        s += 4
    if "compat" in rel or "/integration/" in rel or "conditional" in rel:
        s += 5
    if t in ("minecraft:crafting_shaped", "minecraft:crafting_shapeless"):
        s -= 1
    if t not in MACHINES:
        s += 2
    return s


def explain(item, by_out, names, limit=6):
    """Одна-две строки «Как получить» для предмета."""
    if item in ITEM_GUIDE:
        return ITEM_GUIDE[item]
    recs = sorted(by_out.get(item, []), key=lambda x: _score(x[0], x[1], item))
    if not recs:
        nm = item.split(":", 1)[1]
        if "ore" in nm or nm.startswith("raw_"):
            return "&7Как получить:&r добывается из руды в мире (смотри JEI: наведи и нажми &eR&r)."
        return "&7Как получить:&r лут в мире или дроп мобов — в JEI наведи на предмет и нажми &eR&r / &eU&r."
    lines = []
    recs = [x for x in recs if not any(w in x[1].get("type", "") for w in ("empty", "fill", "spirit_trade"))] or recs
    for rel, r in recs[:2]:
        t = r.get("type", "")
        where = MACHINES.get(t) or ("Верстак" if t.endswith("crafting_shaped") or t.endswith("shaped")
                                    else "Верстак (без формы)" if t.endswith("shapeless") else None) or t.split(":")[0].replace("_", " ").title() + ": " + t.split(":")[-1].replace("_", " ")
        if t == "yukicore:altar":
            where = f"Алтарь Юки, уровень {r.get('tier', 1)} (центр + пьедесталы, ПКМ Морозным жезлом)"
        if t == "create:mixing" and r.get("heat_requirement"):
            where += " (нагрев: " + ("сильный — пылающая горелка на топливе" if r["heat_requirement"] == "superheated"
                                     else "горелка") + ")"
        ing = _ingredients(r)
        ing = {k: n for k, n in ing.items() if k.startswith("#") or k in names or k in CHEM}
        parts = [f"{n}× {CHEM.get(k) or _name(names, k)}" if n > 1 else (CHEM.get(k) or _name(names, k))
                 for k, n in list(ing.items())[:limit]]
        if len(ing) > limit:
            parts.append("…")
        if not parts:
            continue
        lines.append(f"&e{where}:&r " + ", ".join(parts))
        if len(lines) >= 1 and t.startswith("minecraft:crafting"):
            break
    if not lines:
        return "&7Как получить:&r смотри JEI — наведи на предмет и нажми &eR&r."
    return "&7Как получить:&r\n" + "\n".join(lines)
