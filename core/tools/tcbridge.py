#!/usr/bin/env python3
"""ThaumicReforged dev bridge CLI.

Мод поднимает локальный HTTP-мост (только dev-сборки): клиент — порт 9887,
выделенный сервер — 9888. Мост выполняет всё в игровом (серверном) потоке
синхронно, поэтому можно спокойно делать цепочки «команда → прочитать → кликнуть».

Базовые команды:
  status / help / wait N
  exec "<команда>" / execs "cmd1" "cmd2" ...
  block x y z [side] / energy x y z [side] / fluids x y z [side]
  items player | items x y z [side]
  players / player x y z [yaw] [pitch] [--player fake] / break x y z
  use x y z [side] / hand <item|none>        — ПКМ предметом из руки / предмет в руку
  use-entity x y z [r]                       — ПКМ по ближайшей сущности (ядра големов…)
  entities x y z [r] [--dim id]
  recipe <item>
  menu / open x y z / click <slot> [button] [type] / button <id> / close
  map x1 z1 x2 z2 y / shot [name]

Thaumcraft-механики (BridgeActions):
  scan / scanhand / scanat x y z              — просканировать (как таумометром)
  knowledge                                   — знания игрока (аспекты, исследования, варп)
  discover <aspect> / pool <aspect> <amount>  — выдать аспект/очки в пул
  research / autoresearch                     — открытые исследования / открыть все
  unlockresearch <key>                        — открыть одно исследование
  crucible info x y z
  crucible insert x y z <item>
  crucible fill x y z [water]
  selftest                                    — серверный самотест знаний
  node x y z                                  — состояние узла ауры (аспекты/тип/модификатор)
  node-set x y z aer=100,ignis=40 [--type DARK] [--modifier BRIGHT]
  node-drain x y z <aspect> <amount>          — снять аспект (проверка регенерации)
  node-drops x y z                            — дропы узла (эссенция виспов)
  essentia x y z                              — контейнер/транспорт: аспекты + сакция по сторонам
  essentia-insert x y z <aspect> <amount>     — влить эссенцию (банка, алембик…)
  essentia-extract x y z <aspect> <amount>    — вылить эссенцию
  valve x y z [open|close]                    — состояние/переключение клапанной трубки
  filter-tube x y z [aspect|none]             — фильтр трубки (чтение/смена)
  pedestal x y z                              — что лежит на арканном пьедестале
  pedestal-set x y z <item|none>              — положить/снять предмет
  infusion x y z                              — состояние матрицы (структура, крафт, рецепт)
  infusion-start x y z                        — принудительно запустить инфузию
  wand                                        — жезл в руке (стержень, навершие, вис)
  wand-give [rod] [cap]                       — выдать жезл (0 greatwood / 1 silverwood)
  wand-set <aspect> <vis>                     — установить вис в жезле
  wand-add <aspect> <vis>                     — добавить вис
  wand-drain x y z [amount]                   — высосать вис из узла в жезл
  wand-item x y z <slot>                      — жезл из слота контейнера
  wand-focus [item|none]                      — прочитать/поставить фокус на жезл
  wand-cast [block|entity] [x y z]            — каст фокуса (по взгляду / по блоку / по сущности рядом)
  golem x y z [r] [--uuid id]                  — ближайший голем / голем по UUID (материал, задача, ноша)
  golem-spawn x y z [material]                — заспавнить голема (wood/straw/clay/stone/flesh/iron/thaumium/tallow)
  golem-task [x y z] <task> [--uuid id]       — задача голема (none/gather)
  golem-carry [x y z] [item|none] [count] [--uuid id] — вложить/забрать ношу голема
  golem-clear x y z [r]                       — убрать всех големов в зоне

Клиентские (только runClient):
  screen thaumonomicon|inventory|close / screen-click x y
  connect <host> / world / reload / quit

Примеры:
  tools/tcbridge.py status
  tools/tcbridge.py exec "/time set day"
  tools/tcbridge.py crucible info 100 64 -200
  tools/tcbridge.py autoresearch
  tools/tcbridge.py knowledge
"""

import json
import os
import sys
import urllib.request

sys.path.insert(0, os.path.join(os.path.dirname(os.path.abspath(__file__)), "..", "..", "..", "_shared", "devbridge"))
import bridgekit  # noqa: E402

BASE = "http://127.0.0.1:9887"
bridgekit.configure(BASE)


