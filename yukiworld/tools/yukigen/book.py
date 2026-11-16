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
    "altars": ("Алтари Юки", "Мультиструктура в три уровня и ритуалы.", "yukicore:yuki_altar", 4),
    "stars": ("Система Юки", "Пять планет и их стражи.", "yukicore:stellar_shard", 5),
    "workshop": ("Мастерская", "Полезные изобретения Юки.", "yukicore:frost_magnet", 6),
    "newyear": ("Новый год", "Праздник, подарки и угощения.", "yukicore:yuki_tree", 7),
    "industry": ("Индустрия Юки", "Морозная кузня и Звёздный реактор.", "yukicore:frost_forge", 8),
    "sintara": ("Синтара", "Хранительница северного сияния.", "yukicore:sintara_staff", 9),
}


def cube_multiblock(controller, casing, core, crown):
    """Куб 3×3×3: контроллер в центре передней грани (z=-1), ядро в центре, венцы в верхних углах."""
    top = ["WCW", "CCC", "WCW"]
    mid = ["C0C", "CXC", "CCC"]
    bot = ["CCC", "CCC", "CCC"]
    return {"mapping": {"0": controller, "C": casing, "X": core, "W": crown},
            "pattern": [top, mid, bot], "symmetrical": False}


def _grid(n, cells):
    """n×n слой: cells — {(dx, dz): символ}, остальное — '_' (что угодно)."""
    h = n // 2
    return ["".join(cells.get((x, z), "_") for x in range(-h, h + 1)) for z in range(-h, h + 1)]


def altar_multiblock(tier):
    n = 13 if tier == 3 else 7
    peds = [(3, 0), (-3, 0), (0, 3), (0, -3)] + ([(3, 3), (-3, 3), (3, -3), (-3, -3)] if tier >= 2 else [])
    diag = [(2, 2), (-2, 2), (2, -2), (-2, -2)]
    obel = [(6, 0), (-6, 0), (0, 6), (0, -6)] if tier == 3 else []
    body, top = ("C", "S") if tier >= 2 else ("F", "L")
    layers = []
    if obel:
        layers.append(_grid(n, {o: "L" for o in obel}))
    layers.append(_grid(n, {**{d: top for d in diag}, **{o: "C" for o in obel}}))
    layers.append(_grid(n, {**{d: body for d in diag}, **{o: "C" for o in obel}}))
    base = {**{d: body for d in diag}, **{p: "P" for p in peds}, **{o: "C" for o in obel}, (0, 0): "0"}
    layers.append(_grid(n, base))
    return {"mapping": {"0": "yukicore:yuki_altar", "P": "yukicore:yuki_pedestal", "F": "yukicore:frost_block",
                        "L": "yukicore:yuki_lamp", "C": "yukicore:celestium_block", "S": "yukicore:starlight_block"},
            "pattern": layers, "symmetrical": True}

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


