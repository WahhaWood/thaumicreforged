<div align="center">

# Thaumic Reforged

**Порт Thaumcraft 4 на Minecraft 1.21.1 / NeoForge вместе с дополнением Thaumic Energistics.**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1-E68C2D)
![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-GPL--3.0-blue)

</div>

Классическая тайная магия Thaumcraft 4 на современном Minecraft: изучайте мир через аспекты,
исследуйте новые знания, варите в тиглях, заряжайте предметы на рунной матрице, стройте големов
и платите за это порчей (warp).

## Модули

| Модуль | Id мода | Что это |
| --- | --- | --- |
| `core/` | `thaumcraft` | Thaumcraft 4: аспекты, исследования, инфузия, алхимия, големы, warp, измерение Эльдрич |
| `addons/thaumicenergistics/` | `thaumicenergistics` | Мост между Thaumcraft и Applied Energistics 2 |

## Главное

- **Аспекты и исследования.** Сканируйте предметы, сочетайте аспекты в столе исследований и открывайте дерево
  технологий через закрытые открытия.
- **Алхимия и инфузия.** Тигель, алхимическая печь, таумиторий, тайный перегонный куб, рунная матрица со столбами инфузии.
- **Жезлы и узлы.** Постаменты подзарядки жезла, реле виса, стабилизаторы и трансдьюсеры узлов, очиститель флюкса.
- **Тайная мастерская.** Тайный бур, лампы роста и плодородия, мехи, постаменты и врата варпа.
- **Големы и конструкты.** Алхимические конструкты и их улучшенные версии.
- **Warp и измерение Эльдрич.** Опасность, награда и Багровый культ.
- **Великое дерево и Серебряное дерево** с полным набором древесины.
- **Thaumic Energistics.** Хранение и передача эссенции через ME-сети.

## Интеграции

Все необязательные. Предметы других модов получают аспекты: металлы по общим тегам (`c:ingots/nickel`,
`c:ores/uranium` и так далее), предметы IndustrialReforged, Botania и Applied Energistics 2 по id. Тигель делает
кластеры олова и свинца с IndustrialReforged или Mekanism, кластеры серебра с IndustrialReforged.

## Сборка

Нужна Java 21.

```sh
cd core && ./gradlew build              # core/build/libs/thaumcraft-<версия>.jar
```

Дополнение собирается против jar ядра:

```sh
cp core/build/libs/thaumcraft-<версия>.jar addons/thaumicenergistics/libs/thaumcraft.jar
cd addons/thaumicenergistics && ./gradlew build
```

## Благодарности

- **Thaumcraft** создал **Azanor**.
- **Thaumic Energistics** создал **Nividica**.

Это неофициальный порт, он не связан с оригинальными авторами и не одобрен ими.
Thaumcraft и Thaumic Energistics — названия их авторов.

## Лицензия

Код репозитория распространяется по лицензии [GNU General Public License v3.0](LICENSE).
