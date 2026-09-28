from reportlab.lib import colors
from reportlab.lib.enums import TA_CENTER, TA_LEFT
from reportlab.lib.pagesizes import A4
from reportlab.lib.styles import getSampleStyleSheet, ParagraphStyle
from reportlab.lib.units import mm
from reportlab.pdfbase import pdfmetrics
from reportlab.pdfbase.ttfonts import TTFont
from reportlab.platypus import (
    SimpleDocTemplate, Paragraph, Spacer, Table, TableStyle,
    PageBreak, KeepTogether
)
from reportlab.pdfbase.pdfmetrics import stringWidth
from pathlib import Path

OUT = Path(__file__).resolve().parents[1] / "output/pdf/FinnyPet_Documentation_2.2.1.pdf"
OUT.parent.mkdir(parents=True, exist_ok=True)
FONT = r"C:\Windows\Fonts\arial.ttf"
FONT_BOLD = r"C:\Windows\Fonts\arialbd.ttf"
pdfmetrics.registerFont(TTFont("Arial", FONT))
pdfmetrics.registerFont(TTFont("Arial-Bold", FONT_BOLD))

BLUE = colors.HexColor("#263A65")
ACCENT = colors.HexColor("#456DDB")
GOLD = colors.HexColor("#F7B940")
MINT = colors.HexColor("#DFF5EA")
PALE = colors.HexColor("#F4F7FC")
TEXT = colors.HexColor("#3E4E68")

styles = getSampleStyleSheet()
styles.add(ParagraphStyle(name="TitleFinny", parent=styles["Title"], fontName="Arial-Bold", fontSize=28, leading=33, textColor=BLUE, alignment=TA_CENTER, spaceAfter=12))
styles.add(ParagraphStyle(name="SubtitleFinny", parent=styles["Normal"], fontName="Arial", fontSize=14, leading=20, textColor=TEXT, alignment=TA_CENTER))
styles.add(ParagraphStyle(name="H1Finny", parent=styles["Heading1"], fontName="Arial-Bold", fontSize=19, leading=24, textColor=BLUE, spaceBefore=8, spaceAfter=10))
styles.add(ParagraphStyle(name="H2Finny", parent=styles["Heading2"], fontName="Arial-Bold", fontSize=13, leading=17, textColor=BLUE, spaceBefore=7, spaceAfter=5))
styles.add(ParagraphStyle(name="BodyFinny", parent=styles["BodyText"], fontName="Arial", fontSize=10, leading=14, textColor=TEXT, spaceAfter=6))
styles.add(ParagraphStyle(name="SmallFinny", parent=styles["BodyText"], fontName="Arial", fontSize=8.2, leading=10.5, textColor=TEXT))
styles.add(ParagraphStyle(name="TableHead", parent=styles["BodyText"], fontName="Arial-Bold", fontSize=8.2, leading=10, textColor=colors.white))
styles.add(ParagraphStyle(name="TableCell", parent=styles["BodyText"], fontName="Arial", fontSize=7.6, leading=9.5, textColor=TEXT))
styles.add(ParagraphStyle(name="Callout", parent=styles["BodyText"], fontName="Arial-Bold", fontSize=10.5, leading=14, textColor=BLUE, alignment=TA_LEFT))

def p(text, style="BodyFinny"):
    # The sound pass is applied before ReportLab lays out the existing content.
    # Keep the release text aligned with the generated, original WAV assets.
    for old, new in {
        "Интерфейсные и графические ресурсы.": "Интерфейсные, графические и восемь оригинальных звуковых WAV-ресурсов.",
        "В настройках есть отдельный локально сохраняемый переключатель.": "Восемь коротких оригинальных WAV воспроизводятся локально через SoundPool. Переключатель звука сохраняется после перезапуска.",
        "SettingsViewModel; MoreHub": "FinnyAudio; SettingsViewModel; MoreHub",
        "Подтвердить права на все пользовательские графические и звуковые ассеты.": "Подтвердить права на пользовательские графические ассеты; аудио синтезировано в проекте.",
        "Перед публичной публикацией владелец подтверждает права на финальные изображения и звуки.": "Звуковые эффекты синтезированы скриптом tools/generate_sfx.py без сторонних записей; права на финальные изображения подтверждает владелец.",
    }.items():
        text = text.replace(old, new)
    return Paragraph(text, styles[style])

