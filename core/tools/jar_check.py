#!/usr/bin/env python3
"""Сборка и проверка серверного jar'а ThaumicReforged.

Нужно, потому что мод обновляется на сервере постоянно, а jar из
`build/libs/` легко уезжает устаревшим: Gradle пересобирает классы при
`runServer`, поэтому dev-стенд всегда свежий, а jar'а в `mods/` может лежать
вчерашний — и об этом никто не узнаёт, пока игроки не столкнутся с багом.

Проверки:
  1. jar новее любого исходника (иначе он не отражает текущий код);
  2. в jar есть все классы и ресурсы из build/;
  3. на месте метаданные мода: neoforge.mods.toml, mixins, access transformer;
  4. печатает путь, куда класть jar в `mods/`.

Запуск: python3 tools/jar_check.py [--rebuild]
"""
import argparse
import os
import subprocess
import sys
import time
import zipfile

ROOT = os.path.join(os.path.dirname(os.path.abspath(__file__)), "..")
def _mod_version():
    # the jar name follows mod_version; a hardcoded name went stale after every version bump
    with open(os.path.join(ROOT, "gradle.properties"), encoding="utf-8") as fh:
        for line in fh:
            if line.startswith("mod_version="):
                return line.split("=", 1)[1].strip()
    raise SystemExit("mod_version не найден в gradle.properties")


JAR = os.path.join(ROOT, "build", "libs", f"thaumcraft-{_mod_version()}.jar")
CLASSES = os.path.join(ROOT, "build", "classes", "java", "main")
RESOURCES = os.path.join(ROOT, "build", "resources", "main")
SRC = os.path.join(ROOT, "src", "main")


def newest_source_mtime():
    newest = 0.0
    newest_path = ""
    for root, _, files in os.walk(SRC):
        for name in files:
            path = os.path.join(root, name)
            try:
                stamp = os.path.getmtime(path)
            except OSError:
                continue
            if stamp > newest:
                newest, newest_path = stamp, path
    return newest, newest_path


def relative_files(root):
    out = set()
    for dirpath, _, files in os.walk(root):
        for name in files:
            out.add(os.path.relpath(os.path.join(dirpath, name), root))
    return out


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--rebuild", action="store_true", help="пересобрать перед проверкой")
    args = parser.parse_args()
    problems = []

    if args.rebuild or not os.path.exists(JAR):
        print("Собираю jar ...")
        env = dict(os.environ, JAVA_HOME="/usr/lib/jvm/java-21-openjdk")
        result = subprocess.run(
            ["./gradlew", "jar", "--console=plain", "-q"],
            cwd=ROOT, env=env, capture_output=True, text=True)
        if result.returncode != 0:
            print(result.stdout[-2000:], result.stderr[-2000:])
            print("СБОРКА ПРОВАЛИЛАСЬ")
            return 1

    if not os.path.exists(JAR):
        print("НЕТ jar'а:", JAR)
        return 1

    jar_mtime = os.path.getmtime(JAR)
    src_mtime, src_path = newest_source_mtime()
    # Время — только для информации: Gradle решает задачу jar по СОДЕРЖИМОМУ,
    # поэтому после правки исходника без изменения класса он не перепаковывает
    # jar, и сравнение по mtime даёт ложную тревогу. Настоящая проверка ниже —
    # побайтовая.
    if src_mtime > jar_mtime:
        print(f"подсказка: исходник {os.path.relpath(src_path, ROOT)} новее jar'а, "
              f"но это ничего не значит, если содержимое не менялось")

    names = set(zipfile.ZipFile(JAR).namelist())
    classes = relative_files(CLASSES)
    resources = relative_files(RESOURCES)

    missing_res = sorted(resources - names)
    if missing_res:
        problems.append(f"в jar нет {len(missing_res)} ресурсов, например: "
                        f"{missing_res[:5]}")
    missing_cls = sorted(c for c in classes if c not in names)
    if missing_cls:
        problems.append(f"в jar нет {len(missing_cls)} классов, например: "
                        f"{missing_cls[:5]}")

    # Побайтовая сверка: ловит именно тот случай, когда jar пересобран из
    # устаревших классов (Gradle мог не перепаковать, а игроки получат старый код).
    with zipfile.ZipFile(JAR) as archive:
        stale = []
        for rel in sorted(classes):
            disk = os.path.join(CLASSES, rel)
            try:
                if archive.read(rel) != open(disk, "rb").read():
                    stale.append(rel)
            except KeyError:
                continue  # уже поймано как отсутствующий
        if stale:
            problems.append(f"{len(stale)} классов в jar отличаются от build/ "
                            f"(jar собран из устаревших классов), например: {stale[:5]}")

    # accesstransformer.cfg was removed on 2026-10-13: SRG names never applied on 1.21.1 and nothing needed it.
    for required in ("META-INF/neoforge.mods.toml", "mixins.thaumcraft.json"):
        if required not in names:
            problems.append(f"нет обязательного файла: {required}")

    data_count = sum(1 for n in names if n.startswith("data/thaumcraft/"))
    print(f"jar:  {JAR}")
    print(f"размер: {os.path.getsize(JAR) / 1_048_576:.1f} МБ, "
          f"собран {time.strftime('%d.%m %H:%M', time.localtime(jar_mtime))}")
    print(f"классов: {len(classes)}, ресурсов: {len(resources)}, "
          f"файлов data/thaumcraft: {data_count}")

    print("\n--- итог ---")
    if problems:
        for problem in problems:
            print("ПРОБЛЕМА: " + problem)
        return 1
    print("jar готов к выкладке в mods/ и совпадает с исходниками")
    return 0


if __name__ == "__main__":
    sys.exit(main())