ENTRIES += [
    ("altars", "altar_1", "I · Морозный алтарь", "yukicore:frost_block", [
        "Алтарь Юки — мультиструктура. В центре — $(l)Алтарь$(), вокруг — $(l)пьедесталы$() и колонны.$(br2)"
        "ПКМ предметом по алтарю или пьедесталу — положить, пустой рукой — забрать. "
        "$(l)Морозный жезл$() запускает ритуал.",
        ("multiblock", 1, "4 пьедестала по сторонам (r=3), 4 колонны по диагонали: 2 морозных блока + лампа Юки."),
    ]),
    ("altars", "altar_2", "II · Звёздный алтарь", "yukicore:starlight_block", [
        ("multiblock", 2, "8 пьедесталов. Колонны: 2 целестиевых блока + блок звёздного света."),
        "Ритуалы II уровня: печати стражей Авроры, Пиролита и Никты, умножение аурония, калорит из пиролита, "
        "звезда Незера из пустоты и $(l)книги выше максимума$(): Острота VII, Эффективность VII, Защита VI…",
    ]),
    ("altars", "altar_3", "III · Бесконечность", "yukicore:celestium_block", [
        ("multiblock", 3, "Уровень II + 4 обелиска в 6 блоках: 3 целестиевых блока + лампа Юки."),
        "Только Алтарь Бесконечности может создать $(l)Печать Тени$(), $(l)Корону Юки$() и "
        "$(l)Клинок Вечной Зимы$().",
    ]),
    ("stars", "system", "Система Юки", "yukicore:stellar_shard", [
        "Пять планет у далёкой звезды. Добраться можно только на $(l)ракете IV уровня$() Ad Astra.",
        "$(li)$(#7cc8f0)Юкия$() — лёд, можно дышать. Страж: Ледяной Колосс."
        "$(li)$(#c7a8ff)Аврора$() — аметист и кальцит. Страж: Королева Метелей."
        "$(li)$(#f07a1c)Пиролит$() — лава и пепел, 280°. Страж: Пепельный Владыка."
        "$(li)$(#a070f0)Никта$() — вечная ночь. Страж: Пустотный Змей."
        "$(li)$(#ffe070)Целестия$() — золотой свет. Здесь прячется Тень Юки.",
    ]),
    ("stars", "bosses", "Стражи и печати", "yukicore:sigil_colossus", [
        "Стража призывает $(l)Печать$(), созданная на Алтаре. Печать работает только на планете стража.",
        ("spot", "yukicore:sigil_colossus", "Ледяной Колосс: 900 ❤. Линии клыков, удар по земле."),
        ("spot", "yukicore:sigil_queen", "Королева Метелей: 1100 ❤. Заморозка, телепорты, свита."),
        ("spot", "yukicore:sigil_ash_lord", "Пепельный Владыка: 1350 ❤. Огненные новы, ифриты."),
        ("spot", "yukicore:sigil_wyrm", "Пустотный Змей: 1500 ❤. Снаряды шалкеров, тьма, иссушение."),
        ("spot", "yukicore:sigil_shadow", "Тень Юки: 2400 ❤. Всё сразу и молнии во второй фазе."),
        "Когда здоровье стража падает ниже половины, он впадает в $(c)ярость$() — способности вдвое чаще.",
    ]),
    ("stars", "crown", "Корона Юки", "yukicore:crown_of_yuki", [
        ("spot", "yukicore:crown_of_yuki", "Финальная награда. Полёт, сопротивление III, сила III, "
                                           "регенерация II и все бонусы целестия."),
        ("spot", "yukicore:eternal_winter_blade", "Клинок Вечной Зимы: неразрушимый, урон 63."),
    ]),
]

ENTRIES += [
    ("workshop", "tools", "Инструменты", "yukicore:frost_magnet", [
        ("spot", "yukicore:frost_magnet", "$(l)Морозный магнит$(): ПКМ — вкл/выкл, притягивает предметы и опыт "
                                          "в радиусе 8. $(l)Небесный магнит$() (Алтарь II) — радиус 16."),
        ("spot", "yukicore:celestium_hammer", "$(l)Молоты$() копают 3×3 перпендикулярно грани. С Shift — "
                                              "один блок."),
        ("spot", "yukicore:snow_boots", "$(l)Снежные ботинки$(): рыхлый снег, скорость на льду, ледяной мост."),
    ]),
    ("workshop", "machines", "Машины", "yukicore:yuki_chrono", [
        ("spot", "yukicore:cryo_generator", "$(l)Криогенератор$(): энергия от льда на 6 сторонах. Снег 5, лёд 10, "
                                            "плотный 20, синий 40 FE/т за сторону. Сам отдаёт FE соседям."),
        ("spot", "yukicore:yuki_chrono", "$(l)Часы Юки$(): машины и растения в кубе 3×3×3 работают в 4 раза "
                                         "быстрее. Сигнал редстоуна выключает часы."),
    ]),
    ("newyear", "holiday", "С Новым годом!", "yukicore:gift", [
        "В мире без зимы люди всё ещё празднуют Новый год. $(l)Адвент-календарь$() дарит подарок каждый день, "
        "а каждый седьмой — $(l)Большой подарок$().",
        ("spot", "yukicore:gift", "Подарки падают со зимних бродяг и снежных големов и даются за главы квестов."),
        ("spot", "yukicore:olivier", "Угощения: мандарины (в еловой листве), леденцы, пряники, горячий шоколад и "
                                     "оливье — у каждого свой эффект."),
        ("spot", "yukicore:yuki_tree", "Украшения: гирлянды, ёлочные игрушки, ёлочки и снежные шары светятся."),
    ]),
]

