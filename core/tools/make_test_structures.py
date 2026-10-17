#!/usr/bin/env python3
"""Writes empty (air) GameTest arenas into data/thaumcraft/structure/.

"9x5x9" writes empty_9x5x9.nbt (tests reference it with @PrefixGameTestTemplate(false) +
@GameTest(template = "empty_9x5x9")); "name=5x4x5" writes name.nbt.
Usage: python3 tools/make_test_structures.py 1x1x1 9x5x9 thaumatoriumgametest.dismantlebybreakingtop=5x5x5

"size" and "pos" must be int lists, not int arrays: StructureTemplate.load reads getList("size", 3),
so an int array reads as size 0. With size 0 the chunk the test waits for differs by one from the
chunk it force-loads, and a test placed on a chunk border never starts.
"""
import gzip
import os
import struct
import sys

DATA_VERSION = 3955  # Minecraft 1.21.1
OUT = os.path.join(os.path.dirname(__file__), "..", "src", "main", "resources", "data", "thaumcraft", "structure")


def name(s):
    b = s.encode("utf-8")
    return struct.pack(">H", len(b)) + b


def int_list(key, values):
    return b"\x09" + name(key) + b"\x03" + struct.pack(">i", len(values)) + b"".join(struct.pack(">i", v) for v in values)


def compound_list(key, compounds):
    return b"\x09" + name(key) + b"\x0a" + struct.pack(">i", len(compounds)) + b"".join(c + b"\x00" for c in compounds)


def structure(x, y, z):
    air = b"\x08" + name("Name") + name("minecraft:air")
    body = (
        int_list("size", [x, y, z])
        + compound_list("entities", [])
        # one air block: StructureTemplate.placeInWorld refuses a template without blocks,
        # and the GameTest then waits for the arena forever
        + compound_list("blocks", [int_list("pos", [0, 0, 0]) + b"\x03" + name("state") + struct.pack(">i", 0)])
        + compound_list("palette", [air])
        + b"\x03" + name("DataVersion") + struct.pack(">i", DATA_VERSION)
    )
    return b"\x0a" + name("") + body + b"\x00"


def main():
    for spec in sys.argv[1:] or ["1x1x1", "9x5x9"]:
        file_name, _, size = spec.rpartition("=")
        x, y, z = (int(v) for v in size.split("x"))
        path = os.path.join(OUT, f"{file_name or f'empty_{x}x{y}x{z}'}.nbt")
        with gzip.open(path, "wb") as fh:
            fh.write(structure(x, y, z))
        print(path)


if __name__ == "__main__":
    main()