def bullet(text):
    return p("• " + text)

def table(headers, rows, widths):
    data = [[p(h, "TableHead") for h in headers]]
    data += [[p(c, "TableCell") for c in row] for row in rows]
    t = Table(data, colWidths=widths, repeatRows=1, hAlign="LEFT")
    t.setStyle(TableStyle([
        ("BACKGROUND", (0, 0), (-1, 0), ACCENT),
        ("TEXTCOLOR", (0, 0), (-1, 0), colors.white),
        ("GRID", (0, 0), (-1, -1), 0.35, colors.HexColor("#C9D5EB")),
        ("BACKGROUND", (0, 1), (-1, -1), colors.white),
        ("VALIGN", (0, 0), (-1, -1), "TOP"),
        ("LEFTPADDING", (0, 0), (-1, -1), 5),
        ("RIGHTPADDING", (0, 0), (-1, -1), 5),
        ("TOPPADDING", (0, 0), (-1, -1), 5),
        ("BOTTOMPADDING", (0, 0), (-1, -1), 5),
        ("ROWBACKGROUNDS", (0, 1), (-1, -1), [colors.white, PALE]),
    ]))
    return t

def header_footer(canvas, doc):
    canvas.saveState()
    w, h = A4
    canvas.setStrokeColor(colors.HexColor("#D7E1F3"))
    canvas.line(18*mm, h-14*mm, w-18*mm, h-14*mm)
    canvas.setFont("Arial-Bold", 8)
    canvas.setFillColor(ACCENT)
    canvas.drawString(18*mm, h-10*mm, "Питомец Финни - документация прототипа")
    canvas.setFont("Arial", 8)
    canvas.setFillColor(TEXT)
    canvas.drawRightString(w-18*mm, 10*mm, f"Страница {doc.page}")
    canvas.restoreState()

doc = SimpleDocTemplate(str(OUT), pagesize=A4, rightMargin=18*mm, leftMargin=18*mm, topMargin=20*mm, bottomMargin=17*mm)
story = []

# Cover
story += [Spacer(1, 45*mm), p("ПИТОМЕЦ ФИННИ", "TitleFinny"), p("Функциональный офлайн-прототип Android-приложения по финансовой грамотности для детей 7-11 лет", "SubtitleFinny"), Spacer(1, 12*mm)]
cover = Table([[p("Версия", "Callout"), p("2.2.1, versionCode 16", "Callout")], [p("Платформа", "Callout"), p("Android 8.0+; Kotlin; Jetpack Compose", "Callout")], [p("Сборка", "Callout"), p("Локальная Room/SQLite, без сервера и INTERNET permission", "Callout")]], colWidths=[43*mm, 115*mm])
cover.setStyle(TableStyle([("BACKGROUND", (0,0), (-1,-1), colors.white), ("GRID", (0,0), (-1,-1), .6, colors.HexColor("#C9D5EB")), ("ROWBACKGROUNDS", (0,0), (-1,-1), [colors.HexColor("#EDF3FF"), colors.white]), ("VALIGN", (0,0), (-1,-1), "MIDDLE"), ("LEFTPADDING", (0,0), (-1,-1), 9), ("RIGHTPADDING", (0,0), (-1,-1), 9), ("TOPPADDING", (0,0), (-1,-1), 9), ("BOTTOMPADDING", (0,0), (-1,-1), 9)]))
story += [cover, Spacer(1, 16*mm), p("Исходники и материалы: github.com/vorich8/finny-pet. Документ предназначен для сборки, проверки и демонстрации.", "SubtitleFinny"), PageBreak()]