ENTRIES += [
    ("industry", "frost_forge", "Морозная кузня", "yukicore:frost_forge", [
        "Мультиструктура 3×3×3. $(l)Контроллер$() — в центре боковой грани, в центре куба — $(l)синий лёд$(), "
        "в четырёх верхних углах — $(l)лампы Юки$(), всё остальное — $(l)морозная обшивка$().",
        ("multiblock_cube", "yukicore:frost_forge", "yukicore:frost_casing", "minecraft:blue_ice", "yukicore:yuki_lamp",
         "Контроллер можно поставить с любой стороны."),
        "ПКМ предметом — положить во вход (4 слота), пустой рукой — забрать результат, Shift+ПКМ — состояние. "
        "Принимает $(l)FE$() и предметы из труб, отдаёт результат в трубы.$(br2)Плавит $(l)криосталь$() и "
        "$(l)звёздный сплав$(), а ещё $(l)удваивает руды$() YukiCore.",
    ]),
    ("industry", "star_reactor", "Звёздный реактор", "yukicore:star_reactor", [
        ("multiblock_cube", "yukicore:star_reactor", "yukicore:reactor_casing", "yukicore:starlight_block",
         "yukicore:celestium_block", "Ядро — блок звёздного света, углы — целестий, остальное — обшивка реактора."),
        "Топливо (ПКМ или трубы): кристалл Юки — 400 FE/т, звёздный свет — 2 500, пиролит — 3 000, "
        "пустота — 5 000, звёздный осколок — $(l)10 000 FE/т$(). Энергию отдаёт во все стороны.",
    ]),
    ("sintara", "sintara", "Синтара", "yukicore:sintara_staff", [
        "$(o)Когда Юки была маленькой, по ночам к ней прилетала подруга — $(l)Синтара$(), хранительница "
        "северного сияния.$()$(br2)Пока Тень крала свет Юки, Синтара прятала его в небесных огнях.",
        ("spot", "yukicore:sintara_amulet", "$(l)Амулет Синтары$(): удача, ночное зрение, враги подсвечены."),
        ("spot", "yukicore:sintara_staff", "$(l)Посох Синтары$(): луч сияния и исцеляющее Сияние для друзей."),
        "$(l)♥ Эта глава посвящена другу — Sintara.$()",
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
                if isinstance(p, tuple) and p[0] == "multiblock_cube":
                    pj.append({"type": "patchouli:multiblock", "name": "3×3×3",
                               "multiblock": cube_multiblock(p[1], p[2], p[3], p[4]),
                               "enable_visualize": True, "text": p[5]})
                elif isinstance(p, tuple) and p[0] == "multiblock":
                    pj.append({"type": "patchouli:multiblock", "name": f"Уровень {p[1]}",
                               "multiblock": altar_multiblock(p[1]), "enable_visualize": True, "text": p[2]})
                elif isinstance(p, tuple):
                    pj.append({"type": "patchouli:spotlight", "item": p[1], "text": p[2]})
                else:
                    pj.append({"type": "patchouli:text", "text": p})
            out[f"{base}/entries/{cat}/{eid}.json"] = {"name": name, "category": f"yukicore:{cat}", "icon": icon,
                                                       "sortnum": i, "pages": pj}
    return out
