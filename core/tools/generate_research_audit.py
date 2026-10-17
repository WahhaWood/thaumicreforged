#!/usr/bin/env python3
"""
Generate comprehensive audit documentation for all 269 Thaumcraft researches
across 7 categories based on JSON data, recipes, code, and localization.
"""

import os
import glob
import json
import re

REPO_ROOT = os.path.abspath(os.path.join(os.path.dirname(__file__), '..', '..'))
RESEARCH_DIR = os.path.join(REPO_ROOT, 'core/src/main/resources/data/thaumcraft/research')
RECIPES_DIR = os.path.join(REPO_ROOT, 'core/src/main/resources/data/thaumcraft/recipe')
LANG_RU = os.path.join(REPO_ROOT, 'core/src/main/resources/assets/thaumcraft/lang/ru_ru.json')
LANG_EN = os.path.join(REPO_ROOT, 'core/src/main/resources/assets/thaumcraft/lang/en_us.json')
OUTPUT_DIR = os.path.join(REPO_ROOT, 'notes/research')
INDEX_FILE = os.path.join(REPO_ROOT, 'notes/12-thaumonomicon-research-audit.md')

os.makedirs(OUTPUT_DIR, exist_ok=True)

# 1. Load localization
with open(LANG_RU, 'r', encoding='utf-8-sig') as f:
    ru = json.load(f)
with open(LANG_EN, 'r', encoding='utf-8-sig') as f:
    en = json.load(f)

# 2. Load registered blocks and items
with open(os.path.join(REPO_ROOT, 'core/src/main/java/thaumcraft/init/registry/ModBlocks.java')) as f:
    registered_blocks = set(re.findall(r'register\("([^"]+)"', f.read()))

with open(os.path.join(REPO_ROOT, 'core/src/main/java/thaumcraft/init/registry/ModItems.java')) as f:
    registered_items = set(re.findall(r'register\("([^"]+)"', f.read()))

# 3. Scan existing recipes
all_recipes = set()
for rfile in glob.glob(os.path.join(RECIPES_DIR, '**/*.json'), recursive=True):
    rel = os.path.relpath(rfile, RECIPES_DIR)
    rtype = os.path.dirname(rel)
    rname = os.path.splitext(os.path.basename(rel))[0]
    all_recipes.add(f'thaumcraft:{rtype}/{rname}')
    all_recipes.add(f'{rtype}/{rname}')
    all_recipes.add(rname)

def clean_text(text):
    if not text:
        return ''
    t = re.sub(r'§[0-9a-fk-or]', '', text)
    t = t.replace('<BR>', '\n').replace('<PAGE>', '\n\n').replace('<LINE>', '\n')
    # strip multiple consecutive newlines
    t = re.sub(r'\n{3,}', '\n\n', t)
    return t.strip()

CATEGORY_MAP = [
    ('basics', '01-basics.md', 'Базовые сведения (Basics)', '19 исследований: базовый лор, аспекты, исследование, искажение, руды, деревья'),
    ('thaumaturgy', '02-thaumaturgy.md', 'Тауматургия (Thaumaturgy)', '40 исследований: жезлы, наконечники, стержни, набалдашники, пьедестал подзарядки'),
    ('alchemy', '03-alchemy.md', 'Алхимия (Alchemy)', '29 исследований: тигель, перегонка, банки, бирки, трубы, центрифуга, тауматориум'),
    ('artifice', '04-artifice.md', 'Изобретения (Artifice)', '57 исследований: мистический верстак, очки откровения, руническая матрица, зеркала, ботинки'),
    ('golemancy', '05-golemancy.md', 'Големостроение (Golemancy)', '39 исследований: соломенные/глиняные/плотяные/таум-големы, ядра големов, аксессуары'),
    ('eldritch', '06-eldritch.md', 'Древние знания (Eldritch)', '19 исследований: искажение, сингулярность, пустотная броня, око древних, лабиринт'),
    ('tinkerer', '07-tinkerer.md', 'Таумик тинкер (Thaumic Tinkerer)', '66 исследований: аддон к TC4, ихор, ками, чародейская стойка, трансмутации')
]

