"""
Рецепты YukiCore 2.0.

Принцип как в All The Mods: каждый шаг требует машин из разных модов.
  Энд → сырьё → Create (пресс, сборка, мех. крафтер) → AE2 (пресс) → Ars Nouveau (наполнение)
      → Powah (заряжание) → Mekanism (нуклеосинтез антиматерией) → финал.
Все рецепты на машины других модов помечены условием neoforge:mod_loaded.
"""

M = "yukicore"


def I(n):  # noqa: E743
    return f"{M}:{n}"


def ing(x):
    """'mod:item' -> {"item"}, '#tag' -> {"tag"}"""
    return {"tag": x[1:]} if x.startswith("#") else {"item": x}


def when(recipe, *mods):
    recipe = dict(recipe)
    conds = recipe.pop("neoforge:conditions", [])
    conds = conds + [{"type": "neoforge:mod_loaded", "modid": m} for m in mods
                     if {"type": "neoforge:mod_loaded", "modid": m} not in conds]
    return {"neoforge:conditions": conds, **recipe}


def shaped(pattern, key, result, count=1, category="misc", components=None):
    res = {"id": result, "count": count}
    if components:
        res["components"] = components
    return {"type": "minecraft:crafting_shaped", "category": category, "pattern": pattern,
            "key": {k: ing(v) for k, v in key.items()}, "result": res}


def shapeless(items, result, count=1, components=None):
    res = {"id": result, "count": count}
    if components:
        res["components"] = components
    return {"type": "minecraft:crafting_shapeless", "category": "misc",
            "ingredients": [ing(i) for i in items], "result": res}


def cooking(kind, ingredient, result, xp, time):
    return {"type": f"minecraft:{kind}", "category": "misc", "ingredient": ing(ingredient),
            "result": {"id": result}, "experience": xp, "cookingtime": time}


def smithing(template, base, addition, result):
    return {"type": "minecraft:smithing_transform", "template": ing(template), "base": ing(base),
            "addition": ing(addition), "result": {"id": result, "count": 1}}


def mech(pattern, key, result, count=1):
    w = max(len(r) for r in pattern)
    pattern = [r.ljust(w) for r in pattern]
    used = set("".join(pattern)) - {" "}
    assert used == set(key), (result, used ^ set(key))
    return when({"type": "create:mechanical_crafting", "accept_mirrored": False, "category": "misc",
                 "key": {k: ing(v) for k, v in key.items()}, "pattern": pattern,
                 "result": {"count": count, "id": result}, "show_notification": True}, "create")



TOOLS = ["sword", "pickaxe", "axe", "shovel", "hoe"]
ARMOR = ["helmet", "chestplate", "leggings", "boots"]


