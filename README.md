# Thaumic Reforged

A port of **Thaumcraft 4** to **Minecraft 1.21.1 / NeoForge**, with the **Thaumic Energistics** addon.

| Module | Mod id | What it is |
|---|---|---|
| `core/` | `thaumcraft` | Thaumcraft 4: aspects, research, infusion, alchemy, golems, warp, the Eldritch dimension |
| `addons/thaumicenergistics/` | `thaumicenergistics` | Bridge between Thaumcraft and Applied Energistics 2 |

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