stats = {
    'total': 0,
    'categories': {},
    'by_category_counts': {}
}

category_summaries = {}

for cat_id, filename, cat_title, cat_desc in CATEGORY_MAP:
    cat_path = os.path.join(RESEARCH_DIR, cat_id)
    json_files = sorted(glob.glob(os.path.join(cat_path, '*.json')))
    stats['by_category_counts'][cat_id] = len(json_files)
    stats['total'] += len(json_files)

    out_filepath = os.path.join(OUTPUT_DIR, filename)
    entries = []

    for fpath in json_files:
        rid = os.path.splitext(os.path.basename(fpath))[0]
        with open(fpath, 'r', encoding='utf-8') as fp:
            data = json.load(fp)

        name_ru = ru.get(f'tc.research.{rid}.name', en.get(f'tc.research.{rid}.name', rid))
        name_en = en.get(f'tc.research.{rid}.name', rid)
        desc_ru = ru.get(f'tc.research.{rid}.text', en.get(f'tc.research.{rid}.text', ''))
        desc_en = en.get(f'tc.research.{rid}.text', '')

        # Metadata
        col = data.get('column', 0)
        row = data.get('row', 0)
        auto_unlock = data.get('autoUnlock', False)
        round_res = data.get('round', False)
        stub = data.get('stub', False)
        warp = data.get('warp', 0)
        complexity = data.get('complexity', 0)
        parents = data.get('parents', [])
        parents_hidden = data.get('parents_hidden', [])
        siblings = data.get('siblings', [])
        icon = data.get('icon', {})

        # Icon item
        if isinstance(icon, dict):
            icon_item = icon.get('item', icon.get('id', ''))
        elif isinstance(icon, str):
            icon_item = icon
        else:
            icon_item = ''
        icon_status = 'N/A'
        if icon_item:
            clean_item = icon_item.replace('thaumcraft:', '').replace('minecraft:', '')
            if 'thaumcraft:' in icon_item:
                if clean_item in registered_items:
                    icon_status = f'Item OK (`{clean_item}`)'
                elif clean_item in registered_blocks:
                    icon_status = f'Block OK (`{clean_item}`)'
                else:
                    icon_status = f'MISSING (`{clean_item}`)'
            else:
                icon_status = f'Minecraft (`{clean_item}`)'

        # Pages analysis
        text_pages = []
        recipes = []
        structures = []

        for p in data.get('pages', []):
            ptype = p.get('type')
            if ptype == 'text':
                k = p.get('key')
                txt_ru = clean_text(ru.get(k, ''))
                txt_en = clean_text(en.get(k, ''))
                text_pages.append((k, txt_ru, txt_en))
            elif ptype in ('arcane', 'crucible', 'infusion', 'crafting', 'recipes', 'enchant', 'fortress', 'infusion_runic_augmentation'):
                rec_id = p.get('id', p.get('recipe', ''))
                # check if exists
                clean_rec = str(rec_id).replace('thaumcraft:', '')
                found = (rec_id in all_recipes or clean_rec in all_recipes)
                recipes.append((ptype, rec_id, found))
            elif ptype == 'formed_structure':
                structures.append(p)

        entries.append({
            'id': rid,
            'name_ru': name_ru,
            'name_en': name_en,
            'desc_ru': desc_ru,
            'desc_en': desc_en,
            'col': col,
            'row': row,
            'auto_unlock': auto_unlock,
            'round': round_res,
            'stub': stub,
            'warp': warp,
            'complexity': complexity,
            'parents': parents,
            'parents_hidden': parents_hidden,
            'siblings': siblings,
            'icon': icon_item,
            'icon_status': icon_status,
            'text_pages': text_pages,
            'recipes': recipes,
            'structures': structures
        })

    # Render category markdown
    with open(out_filepath, 'w', encoding='utf-8') as out:
        out.write(f'# {cat_title}\n\n')
        out.write(f'> {cat_desc}\n')
        out.write(f'> Всего исследований в категории: **{len(entries)}**\n\n')

        out.write('## Оглавление исследований\n\n')
        out.write('| ID | Название (RU) | Name (EN) | Требования | Варп | Иконка |\n')
        out.write('|---|---|---|---|---|---|\n')
        for e in entries:
            p_str = ', '.join(f'`{p}`' for p in e['parents']) if e['parents'] else '*(нет)*'
            warp_str = f"⚡ {e['warp']}" if e['warp'] else '-'
            out.write(f"| [`{e['id']}`](#{e['id']}) | {e['name_ru']} | {e['name_en']} | {p_str} | {warp_str} | {e['icon_status']} |\n")

        out.write('\n---\n\n## Подробный аудит каждого исследования\n\n')

        for e in entries:
            out.write(f"### <a id=\"{e['id']}\"></a> `{e['id']}`: {e['name_ru']} / {e['name_en']}\n\n")
            out.write(f"**Описание:** *{e['desc_ru']}* (`{e['desc_en']}`)\n\n")
            out.write(f"- **Координаты в Таумономиконе:** Колонка `{e['col']}`, Строка `{e['row']}`\n")
            out.write(f"- **Свойства:** autoUnlock={e['auto_unlock']}, round={e['round']}, stub={e['stub']}, warp={e['warp']}, complexity={e['complexity']}\n")
            if e['parents']:
                out.write(f"- **Родительские исследования:** {', '.join(f'`{p}`' for p in e['parents'])}\n")
            if e['parents_hidden']:
                out.write(f"- **Скрытые родительские исследования:** {', '.join(f'`{p}`' for p in e['parents_hidden'])}\n")
            out.write(f"- **Иконка:** `{e['icon']}` ({e['icon_status']})\n\n")

            if e['recipes']:
                out.write('#### Открываемые рецепты:\n')
                for rtype, rid, found in e['recipes']:
                    status_badge = '✅ найден' if found else '⚠️ не найден'
                    out.write(f"- `{rtype}`: `{rid}` — {status_badge}\n")
                out.write('\n')

            if e['structures']:
                out.write('#### Формируемые структуры:\n')
                for s in e['structures']:
                    out.write(f"```json\n{json.dumps(s, indent=2, ensure_ascii=False)}\n```\n")
                out.write('\n')

            out.write('#### Текст страниц и игровая механика (Таумономикон TC4):\n\n')
            if not e['text_pages']:
                out.write('*(Текстовые страницы отсутствуют)*\n\n')
            else:
                for idx, (k, t_ru, t_en) in enumerate(e['text_pages'], 1):
                    out.write(f"**Страница {idx}** (`{k}`):\n")
                    if t_ru:
                        out.write(f"> {t_ru.replace(chr(10), chr(10) + '> ')}\n\n")
                    elif t_en:
                        out.write(f"> *[EN]* {t_en.replace(chr(10), chr(10) + '> ')}\n\n")

            out.write('#### Чек-лист реализации механики:\n')
            # Custom notes for key items
            if e['id'] == 'crucible':
                out.write('- [x] Регистрация блока `thaumcraft:crucible`\n')
                out.write('- [x] Модель и рендерер кипящей воды (`CrucibleRenderer`)\n')
                out.write('- [x] Бульканье и пар при наличии источника тепла снизу\n')
                out.write('- [x] Аспекты внутри тигля растворяются из выброшенных предметов\n')
                out.write('- [x] Рецепты алхимии по клику катализатором / жезлом\n')
                out.write('- [x] Переполнение аспектов и деградация в порчу (Flux / Warp)\n')
            elif e['id'] == 'distilessence':
                out.write('- [x] Регистрация блока `thaumcraft:arcane_alembic` и `thaumcraft:alchemy_furnace`\n')
                out.write('- [x] Структура: до 5 перегонных кубов сверху над алхимической печью\n')
                out.write('- [x] Алхимическая печь сжигает предметы в эссенцию (алюментум ускоряет)\n')
                out.write('- [x] Заполнение куба аспектом (макс. 32 или 64)\n')
                out.write('- [ ] **Текущая проблема (Playtest):** Внутри куба не отображается уровень и цвет жидкости эссенции\n')
                out.write('- [ ] **Текущая проблема (Playtest):** Пустой блок кода в `ArcaneAlembicBlock.useItemOn` при клике банкой / пузырьком\n')
            elif e['id'] == 'jar_label':
                out.write('- [x] Регистрация блоков `thaumcraft:jar`, `thaumcraft:jar_void`\n')
                out.write('- [x] Регистрация предмета `thaumcraft:jar_label`\n')
                out.write('- [x] Хранение до 64 единиц одного аспекта\n')
                out.write('- [ ] **Текущая проблема (Playtest):** `JarBlock.useItemOn` пустой (бирка не ставится на банку)\n')
                out.write('- [ ] **Текущая проблема (Playtest):** Шифт-ПКМ пустой рукой по бирке не снимает бирку\n')
                out.write('- [ ] **Текущая проблема (Playtest):** Пузырёк эссенции не заливается в банку и не наполняется из неё\n')
                out.write('- [ ] Крафт бирка + пузырек эссенции на верстаке для настройки бирки без траты эссенции\n')
                out.write('- [ ] Очистка бирки в сетке верстака\n')
            elif e['id'] == 'phial_essence':
                out.write('- [x] Регистрация предмета `thaumcraft:phial_essence`\n')
                out.write('- [x] Крафт пустых пузырьков (глина + стекло)\n')
                out.write('- [ ] Взаимодействие с тиглем (забор 8 эссенции)\n')
                out.write('- [ ] Взаимодействие с перегонным кубом (забор 8 эссенции)\n')
                out.write('- [ ] Взаимодействие с банкой (забор 8 эссенции / залив 8 эссенции в банку)\n')
            elif e['id'] == 'infusion':
                out.write('- [x] Руническая матрица (Runic Matrix) и мультиблок\n')
                out.write('- [x] Пьедесталы и центральный пьедестал\n')
                out.write('- [x] Расчет нестабильности по симметрии (свечи, черепа, кристаллы)\n')
                out.write('- [x] Спектр эффектов нестабильности TC4 (от падения предметов до молний и искажения)\n')
                out.write('- [x] Потребление эссенции из банок в радиусе 12 блоков\n')
            elif e['id'] == 'thaumatorium':
                out.write('- [x] Мультиблок алхимического конструктора\n')
                out.write('- [ ] Автоматическая алхимия без тигля и без потерь\n')
            else:
                out.write(f"- [ ] Анализ и верификация логики `{e['id']}` в соответствии со страницами Таумономикона\n")

            out.write('\n---\n\n')