def call(path, payload=None, timeout=60):
    try:
        return bridgekit.call(path, payload, timeout)
    except bridgekit.BridgeDown as error:
        print(f"bridge unreachable: {error}", file=sys.stderr)
        sys.exit(1)


def pretty(data):
    print(json.dumps(data, indent=2, ensure_ascii=False))


def get(path, **query):
    url = BASE + path
    if query:
        url += "?" + "&".join(f"{k}={v}" for k, v in query.items())
    request = urllib.request.Request(url, headers={"Accept": "application/json"})
    with urllib.request.urlopen(request, timeout=30) as response:
        return json.loads(response.read().decode("utf-8"))


def flag(args, name, default=None):
    if name in args:
        index = args.index(name)
        value = args[index + 1]
        del args[index : index + 2]
        return value
    return default


def main(argv):
    global BASE
    if "--base" in argv:
        index = argv.index("--base")
        BASE = argv[index + 1]
        bridgekit.configure(BASE)
        argv = argv[:index] + argv[index + 2:]

    player = flag(argv, "--player", None)
    dim = flag(argv, "--dim", None)
    entity_uuid = flag(argv, "--uuid", None)

    command = argv[0] if argv else "status"
    args = argv[1:]

    def with_player(payload):
        if player:
            payload["player"] = player
        if dim:
            payload["dim"] = dim
        return payload

    if command == "status":
        pretty(call("/status"))
    elif command == "help":
        pretty(call("/help"))
    elif command == "exec":
        data = call("/exec", {"command": " ".join(args)}, timeout=120)
        for line in data.get("output", []):
            print(line)
        if not data.get("ok", True):
            print("FAILED:", data.get("error"))
            sys.exit(1)
    elif command == "execs":
        data = call("/exec", {"commands": args}, timeout=300)
        for line in data.get("output", []):
            print(line)
        if not data.get("ok", True):
            print("FAILED:", data.get("error"))
            sys.exit(1)
    elif command == "wait":
        pretty(call("/wait", {"ticks": int(args[0]) if args else 20}, timeout=300))
    elif command in ("block", "energy", "fluids"):
        x, y, z = args[:3]
        query = {"x": x, "y": y, "z": z}
        if len(args) > 3:
            query["side"] = args[3]
        pretty(get(f"/{command}", **query))
    elif command == "items":
        if not args or args[0] == "player":
            pretty(get("/items", player="player"))
        else:
            query = {"x": args[0], "y": args[1], "z": args[2]}
            if len(args) > 3:
                query["side"] = args[3]
            pretty(get("/items", **query))
    elif command == "players":
        pretty(call("/players"))
    elif command == "player":
        x, y, z = args[:3]
        payload = {"x": float(x), "y": float(y), "z": float(z)}
        if len(args) > 3:
            payload["yaw"] = float(args[3])
        if len(args) > 4:
            payload["pitch"] = float(args[4])
        pretty(call("/player", with_player(payload)))
    elif command == "break":
        pretty(call("/break", with_player({"x": int(args[0]), "y": int(args[1]), "z": int(args[2])})))
    elif command == "use":
        payload = {"x": int(args[0]), "y": int(args[1]), "z": int(args[2])}
        if len(args) > 3:
            payload["side"] = args[3]
        pretty(call("/use", with_player(payload)))
    elif command == "hand":
        pretty(call("/hand", with_player({"item": args[0] if args else "none"})))
    elif command == "use-entity":
        payload = {"x": float(args[0]), "y": float(args[1]), "z": float(args[2])}
        if len(args) > 3:
            payload["r"] = float(args[3])
        pretty(call("/use/entity", with_player(payload)))
    elif command == "entities":
        payload = {"x": float(args[0]), "y": float(args[1]), "z": float(args[2])}
        if len(args) > 3:
            payload["r"] = float(args[3])
        pretty(call("/entities", with_player(payload)))
    elif command == "recipe":
        pretty(call("/recipe", {"item": args[0]}))
    elif command == "menu":
        pretty(call("/menu", with_player({})))
    elif command == "open":
        pretty(call("/menu/open", with_player({"x": int(args[0]), "y": int(args[1]), "z": int(args[2])})))
    elif command == "click":
        payload = {"slot": int(args[0])}
        if len(args) > 1:
            payload["button"] = int(args[1])
        if len(args) > 2:
            payload["type"] = args[2]
        pretty(call("/menu/click", with_player(payload)))
    elif command == "button":
        pretty(call("/menu/button", with_player({"id": int(args[0])})))
    elif command == "close":
        pretty(call("/menu/close", with_player({})))
    elif command == "map":
        x1, z1, x2, z2, y = (int(value) for value in args[:5])
        query = {"x1": x1, "z1": z1, "x2": x2, "z2": z2, "y": y}
        if dim:
            query["dim"] = dim
        data = get("/map", **query)
        print("\n".join(data.get("rows", [])))
        legend = data.get("legend", {})
        print("legend: " + ", ".join(f"{key}={value}" for key, value in legend.items()))
    elif command == "shot":
        pretty(call("/screenshot", {"path": args[0] if args else "bridge"}))
    # --- Thaumcraft ---
    elif command == "scan":
        pretty(call("/scan", with_player({})))
    elif command == "scanhand":
        pretty(call("/scanhand", with_player({})))
    elif command == "scanat":
        pretty(call("/scanat", with_player({"x": int(args[0]), "y": int(args[1]), "z": int(args[2])})))
    elif command == "knowledge":
        pretty(call("/knowledge", with_player({})))
    elif command == "discover":
        pretty(call("/discover", with_player({"aspect": args[0]})))
    elif command == "pool":
        pretty(call("/pool", with_player({"aspect": args[0], "amount": int(args[1])})))
    elif command == "research":
        pretty(call("/research", with_player({})))
    elif command == "autoresearch":
        pretty(call("/autoresearch", with_player({})))
    elif command == "unlockresearch":
        pretty(call("/unlockresearch", with_player({"key": args[0]})))
    elif command == "crucible":
        action = args[0]
        x, y, z = (int(value) for value in args[1:4])
        payload = {"x": x, "y": y, "z": z}
        if action == "insert":
            payload["item"] = args[4]
        elif action == "fill" and len(args) > 4:
            payload["water"] = int(args[4])
        pretty(call(f"/crucible/{action}", with_player(payload)))
    elif command == "selftest":
        pretty(call("/knowledge/selftest", with_player({})))
    elif command == "node":
        pretty(get("/node", x=args[0], y=args[1], z=args[2]))
    elif command == "node-set":
        aspects = {}
        node_type = None
        modifier = None
        rest = args[3:]
        index = 0
        while index < len(rest):
            if rest[index] == "--type":
                node_type = rest[index + 1]
                index += 2
            elif rest[index] == "--modifier":
                modifier = rest[index + 1]
                index += 2
            else:
                for part in rest[index].split(","):
                    if "=" in part:
                        key, value = part.split("=", 1)
                        aspects[key.strip()] = int(value)
                index += 1
        payload = {"x": int(args[0]), "y": int(args[1]), "z": int(args[2])}
        if aspects:
            payload["aspects"] = aspects
        if node_type:
            payload["type"] = node_type
        if modifier:
            payload["modifier"] = modifier
        pretty(call("/node/config", payload))
    elif command == "node-drain":
        pretty(call("/node/config", {
            "x": int(args[0]), "y": int(args[1]), "z": int(args[2]),
            "drain": {args[3]: int(args[4])},
        }))
    elif command == "node-drops":
        pretty(get("/node/drops", x=args[0], y=args[1], z=args[2]))
    elif command == "essentia":
        pretty(get("/essentia", x=args[0], y=args[1], z=args[2]))
    elif command == "essentia-insert":
        pretty(call("/essentia/insert", {
            "x": int(args[0]), "y": int(args[1]), "z": int(args[2]),
            "aspect": args[3], "amount": int(args[4]),
        }))
    elif command == "essentia-extract":
        pretty(call("/essentia/extract", {
            "x": int(args[0]), "y": int(args[1]), "z": int(args[2]),
            "aspect": args[3], "amount": int(args[4]),
        }))
    elif command == "valve":
        payload = {"x": int(args[0]), "y": int(args[1]), "z": int(args[2])}
        if len(args) > 3:
            payload["open"] = args[3].lower() in ("open", "true", "1", "on", "открыть")
        pretty(call("/tube/valve", payload))
    elif command == "filter-tube":
        payload = {
            "x": int(args[0]), "y": int(args[1]), "z": int(args[2]),
            "aspect": args[3] if len(args) > 3 else "none",
        }
        pretty(call("/tube/filter", payload))
    elif command == "pedestal":
        pretty(get("/pedestal", x=args[0], y=args[1], z=args[2]))
    elif command == "pedestal-set":
        pretty(call("/pedestal/set", {
            "x": int(args[0]), "y": int(args[1]), "z": int(args[2]),
            "item": args[3] if len(args) > 3 else "",
        }))
    elif command == "infusion":
        pretty(get("/infusion", x=args[0], y=args[1], z=args[2]))
    elif command == "infusion-start":
        pretty(call("/infusion/start", {"x": int(args[0]), "y": int(args[1]), "z": int(args[2])}))
    elif command == "wand":
        pretty(call("/wand", with_player({})))
    elif command == "wand-give":
        payload = {"rod": int(args[0]) if args else 0, "cap": int(args[1]) if len(args) > 1 else 0}
        pretty(call("/wand/give", with_player(payload)))
    elif command in ("wand-set", "wand-add"):
        pretty(call("/wand/vis", with_player({"aspect": args[0], "amount": int(args[1]), "set": command == "wand-set"})))
    elif command == "wand-drain":
        payload = {"x": int(args[0]), "y": int(args[1]), "z": int(args[2])}
        if len(args) > 3:
            payload["amount"] = int(args[3])
        pretty(call("/wand/drain", with_player(payload)))
    elif command == "wand-item":
        pretty(call("/wand/item", {"x": int(args[0]), "y": int(args[1]), "z": int(args[2]), "slot": int(args[3])}))
    elif command == "wand-focus":
        payload = {"item": args[0]} if args else {}
        pretty(call("/wand/focus", with_player(payload)))
    elif command == "wand-cast":
        payload = {}
        if args and args[0] in ("block", "entity"):
            payload = {"mode": args[0], "x": int(args[1]), "y": int(args[2]), "z": int(args[3])}
        pretty(call("/wand/cast", with_player(payload)))
    elif command == "golem":
        payload = {"x": float(args[0]) if args else 0.0, "y": float(args[1]) if len(args) > 1 else 0.0,
                   "z": float(args[2]) if len(args) > 2 else 0.0}
        if len(args) > 3:
            payload["r"] = float(args[3])
        if entity_uuid:
            payload["uuid"] = entity_uuid
        pretty(call("/golem", payload))
    elif command == "golem-spawn":
        payload = {
            "x": float(args[0]), "y": float(args[1]), "z": float(args[2]),
            "material": args[3] if len(args) > 3 else "wood",
        }
        pretty(call("/golem/spawn", with_player(payload)))
    elif command == "golem-task":
        payload = {
            "x": float(args[0]) if args else 0.0, "y": float(args[1]) if len(args) > 1 else 0.0,
            "z": float(args[2]) if len(args) > 2 else 0.0,
            "task": args[3] if len(args) > 3 else "none",
        }
        if entity_uuid:
            payload["uuid"] = entity_uuid
        pretty(call("/golem/task", payload))
    elif command == "golem-carry":
        payload = {
            "x": float(args[0]) if args else 0.0, "y": float(args[1]) if len(args) > 1 else 0.0,
            "z": float(args[2]) if len(args) > 2 else 0.0,
            "item": args[3] if len(args) > 3 else "none",
            "count": int(args[4]) if len(args) > 4 else 1,
        }
        if entity_uuid:
            payload["uuid"] = entity_uuid
        pretty(call("/golem/carry", payload))
    elif command == "golem-clear":
        payload = {"x": float(args[0]), "y": float(args[1]), "z": float(args[2])}
        if len(args) > 3:
            payload["r"] = float(args[3])
        pretty(call("/golem/clear", payload))
    # --- клиентские ---
    elif command == "screen":
        pretty(call("/screen", {"name": args[0]}))
    elif command == "screen-click":
        pretty(call("/screen/click", {"x": float(args[0]), "y": float(args[1])}))
    elif command == "connect":
        pretty(call("/connect", {"host": args[0]}))
    elif command == "world":
        pretty(call("/world", {}))
    elif command == "reload":
        pretty(call("/reload", {}))
    elif command == "quit":
        pretty(call("/quit", {}))
    else:
        print(__doc__, file=sys.stderr)
        sys.exit(2)


if __name__ == "__main__":
    main(sys.argv[1:])