# Readme / run
story += [p("1. Назначение и быстрый запуск", "H1Finny"), p("«Питомец Финни» - детская игра для развития базовых финансовых навыков. Ребёнок создаёт виртуального питомца, получает только игровую валюту, планирует расходы, выбирает обязательное и желаемое, копит на цель и видит последствия решений."), p("В проекте нет реальных платежей, рекламы, регистрации, чатов, социальных функций или сбора персональных данных. Основной цикл работает офлайн."), p("Состав репозитория", "H2Finny"), table(["Папка/файл", "Назначение"], [["app/src/main/java", "Kotlin-код экранов, навигации, игровых механик, ViewModel, домена и локального хранилища."], ["app/src/main/assets", "Учебный контент: варианты питомцев, цели, товары, уроки и конфигурации игр."], ["app/src/main/res", "Интерфейсные и графические ресурсы."], ["app/schemas", "Зафиксированная схема Room базы данных."], ["docs", "Матрица ТЗ, карта контента, тест-план, архитектура, RuStore и чек-лист релиза."]], [45*mm, 113*mm]), Spacer(1, 8*mm), p("Быстрый запуск APK", "H2Finny"), bullet("Передать файл FinnyPet.apk на Android-устройство с Android 8.0 или новее."), bullet("Открыть APK в файловом менеджере и разрешить установку из выбранного источника, если система спросит."), bullet("Запустить «Питомец Финни». При первом запуске выбрать питомца и пройти короткое знакомство."), p("Сборка из исходников", "H2Finny"), bullet("Открыть C:\\FinnyPet в Android Studio, использовать JDK 17 и Android SDK 35."), bullet("Выполнить ./gradlew.bat assembleRelease. Результат: app/build/outputs/apk/release/app-release.apk."), PageBreak()]

# Architecture
story += [p("2. Функциональная и компонентная архитектура", "H1Finny"), p("Приложение однопользовательское и не имеет серверной части. Все состояния сохраняются локально. Архитектура построена по MVVM: экран Compose наблюдает StateFlow ViewModel; ViewModel обращается к use case; use case выполняет правила экономики и работает с репозиториями; репозитории используют Room/SQLite."), table(["Слой", "Ответственность", "Основные элементы"], [["UI", "Экраны, анимации, доступные действия и обратная связь.", "Screens, CityScreens, GameScreens, ExtendedGameScreen"], ["Навигация", "Переходы между онбордингом, домом, картой, зданиями, играми и итогами.", "FinnyNavHost, Routes"], ["Presentation", "Состояния, проверка действий, сохранение настроек.", "AppViewModels, GameViewModels"], ["Domain", "Формулы баланса, наград, роста, покупки и сброса.", "Calculators, UseCases"], ["Data", "Локальная база и репозитории.", "Room AppDatabase, DAO, Repositories"], ["Content", "Независимый от интерфейса учебный контент.", "assets/*.json, EducationalGameCatalog"]], [28*mm, 67*mm, 63*mm]), Spacer(1, 7*mm), p("Обязательный путь ребёнка", "H2Finny"), p("Онбординг -> выбор питомца и игрового имени -> три простых правила бюджета -> главный экран -> распределение средств и задания -> покупка с проверкой нехватки -> вклад в цель -> итог дня -> следующий период и рост питомца -> сохранённый прогресс при следующем запуске. В разделе для взрослого доступен сброс с подтверждением."), p("Локальное хранение", "H2Finny"), p("Room/SQLite сохраняет профиль, питомца, баланс, цели, покупки, задания, потребности, предметы, скины, уровни и журнал игровых операций. Сброс выполняется одной транзакцией и возвращает пользователя к выбору питомца."), PageBreak()]