def build():
    r = {}

    # ---------------------------------------------------------------- сырьё Энда
    r["frost_ingot_from_smelting"] = cooking("smelting", I("raw_frost"), I("frost_ingot"), 1.0, 300)
    r["frost_ingot_from_blasting"] = cooking("blasting", I("raw_frost"), I("frost_ingot"), 1.0, 150)
    r["yuki_crystal_from_blasting"] = cooking("blasting", I("yuki_crystal_ore"), I("yuki_crystal"), 1.5, 200)
    r["starlight_shard_from_blasting"] = cooking("blasting", I("starlight_ore"), I("starlight_shard"), 3.0, 400)

    for block, item in [("frost_block", "frost_ingot"), ("yuki_crystal_block", "yuki_crystal"),
                        ("starlight_block", "starlight_shard"), ("aurorium_block", "aurorium_ingot"),
                        ("celestium_block", "celestium_ingot")]:
        r[block] = shaped(["XXX", "XXX", "XXX"], {"X": I(item)}, I(block), category="building")
        r[f"{item}_from_{block}"] = shapeless([I(block)], I(item), 9)

    r["yuki_lamp"] = shaped(["GCG", "CLC", "GCG"], {"G": "minecraft:glass", "C": I("yuki_crystal"),
                                                    "L": "minecraft:glowstone"}, I("yuki_lamp"), 4, "building")

    # ---------------------------------------------------------------- компоненты (машины модов)
    r["frost_plate"] = when({"type": "create:pressing", "ingredients": [ing(I("frost_ingot"))],
                             "results": [{"id": I("frost_plate")}]}, "create")

    r["yuki_essence"] = when({"type": "ars_nouveau:imbuement", "input": ing(I("yuki_crystal")),
                              "output": {"count": 1, "id": I("yuki_essence")},
                              "pedestalItems": [ing("minecraft:blue_ice"), ing("ars_nouveau:water_essence"),
                                                ing("minecraft:snow_block")],
                              "source": 3000}, "ars_nouveau")

    r["cryo_circuit"] = when({"type": "ae2:inscriber", "mode": "press",
                              "ingredients": {"top": ing("ae2:printed_engineering_processor"),
                                              "middle": ing(I("yuki_crystal")),
                                              "bottom": ing(I("frost_plate"))},
                              "result": {"count": 1, "id": I("cryo_circuit")}}, "ae2")

    tr = I("incomplete_frost_mechanism")

    def step(kind, extra=None):
        ings = [ing(tr)] + ([ing(extra)] if extra else [])
        return {"type": f"create:{kind}", "ingredients": ings, "results": [{"id": tr}]}

    r["frost_mechanism"] = when({
        "type": "create:sequenced_assembly", "ingredient": ing("create:precision_mechanism"), "loops": 3,
        "results": [{"chance": 100.0, "id": I("frost_mechanism")}],
        "sequence": [step("deploying", I("frost_plate")), step("deploying", I("cryo_circuit")),
                     step("deploying", I("yuki_crystal")), step("pressing")],
        "transitional_item": {"id": tr}}, "create")

    r["aurorium_ingot"] = when({
        "type": "create:mixing", "heat_requirement": "superheated",
        "ingredients": [ing(I("frost_ingot")), ing(I("frost_ingot")), ing(I("yuki_essence")),
                        ing("mekanism:ingot_refined_obsidian"), ing("minecraft:netherite_scrap"),
                        {"type": "neoforge:tag", "amount": 500, "tag": "minecraft:lava"}],
        "results": [{"count": 1, "id": I("aurorium_ingot")}]}, "create", "mekanism")

    r["celestium_ingot"] = when({
        "type": "powah:energizing", "energy": 25_000_000,
        "ingredients": [ing(I("aurorium_ingot")), ing(I("starlight_shard")),
                        ing("powah:crystal_nitro"), ing("minecraft:end_crystal"), ing(I("yuki_essence"))],
        "result": {"count": 1, "id": I("celestium_ingot")}}, "powah")

    # ---------------------------------------------------------------- шаблоны улучшения
    r["frost_upgrade_template"] = shaped(["FCF", "CDC", "FCF"], {
        "F": I("frost_ingot"), "C": I("yuki_crystal"), "D": "minecraft:diamond_block"}, I("frost_upgrade_template"))
    r["frost_upgrade_template_duplicate"] = shaped(["CTC", "CEC", "CCC"], {
        "C": I("yuki_crystal"), "T": I("frost_upgrade_template"), "E": "minecraft:end_stone"},
        I("frost_upgrade_template"), 2)
    r["aurorium_upgrade_template"] = shaped(["AQA", "QTQ", "AQA"], {
        "A": I("aurorium_ingot"), "Q": I("cryo_circuit"), "T": I("frost_upgrade_template")},
        I("aurorium_upgrade_template"))
    r["aurorium_upgrade_template_duplicate"] = shaped(["ETE", "EAE", "EEE"], {
        "E": I("yuki_essence"), "T": I("aurorium_upgrade_template"), "A": I("aurorium_ingot")},
        I("aurorium_upgrade_template"), 2)
    r["celestium_upgrade_template"] = shaped(["CSC", "STS", "CSC"], {
        "C": I("celestium_ingot"), "S": I("starlight_block"), "T": I("aurorium_upgrade_template")},
        I("celestium_upgrade_template"))
    r["celestium_upgrade_template_duplicate"] = shaped(["STS", "SCS", "SSS"], {
        "S": I("starlight_shard"), "T": I("celestium_upgrade_template"), "C": I("celestium_ingot")},
        I("celestium_upgrade_template"), 2)

    # ---------------------------------------------------------------- кузница: алмаз → мороз → ауроний → целестий
    chain = [("diamond", "frost", "frost_ingot", "minecraft"),
             ("frost", "aurorium", "aurorium_ingot", M),
             ("aurorium", "celestium", "celestium_ingot", M)]
    for base, tier, addition, base_ns in chain:
        for piece in TOOLS + ARMOR:
            r[f"{tier}_{piece}_smithing"] = smithing(I(f"{tier}_upgrade_template"), f"{base_ns}:{base}_{piece}",
                                                     I(addition), I(f"{tier}_{piece}"))

    # ---------------------------------------------------------------- особые предметы
    r["yuki_star"] = mech([" CPC ", "CEBEC", "PBNBP", "CEBEC", " CPC "], {
        "C": I("yuki_crystal"), "P": I("frost_plate"), "E": I("yuki_essence"), "B": I("yuki_crystal_block"),
        "N": "minecraft:nether_star"}, I("yuki_star"))
    r["frost_apple"] = shaped(["ECE", "CAC", "ECE"], {
        "E": I("yuki_essence"), "C": I("yuki_crystal"), "A": "minecraft:golden_apple"}, I("frost_apple"), 2)

    # ---------------------------------------------------------------- ядра (механический крафтер Create)
    r["frost_core"] = mech([" BCB ", "BMQMB", "CQSQC", "BMQMB", " BCB "], {
        "B": I("frost_block"), "C": I("yuki_crystal_block"), "M": I("frost_mechanism"), "Q": I("cryo_circuit"),
        "S": I("yuki_star")}, I("frost_core"))

    r["tech_core"] = when(mech(
        ["UCSPSCU",
         "CMQAQMC",
         "NQJEJQN",
         "RAEKEAR",
         "OQJFJQO",
         "CMQXQMC",
         "UCSPSCU"], {
            "U": I("aurorium_block"), "C": "mekanism:ultimate_control_circuit", "S": "ae2:singularity",
            "P": "create:precision_mechanism", "M": I("frost_mechanism"), "Q": I("cryo_circuit"),
            "A": "mekanism:alloy_atomic", "N": "powah:capacitor_nitro", "J": "justdirethings:eclipsealloy_ingot",
            "E": "immersiveengineering:component_electronic_adv", "R": "refinedstorage:construction_core",
            "K": I("frost_core"), "O": "oritech:overcharged_crystal", "F": "industrialforegoing:machine_frame_supreme",
            "X": "extendedae:concurrent_processor"}, I("tech_core")),
        "mekanism", "ae2", "powah", "justdirethings", "immersiveengineering", "refinedstorage", "oritech",
        "industrialforegoing", "extendedae")

    r["magic_core"] = when(mech(
        ["UBWLWBU",
         "BZOGOZB",
         "HODYDOH",
         "LGYKYGL",
         "HODIDOH",
         "BZOVOZB",
         "UBWLWBU"], {
            "U": I("aurorium_block"), "B": "ars_nouveau:source_gem_block", "W": "ars_nouveau:wilden_tribute",
            "L": "irons_spellbooks:legendary_ink", "Z": I("yuki_essence"), "O": "occultism:afrit_essence",
            "G": "occultism:spirit_attuned_gem", "H": "irons_spellbooks:cinder_essence",
            "D": "irons_spellbooks:divine_pearl", "Y": "irons_spellbooks:dragonskin", "K": I("frost_core"),
            "I": "apothic_enchanting:infused_breath", "V": "evilcraft:dark_gem"}, I("magic_core")),
        "ars_nouveau", "irons_spellbooks", "occultism", "apothic_enchanting", "evilcraft")

    r["nature_core"] = when(mech(
        ["UTSRSTU",
         "TZBJBZT",
         "SPGEGPS",
         "RJEKEJR",
         "SPGWGPS",
         "TZBHBZT",
         "UTSRSTU"], {
            "U": I("aurorium_block"), "T": "twilightforest:steeleaf_ingot",
            "S": "mysticalagriculture:awakened_supremium_ingot", "R": "farmersdelight:rich_soil",
            "Z": I("yuki_essence"), "B": "the_bumblezone:essence_of_the_bees",
            "J": "the_bumblezone:royal_jelly_block", "P": "minecraft:honey_block",
            "G": "minecraft:enchanted_golden_apple", "E": "artifacts:everlasting_beef", "K": I("frost_core"),
            "W": "minecraft:totem_of_undying", "H": "productivebees:honey_treat"}, I("nature_core")),
        "twilightforest", "mysticalagriculture", "farmersdelight", "the_bumblezone", "artifacts", "productivebees")

    r["dimension_core"] = when(mech(
        ["UNCLCNU",
         "SZGHGZS",
         "CGFMFGC",
         "LHMKMHL",
         "CGFDFGC",
         "SZGEGZS",
         "UNCLCNU"], {
            "U": I("aurorium_block"), "N": "minecraft:nether_star", "C": "twilightforest:carminite",
            "L": I("starlight_block"), "S": "minecraft:shulker_shell", "Z": I("yuki_essence"),
            "G": "aether:enchanted_gravitite", "H": "minecraft:heavy_core", "F": "twilightforest:fiery_ingot",
            "M": "twilightforest:knightmetal_block", "K": I("frost_core"), "D": "minecraft:dragon_breath",
            "E": "minecraft:echo_shard"}, I("dimension_core")),
        "twilightforest", "aether")

    # ---------------------------------------------------------------- финал (9×9)
    r["yuki_heart"] = when(mech(
        ["    X    ",
         "   SAS   ",
         "  ZLULZ  ",
         " SLTHMLS ",
         "XAUQYPUAX",
         " SLNVDLS ",
         "  ZLULZ  ",
         "   SAS   ",
         "    X    "], {
            "X": I("celestium_block"), "S": I("star_shard"), "A": "mekanism:pellet_antimatter",
            "Z": I("yuki_essence"), "L": I("starlight_block"), "U": "justdirethings:time_crystal",
            "T": I("tech_core"), "M": I("magic_core"), "N": I("nature_core"), "D": I("dimension_core"),
            "H": I("colossus_heart"), "Q": I("blizzard_crown"), "P": I("ash_core"), "V": I("void_scale"),
            "Y": I("yuki_star")}, I("yuki_heart")),
        "mekanism", "justdirethings")

    r["unstable_infinity_star"] = when(mech(
        ["R   X   R",
         " H  Q  H ",
         "  WSPSW  ",
         "   PEP   ",
         "XQSEYESQX",
         "   PEP   ",
         "  WSPSW  ",
         " H  Q  H ",
         "R   X   R"], {
            "R": "create:refined_radiance", "H": "create:shadow_steel", "X": I("celestium_block"),
            "Q": "ae2:singularity", "W": "mekanism:pellet_polonium", "S": I("star_shard"),
            "P": "mysticalagriculture:awakened_supremium_block", "E": I("starlight_block"),
            "Y": I("yuki_heart")}, I("unstable_infinity_star")),
        "ae2", "mekanism", "mysticalagriculture")

    r["yuki_infinity_star"] = when({
        "type": "mekanism:nucleosynthesizing", "chemical_input": {"amount": 1000, "chemical": "mekanism:antimatter"},
        "duration": 6000, "item_input": {"count": 1, "item": I("unstable_infinity_star")},
        "output": {"count": 1, "id": I("yuki_infinity_star")}, "per_tick_usage": False}, "mekanism")

    r["yuki_blade"] = mech(
        ["   X   ",
         "  SCS  ",
         "  CWC  ",
         "  SCS  ",
         "  ZKZ  ",
         "  ZHZ  ",
         "   H   "], {
            "X": "minecraft:nether_star", "S": I("star_shard"), "C": I("celestium_block"),
            "W": I("celestium_sword"), "Z": I("yuki_essence"), "K": I("starlight_block"),
            "H": "minecraft:netherite_ingot"}, I("yuki_blade"))

    # ---------------------------------------------------------------- креатив (как ATM Star: звезда не расходуется)
    creative = {
        "mekanism:creative_energy_cube": ("mekanism:ultimate_energy_cube", "mekanism"),
        "ae2:creative_energy_cell": ("ae2:dense_energy_cell", "ae2"),
        "powah:energy_cell_creative": ("powah:energy_cell_nitro", "powah"),
        "create:creative_motor": ("create:steam_engine", "create"),
        "immersiveengineering:capacitor_creative": ("immersiveengineering:capacitor_hv", "immersiveengineering"),
    }
    for out, (base, mod) in creative.items():
        r["creative/" + out.split(":")[1]] = when(shaped(["CBC", "BSB", "CBC"], {
            "C": I("celestium_block"), "B": base, "S": I("yuki_infinity_star")}, out), mod)

    # ---------------------------------------------------------------- 3.0: планеты, алтарь, ритуалы
    r.update(build_v3())

    # ---------------------------------------------------------------- книга-летопись
    r["chronicle"] = when(shapeless(["minecraft:book", "minecraft:snowball"], "patchouli:guide_book",
                                    components={"patchouli:book": "yukicore:chronicle"}), "patchouli")
    return r


