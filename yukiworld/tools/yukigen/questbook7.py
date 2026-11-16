"""YukiWorld 7.0 «Вечная зима»: ME-сеть Юки (AE2), лучшая броня, путь архимага."""
from .questbook import C, D, K  # noqa: F401

Y = "yukicore:"
AE = "ae2:"

CHAPTERS7 = [
    ("me_network", "💠 ME-сеть Юки", "Applied Energistics 2 и его аддоны", AE + "controller", 1200, [
        (C(AE + "controller"), 1, "Письмо Юки: память кристаллов",
         "&b«Кристаллы помнят всё. Научи их хранить твои сокровища — и мастерская станет бесконечной.»&r", None),
        (AE + "certus_quartz_crystal", 32, "Истинный кварц", "", None),
        (AE + "inscriber", 1, "Высекатель", "Печатает процессоры. Морозная кузня делает их дешевле — за энергию.", None),
        (AE + "fluix_crystal", 64, "Флюикс", "Кузня: 2 кварца + редстоун + кристалл Юки → 6 флюикса.", None),
        (AE + "logic_processor", 16, "Логические процессоры", "", None),
        (AE + "engineering_processor", 16, "Инженерные процессоры", "", None),
        (AE + "calculation_processor", 16, "Вычислительные процессоры", "", None),
        (AE + "controller", 1, "ME-контроллер", "Сердце сети.", None),
        (AE + "drive", 2, "ME-приводы", "", None),
        (AE + "item_storage_cell_64k", 4, "Ячейки 64k", "", None),
        (AE + "molecular_assembler", 4, "Молекулярные сборщики", "Автокрафт!", None),
        (AE + "pattern_provider", 4, "Поставщики шаблонов", "", None),
        ("extendedae:ex_pattern_provider", 2, "Расширенный поставщик", "ExtendedAE: больше слотов.", None),
        ("megacells:item_storage_cell_1m", 1, "Мега-ячейка 1M", "MEGA Cells: ячейки до 256M.", None),
        ("appflux:flux_accessor", 1, "Флакс-аксессор", "Applied Flux: храни энергию FE прямо в ME-сети.", None),
        ("appmek:chemical_storage_cell_4k", 1, "Ячейка химикатов", "Applied Mekanistics: газы Mekanism в ME.", None),
        ("merequester:requester", 1, "ME-заказчик", "ME Requester: держит запас предметов на автокрафте.", None),
        (AE + "wireless_access_point", 1, "Беспроводной доступ", "", None),
        (AE + "singularity", 4, "Сингулярности", "Кузня: 16 материальных шаров + звёздный осколок.", None),
        (AE + "quantum_link", 2, "Квантовая связь", "Сеть между мирами — даже на планетах Юки.",
         (Y + "stellar_alloy", 2)),
    ]),
    ("eternal", "🛡 Вечная зима", "Сильнейшая броня сборки", Y + "eternal_chestplate", 1200, [
        (C(Y + "eternal_chestplate"), 1, "Письмо Юки: доспех вечной зимы",
         "&b«Моя броня не просто держит удар — она отнимает у него силу. Каждая часть снижает любой урон.»&r\n\n"
         "Мороз −15% · Аврорий −25% · Целестий −42% · Регалии −50% · &bВечная зима −75%&r", None),
        (Y + "celestium_helmet", 1, "Целестиевый шлем", "Основа для Вечной зимы.", None),
        (Y + "celestium_chestplate", 1, "Целестиевая кираса", "", None),
        (Y + "celestium_leggings", 1, "Целестиевые поножи", "", None),
        (Y + "celestium_boots", 1, "Целестиевые сапоги", "", None),
        (Y + "yuki_tear", 4, "Слёзы Юки", "По одной на каждую часть.", None),
        (Y + "stellar_alloy", 8, "Звёздный сплав", "", None),
        (Y + "eternal_helmet", 1, "Шлем Вечной зимы", "Алтарь III: шлем + Слеза + Сердце Колосса + сплав + осколки.", None),
        (Y + "eternal_chestplate", 1, "Кираса Вечной зимы", "Алтарь III: + Корона Метели.", None),
        (Y + "eternal_leggings", 1, "Поножи Вечной зимы", "Алтарь III: + Пепельное ядро.", None),
        (Y + "eternal_boots", 1, "Сапоги Вечной зимы", "Алтарь III: + Чешуя Пустоты.",
         (Y + "grand_gift", 3)),
    ]),
    ("archmage", "✦ Архимаг", "Регалии, тома и Аватары стихий", Y + "yuki_regalia_robe", 1200, [
        (C(Y + "yuki_regalia_robe"), 1, "Письмо Юки и Синтары: архимаг",
         "&b«Ты прошёл всё дерево льда…»&r &6«…и пламени!»&r Осталось последнее — стать Аватаром.", None),
        (Y + "greater_frost_essence", 16, "Великие эссенции льда", "Алтарь I: 5 эссенций → 2 великие (+3000 опыта).", None),
        (Y + "greater_flame_essence", 16, "Великие эссенции пламени", "", None),
        (Y + "tome_of_winter", 1, "Том зимы", "+1 очко навыков Юки. Падает с Архонта и Тени.", None),
        (Y + "tome_of_flame", 1, "Том пламени", "+1 очко навыков Синтары. Падает с Феникса и Тени.", None),
        (Y + "yuki_regalia_robe", 1, "Регалия Юки", "Алтарь III из мантии криоманта. 3D-броня с ледяными крыльями.", None),
        (Y + "sintara_regalia_robe", 1, "Регалия Синтары", "Алтарь III из мантии пироманта. 3D-броня с крыльями феникса.",
         None),
        (C(Y + "yuki_staff"), 1, "Аватар Юки", "Изучи Аватар Юки (50 уровень): полёт, ледяная аура, −75% маны.", None),
        (C(Y + "sintara_staff"), 1, "Аватар Синтары", "Изучи Аватар Синтары (50 уровень): форма феникса.",
         (Y + "mana_crystal", 4)),
    ]),
]

INSERT_AFTER7 = {"me_network": "digital", "eternal": "cores", "archmage": "mastery"}
GROUP7 = {"me_network": "act1", "eternal": "act4", "archmage": "act4"}