# data formula
story += [p("3. Данные и правила игровой экономики", "H1Finny"), table(["Сущность", "Что хранит"], [["ProfileEntity", "Игровое имя питомца, текущий из пяти периодов."], ["PetEntity", "Выбранный визуальный вариант, стадия роста, эмоция."], ["BalanceEntity / WalletTransactionEntity", "Монеты, звёзды и история игровых начислений/трат."], ["GoalEntity", "Цель, стоимость, накопленная сумма, активность и завершение."], ["TaskProgressV2Entity / ProgressEntity", "Попытки, лучший результат, открытые уровни и настройки."], ["PetNeedsEntity / InventoryEntity", "Еда, вода, здоровье, предметы рюкзака."], ["PeriodEntity / SkinOwnershipEntity", "Прогресс сюжета и внешний вид питомца."]], [55*mm, 103*mm]), Spacer(1, 7*mm), p("Формулы", "H2Finny"), p("Баланс: <b>остаток = max(0, доходы - обязательные расходы - необязательные расходы - накопления)</b>. Решение принимается только если расходы и накопления не превышают доходы."), p("Награды за первое успешное прохождение уровней 1-10: <b>5, 8, 10, 12, 15, 18, 20, 23, 26, 30</b> монет. Повтор уровня не выдаёт монеты повторно, но сохраняет лучший результат в звёздах."), p("Рост питомца: день 1 - малыш; дни 2-4 - подросший; день 5 - взрослый. Цели накопления: игрушка 50, домик 100, игровой комплекс 150 монет; также есть недельная цель."), p("Безопасная ошибка", "H2Finny"), p("При нехватке средств баланс не меняется. Ребёнок видит цену, свой остаток, размер недостачи и безопасный путь: выполнить задание или выбрать менее дорогой вариант. Питомец не умирает; лечение - восстанавливаемый учебный случай непредвиденной траты."), PageBreak()]

# content
story += [p("4. Карта образовательного контента", "H1Finny"), table(["Тема", "Игры", "Навык и правильная логика"], [["Планирование бюджета", "Весы; Приоритеты; Список покупок; Неделя друга", "Различать доходы и расходы, выбирать обязательное, распределять недельную сумму."], ["Сбережения", "Копилка; Цель; Секретный ящик", "Оставлять резерв и откладывать только свободную сумму после важного."], ["Платежи и покупки", "Магазин; Сдача; Гонка расходов; Светофор трат; Сравни цены", "Следовать списку, соблюдать лимит, считать сдачу, сопоставлять предложения."]], [35*mm, 58*mm, 65*mm]), Spacer(1, 7*mm), p("Контентный минимум", "H2Finny"), bullet("12 учебных игр, каждая имеет 10 уровней и возможность повторного прохождения."), bullet("Пять последовательных сюжетных периодов."), bullet("12 визуально различимых комбинаций питомца и три стадии роста."), bullet("Не менее восьми товаров: обязательные и необязательные."), bullet("Три цели накопления, все в игровой валюте."), p("Объяснение для ребёнка", "H2Finny"), p("Короткие правила используют понятную последовательность: сначала еда, вода, здоровье и жильё; затем желания; затем накопление. При неправильном ответе игра показывает не красную угрозу, а причину и подсказку: например, «Не хватает 10 монет. Перенеси необязательную покупку в “Можно потом”»."), PageBreak()]

# UX privacy
story += [p("5. UX/UI, доступность и безопасность", "H1Finny"), p("Интерфейс рассчитан на детей 7-11 лет: крупные кнопки, короткие фразы, контрастные непрозрачные карточки, подписи к иконкам и визуально выделенные следующие шаги. Смысл не передаётся только цветом: статусы продублированы текстом, символами и прогресс-барами."), table(["Решение", "Реализация"], [["Крупные элементы", "Основные кнопки и нижнее меню имеют высоту не менее 48 dp."], ["Читаемость", "Основной текст 16 sp и выше; меньший текст используется только для вторичных подписей."], ["Анимация", "Питомец, карта и игровые сцены имеют анимации; переключатель позволяет отключить их."], ["Звук", "В настройках есть отдельный локально сохраняемый переключатель."], ["Ошибки", "Диалог или карточка помощи с объяснением; можно повторить действие."], ["Важные действия", "Сброс профиля требует подтверждения."], ["Навигация", "Назад возвращает в предыдущий логичный экран; здания открываются через короткий переход."], ["Безопасность", "Нет рекламы, покупок за реальные деньги, регистрации, чатов, рейтингов или передачи данных."]], [42*mm, 116*mm]), Spacer(1, 7*mm), p("Разрешения, данные и удаление профиля", "H2Finny"), p("Manifest не содержит INTERNET permission и не запрашивает доступ к контактам, геолокации, камере, микрофону или хранилищу. Вводится только локальное игровое имя питомца. Все данные остаются в Room/SQLite на устройстве. В разделе «Для взрослых» задача 6+3 открывает кнопку «Сбросить профиль»; после подтверждения транзакция очищает локальные данные и возвращает к выбору питомца."), PageBreak()]