# 4. Generate Master Index document
with open(INDEX_FILE, 'w', encoding='utf-8') as out:
    out.write('# Генеральный реестр и аудит исследований Таумономикона TC4\n\n')
    out.write('Данный документ представляет собой пошаговую инвентаризацию всех 269 исследований из оригинального Thaumcraft 4.2.3.5 (+ Thaumic Tinkerer), их текстов, механик, структур, открываемых рецептов и соответствия кодовой базе 1.21.1.\n\n')
    out.write('## Сводная статистика по категориям\n\n')
    out.write('| Категория | Файл аудита | Количество исследований | Статус |\n')
    out.write('|---|---|---|---|\n')
    for cat_id, filename, cat_title, cat_desc in CATEGORY_MAP:
        count = stats['by_category_counts'][cat_id]
        out.write(f'| **{cat_title}** | [{filename}](research/{filename}) | **{count}** | Сгенерирован подробный разбор |\n')
    out.write(f'| **ИТОГО** | - | **{stats["total"]} исследований** | 100% покрытие реестра |\n\n')

    out.write('## Приоритетный план работы «Сверху вниз» (Top-to-Bottom)\n\n')
    out.write('На основе прямых отзывов из live playtest и описаний Таумономикона, сформирован порядок первоочередных доработок:\n\n')
    out.write('### Этап 1. Немедленные исправления Алхимии (Live Playtest Feedback)\n')
    out.write('1. **Банки и Бирки (`jar_label`)**:\n')
    out.write('   - Восстановить логику `JarBlock.useItemOn`:\n')
    out.write('     - ПКМ биркой с аспектом по банке -> установка фильтра `aspectFilter` (если пустая или совпадает аспект).\n')
    out.write('     - ПКМ чистой биркой по банке с эссенцией -> настройка бирки и установка фильтра на банку.\n')
    out.write('     - Shift-ПКМ пустой рукой по стороне с биркой -> снятие бирки и выпадение `ItemJarLabel` с аспектом фильтра.\n')
    out.write('     - Shift-ПКМ пустой рукой по другой стороне банки -> опустошение содержимого банки (сброс в 0, звук выплескивания).\n')
    out.write('   - Крафт бирки с пузырьком эссенции на верстаке (настройка бирки без траты пузырька).\n')
    out.write('   - Крафт бирки в сетке (очистка фильтра с бирки).\n\n')
    out.write('2. **Пузырьки с эссенцией (`phial_essence`)**:\n')
    out.write('   - Заливание эссенции из пузырька в банку: ПКМ полным пузырьком по банке добавляет 8 эссенции (если емкость позволяет и фильтр совпадает), возвращает пустой пузырек.\n')
    out.write('   - Забор эссенции из банки: ПКМ пустым пузырьком по банке забирает 8 эссенции (если в банке >= 8), возвращает заполненный пузырек.\n')
    out.write('   - Забор эссенции из перегонного куба: ПКМ пустым пузырьком по перегонному кубу забирает 8 эссенции (если >= 8).\n\n')
    out.write('3. **Перегонный куб (`distilessence`)**:\n')
    out.write('   - Заполнение банки из куба: ПКМ пустой/подходящей банкой по кубу сливает всю эссенцию (до 64).\n')
    out.write('   - Визуализация уровня эссенции в `ArcaneAlembicBlockEntityRenderer`: отрисовка внутреннего объема эссенции цветом аспекта с помощью `JarRenderUtil.renderFluidBox`.\n')
    out.write('   - Наглядное отображение названия и количества аспекта (включая всплывающий HUD при наведении или четкий лейбл).\n\n')
    out.write('### Этап 2. Систематический проход по категориям\n')
    out.write('1. **Алхимия (`03-alchemy.md`, 29 исследований)**: завершение тигля, труб, фильтров, центрифуги, тауматориума, очистки порчи.\n')
    out.write('2. **Базовые знания (`01-basics.md`, 19 исследований)**: таумометр, исследование узлов ауры, сканирование объектов, стол исследований, механика искажения.\n')
    out.write('3. **Тауматургия (`02-thaumaturgy.md`, 40 исследований)**: жезлы, скипетры, посохи, набалдашники (огонь, мороз, копание, укрепление, сингулярность), пьедестал подзарядки.\n')
    out.write('4. **Изобретения (`04-artifice.md`, 57 исследований)**: мистический верстак, зеркала перемещения/эссенции, ботинки путешественника, левитатор, адская печь, очки откровения.\n')
    out.write('5. **Големостроение (`05-golemancy.md`, 39 исследований)**: все типы големов (солома, глина, плоть, таумий), ядра (жатва, сбор, охрана, сортировка), улучшения.\n')
    out.write('6. **Древние знания (`06-eldritch.md`, 19 исследований)**: глаза древних, врата лабиринта, древние стражи, сингулярность, пустотное снаряжение, защита от порчи.\n')
    out.write('7. **Thaumic Tinkerer (`07-tinkerer.md`, 66 исследований)**: ихор, ками, чародейская стойка, трансмутации, мобильное зеркало.\n\n')

print(f'Successfully generated audit files in {OUTPUT_DIR} and {INDEX_FILE}')
