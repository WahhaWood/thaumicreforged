<div align="center">

# Thaumic Reforged

**A port of Thaumcraft 4 to Minecraft 1.21.1 / NeoForge, with the Thaumic Energistics addon.**

![Minecraft](https://img.shields.io/badge/Minecraft-1.21.1-62B47A)
![NeoForge](https://img.shields.io/badge/NeoForge-21.1-E68C2D)
![Java](https://img.shields.io/badge/Java-21-007396?logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/license-GPL--3.0-blue)

</div>

The classic arcane magic of Thaumcraft 4, rebuilt for modern Minecraft: study the world through aspects,
research new knowledge, brew in crucibles, infuse items on runic matrices, build golems and pay the price
in warp.

## Modules

| Module | Mod id | What it is |
| --- | --- | --- |
| `core/` | `thaumcraft` | Thaumcraft 4: aspects, research, infusion, alchemy, golems, warp, the Eldritch dimension |
| `addons/thaumicenergistics/` | `thaumicenergistics` | Bridge between Thaumcraft and Applied Energistics 2 |

## Highlights

- **Aspects and research.** Scan things, combine aspects in the research table and unlock the tech tree
  through gated discoveries.
- **Alchemy and infusion.** Crucible, alchemical furnace, thaumatorium, arcane alembic, runic matrix with
  infusion pillars.
- **Wands and nodes.** Wand recharge pedestals, vis relays, node stabilizers, transducers and the flux scrubber.
- **Arcane workshop.** Arcane bore, lamps of growth and fertility, bellows, pedestals and the warp gate.
- **Golems and constructs.** Alchemical constructs and their advanced versions.
- **Warp and the Eldritch dimension.** Danger, reward and a Crimson Cult to face.
- **Greatwood and Silverwood** trees with a full wood set.
- **Thaumic Energistics.** Store and move essentia through ME networks.

## Building

Java 21 is required.

```sh
cd core && ./gradlew build              # core/build/libs/thaumcraft-<version>.jar
```

The addon compiles against the core jar:

```sh
cp core/build/libs/thaumcraft-<version>.jar addons/thaumicenergistics/libs/thaumcraft.jar
cd addons/thaumicenergistics && ./gradlew build
```

## Credits

- **Thaumcraft** was created by **Azanor**.
- **Thaumic Energistics** was created by **Nividica**.

This is an unofficial port, not affiliated with or endorsed by the original authors.
Thaumcraft and Thaumic Energistics are names of their respective authors.

## License

The code in this repository is licensed under the [GNU General Public License v3.0](LICENSE).