# matrix
story += [p("6. Матрица соответствия обязательным требованиям", "H1Finny"), table(["Требование", "Статус", "Экран/модуль"], [["Android 8.0+, portrait", "Реализовано", "build.gradle.kts; AndroidManifest.xml"], ["Локальный профиль, офлайн", "Реализовано", "Room AppDatabase; Manifest без INTERNET"], ["Игровая валюта, без реальных платежей", "Реализовано", "BalanceEntity; отсутствуют billing SDK"], ["5 периодов, 3 стадии роста", "Реализовано", "AdvanceStoryDay; PetEntity"], ["9+ питомцев, 6+ заданий, 3 темы", "Реализовано", "pet_variants.json; EducationalGameCatalog"], ["8+ покупок, 3+ цели", "Реализовано", "shop.json; goals.json"], ["Нехватка средств и обратная связь", "Реализовано", "CityPurchase; игровые ViewModel"], ["Сохранение, повтор без монет", "Реализовано", "AwardGameResult; Room"], ["Взрослый раздел и подтверждённый сброс", "Реализовано", "AdultScreen; ResetProfile"], ["Доступность: звук/анимация", "Реализовано", "SettingsViewModel; MoreHub"], ["Release APK", "Реализовано для демо", "FinnyPet.apk, debug signing"], ["Физическое устройство 3 ГБ+", "В работе", "Нужна реальная проверка"], ["Production key, RuStore icon, права ассетов", "В работе", "Нужны материалы владельца"], ["Видео и презентация", "В работе", "Нужны финальные демо-материалы"]], [50*mm, 30*mm, 78*mm]), PageBreak()]

# QA limitations licences
story += [p("7. Проверка, ограничения и дальнейшее развитие", "H1Finny"), p("Автотесты", "H2Finny"), p("В проекте есть 7 unit-тестов для шкалы наград, экономики пяти дней, баланса, сдачи, обязательных бюджетов и оценки «Копилки». Release APK и release lint успешно собираются. В текущей Windows-среде запуск Gradle Test Executor блокируется системной ошибкой загрузки GradleWorkerMain; успешный прогон на этой машине не заявляется."), p("Smoke-сценарии", "H2Finny"), table(["Сценарий", "Ожидаемый результат"], [["Первый запуск", "Онбординг, выбор питомца, три правила, главный экран."], ["Покупка при нехватке", "Сумма не списывается; объясняются дефицит и следующий шаг."], ["Повтор задания", "Лучший результат сохраняется, монеты повторно не начисляются."], ["Карта и здания", "Открытое здание запускает переход; закрытое сообщает условие."], ["Игры", "Весы реагируют на перенос, копилка ловит монеты, гонка расходов движет товары."], ["Сброс", "Есть подтверждение и возврат к начальному выбору питомца."]], [58*mm, 100*mm]), Spacer(1, 7*mm), p("Известные ограничения и план", "H2Finny"), bullet("Перед конкурсной финальной сдачей пройти полный сценарий на физическом Android 8+ с RAM от 3 ГБ."), bullet("Собрать production APK/AAB ключом владельца, выбрать окончательный уникальный applicationId."), bullet("Утвердить три подготовленных QA-скриншота, подготовить иконку RuStore 512x512, презентацию и видео до трёх минут."), bullet("Подтвердить права на все пользовательские графические и звуковые ассеты."), p("Лицензии", "H2Finny"), p("Kotlin, AndroidX, Jetpack Compose, Room, Navigation, DataStore, Dagger/Hilt и Material Icons используются по Apache License 2.0. В APK включены пять изображений предметов, переданных заказчиком; права на распространение перед публикацией должны быть подтверждены. Перед публичной публикацией владелец подтверждает права на финальные изображения и звуки."), Spacer(1, 8*mm), p("Конец документа", "SubtitleFinny")]

doc.build(story, onFirstPage=header_footer, onLaterPages=header_footer)
print(OUT)
