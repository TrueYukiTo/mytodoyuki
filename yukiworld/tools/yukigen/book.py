"""Книга Patchouli «Летопись Юки»: сюжет сборки + справочник по YukiCore."""

BOOK = {
    "name": "Летопись Юки",
    "landing_text": "$(o)Когда зима ушла из мира, осталась только эта книга.$()$(br2)Здесь записана легенда о Юки, "
                    "духе зимы, и путь Хранителя, который должен вернуть ей Сердце.",
    "version": 1,
    "creative_tab": "yukicore:yukiworld",
    "use_resource_pack": True,
    "show_progress": False,
    "book_texture": "patchouli:textures/gui/book_cyan.png",
    "filler_texture": "patchouli:textures/gui/page_filler.png",
    "model": "patchouli:book_cyan",
}

CATEGORIES = {
    "legend": ("Легенда", "История мира YukiWorld и твоя роль в ней.", "yukicore:yuki_star", 0),
    "end": ("Острова Энда", "Где искать руды Юки.", "yukicore:yuki_crystal_ore", 1),
    "forge": ("Ледяная кузня", "Металлы, компоненты и снаряжение.", "yukicore:aurorium_ingot", 2),
    "cores": ("Ядра и Сердце", "Финальная цепочка YukiWorld.", "yukicore:yuki_heart", 3),
}