def ingredient_ids(recipes):
    """Все предметы, на которые ссылаются рецепты (для проверки)."""
    out = set()

    def walk(v):
        if isinstance(v, dict):
            if "item" in v and isinstance(v["item"], str):
                out.add(v["item"])
            for x in v.values():
                walk(x)
        elif isinstance(v, list):
            for x in v:
                walk(x)
    for rec in recipes.values():
        walk(rec)
        res = rec.get("result") or rec.get("output")
        if isinstance(res, dict) and "id" in res:
            out.add(res["id"])
    return out


def altar(tier, center, pedestals, result, count=1, duration=100, components=None):
    res = {"id": result, "count": count}
    if components:
        res["components"] = components
    return {"type": "yukicore:altar", "tier": tier, "center": ing(center),
            "pedestals": [ing(p) for p in pedestals], "result": res, "duration": duration}


def book(enchant, level):
    return {"minecraft:stored_enchantments": {"levels": {enchant: level}}}


def build_v3():
    r = {}
    # планетарные руды
    for ore, out, xp in [("permafrost_ore", "permafrost_crystal", 2.0), ("aurora_ore", "aurora_shard", 2.5),
                         ("pyrolite_ore", "raw_pyrolite", 2.0), ("void_ore", "void_shard", 3.0),
                         ("stellar_ore", "stellar_shard", 4.0)]:
        r[f"{out}_from_{ore}"] = cooking("blasting", I(ore), I(out), xp, 200)
    r["pyrolite_ingot_from_blasting"] = cooking("blasting", I("raw_pyrolite"), I("pyrolite_ingot"), 2.0, 200)
    for block, item in [("permafrost_block", "permafrost_crystal"), ("aurora_block", "aurora_shard"),
                        ("pyrolite_block", "pyrolite_ingot"), ("void_block", "void_shard"),
                        ("stellar_block", "stellar_shard")]:
        r[block] = shaped(["XXX", "XXX", "XXX"], {"X": I(item)}, I(block), category="building")
        r[f"{item}_from_{block}"] = shapeless([I(block)], I(item), 9)

    # мультиструктура
    r["yuki_altar"] = shaped(["EYE", "QBQ", "PPP"], {
        "E": I("yuki_essence"), "Y": I("yuki_star"), "Q": I("cryo_circuit"), "B": I("frost_block"),
        "P": I("frost_plate")}, I("yuki_altar"))
    r["yuki_pedestal"] = shaped(["CEC", " B ", "PPP"], {
        "C": I("yuki_crystal"), "E": I("yuki_essence"), "B": I("frost_block"), "P": I("frost_plate")},
        I("yuki_pedestal"), 2)
    r["frost_wand"] = shaped(["  C", " I ", "I  "], {"C": I("yuki_crystal_block"), "I": I("frost_ingot")},
                             I("frost_wand"))

    a = {}
    # --- уровень I (4 пьедестала)
    a["essence"] = altar(1, I("yuki_crystal"), ["minecraft:blue_ice", "minecraft:snow_block",
                                                "minecraft:blue_ice", "minecraft:snow_block"], I("yuki_essence"), 2)
    a["frost_apple"] = altar(1, "minecraft:golden_apple", [I("yuki_crystal")] * 4, I("frost_apple"), 3)
    a["starlight"] = altar(1, I("yuki_crystal_block"), ["minecraft:ender_pearl"] * 4, I("starlight_shard"), 2, 160)
    a["permafrost_crystals"] = altar(1, I("permafrost_crystal"), ["minecraft:snow_block"] * 4, I("yuki_crystal"), 4)
    a["sigil_colossus"] = altar(1, I("yuki_star"), [I("permafrost_crystal")] * 4, I("sigil_colossus"), 1, 200)
    # --- уровень II (8 пьедесталов)
    a["sigil_queen"] = altar(2, I("colossus_heart"), [I("aurora_shard")] * 4 + [I("yuki_essence")] * 2
                             + [I("celestium_ingot")] * 2, I("sigil_queen"), 1, 300)
    a["sigil_ash_lord"] = altar(2, I("blizzard_crown"), [I("pyrolite_ingot")] * 4 + ["ad_astra:calorite_ingot"] * 2
                                + ["minecraft:magma_cream"] * 2, I("sigil_ash_lord"), 1, 300)
    a["sigil_wyrm"] = altar(2, I("ash_core"), [I("void_shard")] * 4 + ["minecraft:echo_shard"] * 2
                            + ["minecraft:dragon_breath"] * 2, I("sigil_wyrm"), 1, 300)
    a["aurorium_multiply"] = altar(2, I("aurorium_block"), [I("aurora_shard")] * 8, I("aurorium_block"), 2, 240)
    a["calorite"] = altar(2, I("pyrolite_ingot"), ["ad_astra:ostrum_ingot"] * 4, "ad_astra:calorite_ingot", 2, 160)
    a["nether_star"] = altar(2, "minecraft:wither_skeleton_skull", [I("void_shard")] * 4 + ["minecraft:soul_sand"] * 4,
                             "minecraft:nether_star", 1, 400)
    books = [("sharpness", "minecraft:sharpness", 7, "minecraft:netherite_sword"),
             ("efficiency", "minecraft:efficiency", 7, "minecraft:netherite_pickaxe"),
             ("protection", "minecraft:protection", 6, "minecraft:netherite_chestplate"),
             ("fortune", "minecraft:fortune", 5, "minecraft:emerald_block"),
             ("looting", "minecraft:looting", 5, "minecraft:wither_skeleton_skull"),
             ("unbreaking", "minecraft:unbreaking", 5, "minecraft:obsidian"),
             ("feather_falling", "minecraft:feather_falling", 6, "minecraft:phantom_membrane"),
             ("power", "minecraft:power", 7, "minecraft:bow")]
    for name, ench, lvl, cat in books:
        a[f"book_{name}"] = altar(2, "minecraft:book", [cat, cat, I("aurorium_ingot"), I("aurorium_ingot"),
                                                        I("yuki_essence"), I("yuki_essence"),
                                                        "minecraft:experience_bottle", "minecraft:experience_bottle"],
                                  "minecraft:enchanted_book", 1, 200, book(ench, lvl))
    # --- уровень III (Алтарь Бесконечности)
    a["sigil_shadow"] = altar(3, I("stellar_block"), [I("colossus_heart"), I("blizzard_crown"), I("ash_core"),
                                                      I("void_scale")] + [I("stellar_shard")] * 4,
                              I("sigil_shadow"), 1, 600)
    a["crown_of_yuki"] = altar(3, I("yuki_tear"), [I("celestium_block")] * 4 + [
        I("stellar_block"), I("aurora_block"), I("permafrost_block"), I("void_block")], I("crown_of_yuki"), 1, 800)
    a["eternal_winter_blade"] = altar(3, I("yuki_blade"), [I("yuki_tear"), I("stellar_block"), I("void_block"),
                                                           I("pyrolite_block"), I("permafrost_block"),
                                                           I("aurora_block"), I("celestium_block"),
                                                           I("celestium_block")], I("eternal_winter_blade"), 1, 800)
    for k, v in a.items():
        r["altar/" + k] = v
    return r