# (категория, id, название, иконка, страницы)
# страница: str -> текст; ("spot", item, text) -> предмет крупно
ENTRIES = [
    ("legend", "awakening", "Пробуждение", "minecraft:snowball", [
        "Когда-то над всеми мирами шёл тихий снег. Его хранила $(l)Юки$() — дух зимы, чьё $(#8fd8ff)Сердце$() "
        "держало в равновесии машины, магию, природу и границы измерений.",
        "Но Эндер-дракон позавидовал её свету. Он расколол Сердце, и осколки рассыпались по $(#ffb3e6)дальним "
        "островам Энда$(). С тех пор зима ушла, а мир начал медленно разрушаться.",
        "Ты проснулся с этой книгой в руках. В её страницах шепчет голос Юки. Она верит, что ты — $(l)Хранитель$(): "
        "тот, кто соберёт силу всех миров и вернёт ей Сердце.",
    ]),
    ("legend", "path", "Путь Хранителя", "minecraft:compass", [
        "Юки не может дать тебе силу — только направление. Чтобы дойти до Энда, тебе придётся освоить $(l)машины$(), "
        "$(l)магию$(), $(l)фермы$() и $(l)чужие миры$().",
        "Открой книгу квестов (FTB Quests). Каждая глава — шаг легенды. Квесты идут по очереди: следующий "
        "открывается, только когда закончен предыдущий.",
        "В конце пути тебя ждёт $(#ffb3e6)Бесконечная звезда Юки$() — и благодарность духа зимы.",
    ]),
    ("legend", "four_powers", "Четыре силы", "yukicore:frost_core", [
        "Сила Сердца распалась на четыре части. Каждую нужно собрать заново в виде $(l)Ядра$():",
        "$(li)$(#f0a030)Технологическое ядро$() — разум машин$(li)$(#a65cf0)Магическое ядро$() — шёпот заклинаний"
        "$(li)$(#5cc060)Ядро природы$() — дыхание жизни$(li)$(#22c2c8)Ядро измерений$() — память миров",
        "Все четыре строятся вокруг $(l)Морозного ядра$(), а его можно создать только из руды Энда.",
    ]),
    ("end", "end_ores", "Руды Энда", "yukicore:frost_ore", [
        "Осколки Сердца упали на $(l)внешние острова$() Энда — за главным островом дракона. Найди их через "
        "шлюз, который открывается после победы над драконом.",
        ("spot", "yukicore:frost_ore", "$(l)Морозная руда$() — частая. Нужна алмазная кирка. Даёт необработанный "
                                        "мороз → морозный слиток."),
        ("spot", "yukicore:yuki_crystal_ore", "$(l)Руда кристалла Юки$() — светится. Кристаллы — основа "
                                               "Эссенции и Криосхем."),
        ("spot", "yukicore:starlight_ore", "$(l)Руда звёздного света$() — очень редкая. Плавится только в "
                                            "доменной печи. Нужна для Целестия."),
    ]),
    ("end", "star_shards", "Осколки звезды", "yukicore:star_shard", [
        "Самые сильные существа миров проглотили частицы света Юки. Победи их, чтобы вернуть $(l)Осколки звезды$():",
        "$(li)Иссушитель — 1–2 осколка$(li)Хранитель (Warden) — 1 осколок$(li)Древний страж — 1 осколок",
        "Для финала понадобится $(l)14 осколков$(). Готовь фермы Иссушителя и броню получше.",
    ]),
    ("forge", "components", "Компоненты", "yukicore:frost_mechanism", [
        "Магия Юки капризна: её материалы рождаются только в машинах разных модов.",
        ("spot", "yukicore:frost_plate", "$(l)Морозная пластина$() — механический пресс Create."),
        ("spot", "yukicore:yuki_essence", "$(l)Эссенция Юки$() — камера наполнения Ars Nouveau: кристалл Юки, "
                                          "голубой лёд, снег, эссенция воды, 3000 источника."),
        ("spot", "yukicore:cryo_circuit", "$(l)Криосхема$() — высекатель AE2: инженерная плата + кристалл Юки + "
                                          "морозная пластина."),
        ("spot", "yukicore:frost_mechanism", "$(l)Морозный механизм$() — последовательная сборка Create из точного "
                                             "механизма: пластина → криосхема → кристалл → пресс, 3 круга."),
    ]),
    ("forge", "metals", "Три металла", "yukicore:celestium_ingot", [
        ("spot", "yukicore:frost_ingot", "$(l)Морозный слиток$() — переплавка руды Энда."),
        ("spot", "yukicore:aurorium_ingot", "$(l)Ауроний$() — смешивание Create с $(l)перегревом$(): 2 морозных "
                                            "слитка, эссенция Юки, очищенный обсидиан Mekanism, незеритовый "
                                            "лом и 500 мБ лавы."),
        ("spot", "yukicore:celestium_ingot", "$(l)Целестий$() — заряжающий стержень Powah, 25 млн FE: ауроний, "
                                             "осколок звёздного света, нитро-кристалл, кристалл Энда, эссенция."),
    ]),
    ("forge", "gear", "Снаряжение", "yukicore:celestium_chestplate", [
        "Снаряжение улучшается на $(l)кузнечном столе$() с шаблонами, как незерит: "
        "$(br)алмаз → $(#8fd8ff)мороз$() → $(#35d6b4)ауроний$() → $(#f2cf55)целестий$().",
        "Полный комплект брони даёт бонусы:$(li)Мороз: скорость$(li)Ауроний: спешка II, сила, огнестойкость"
        "$(li)Целестий: всё сразу — ночное зрение, дыхание, регенерация, сила II, спешка III, скорость II",
    ]),
    ("cores", "cores", "Четыре Ядра", "yukicore:tech_core", [
        "Ядра собираются на $(l)механическом крафтере Create 7×7$(). Каждое требует лучшие предметы своих модов "
        "вокруг Морозного ядра. Точные схемы смотри в JEI.",
        ("spot", "yukicore:tech_core", "Mekanism, AE2, Create, Powah, IE, Refined Storage, Oritech, Industrial "
                                       "Foregoing, ExtendedAE, Just Dire Things."),
        ("spot", "yukicore:magic_core", "Ars Nouveau, Iron's Spells, Occultism, Apotheosis, EvilCraft."),
        ("spot", "yukicore:nature_core", "Mystical Agriculture, Bumblezone, Productive Bees, Farmer's Delight, "
                                         "Сумеречный лес, Artifacts."),
        ("spot", "yukicore:dimension_core", "Звёзды Незера, Энд, Древний город, Испытания, Aether, Сумеречный лес."),
    ]),
    ("cores", "finale", "Сердце и Звезда", "yukicore:yuki_infinity_star", [
        ("spot", "yukicore:yuki_heart", "$(l)Сердце YukiWorld$() — крафтер 9×9: четыре Ядра, 4 антиматерии, "
                                        "8 осколков звезды, кристаллы времени, звёздный свет и целестий."),
        ("spot", "yukicore:unstable_infinity_star", "$(l)Нестабильная звезда$() — крафтер 9×9: Сердце, сияние и "
                                                    "теневая сталь Create, сингулярности AE2, полоний, пробуждённый "
                                                    "супремиум."),
        ("spot", "yukicore:yuki_infinity_star", "$(l)Бесконечная звезда Юки$() — антипротонный нуклеосинтезатор "
                                                "Mekanism стабилизирует её 1000 мБ антиматерии."),
        "В инвентаре звезда даёт полёт и защиту. А ещё она — ключ к $(l)креативным$() батареям Mekanism, AE2, "
        "Powah, IE и мотору Create, и при этом не тратится.",
    ]),
]


def files():
    """Возвращает {относительный путь: json} для data/ и assets/."""
    out = {"data/yukicore/patchouli_books/chronicle/book.json": BOOK}
    for lang in ("ru_ru", "en_us"):
        base = f"assets/yukicore/patchouli_books/chronicle/{lang}"
        for cid, (name, desc, icon, sort) in CATEGORIES.items():
            out[f"{base}/categories/{cid}.json"] = {"name": name, "description": desc, "icon": icon,
                                                    "sortnum": sort}
        for i, (cat, eid, name, icon, pages) in enumerate(ENTRIES):
            pj = []
            for p in pages:
                if isinstance(p, tuple):
                    pj.append({"type": "patchouli:spotlight", "item": p[1], "text": p[2]})
                else:
                    pj.append({"type": "patchouli:text", "text": p})
            out[f"{base}/entries/{cat}/{eid}.json"] = {"name": name, "category": f"yukicore:{cat}", "icon": icon,
                                                       "sortnum": i, "pages": pj}
    return out
