#!/usr/bin/env python3
"""Замер пятен биома: связные компоненты magical_forest на скане /biomescan."""
import json
import sys
import urllib.request

BASE = "http://127.0.0.1:9888"
TARGETS = ("thaumcraft:magical_forest", "thaumcraft:taint")


def post(path, payload=None):
    req = urllib.request.Request(
        BASE + path, data=json.dumps(payload or {}).encode(),
        headers={"Content-Type": "application/json"}, method="POST")
    with urllib.request.urlopen(req, timeout=300) as r:
        return json.loads(r.read().decode())


def components(rows, legend, want, step):
    idx = {v: k for k, v in legend.items()}
    grid = [[c for c in row] for row in rows]
    h, w = len(grid), len(grid[0])
    seen = [[False] * w for _ in range(h)]
    sizes = []
    for z in range(h):
        for x in range(w):
            name = idx.get(grid[z][x], "?")
            if name in want and not seen[z][x]:
                stack, seen[z][x], n = [(x, z)], True, 0
                while stack:
                    cx, cz = stack.pop()
                    n += 1
                    for dx, dz in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                        nx, nz = cx + dx, cz + dz
                        if 0 <= nx < w and 0 <= nz < h and not seen[nz][nx] \
                                and idx.get(grid[nz][nx], "?") in want:
                            seen[nz][nx] = True
                            stack.append((nx, nz))
                sizes.append(n)
    return sizes


def main():
    x1, z1, x2, z2, step = (int(a) for a in
                            (sys.argv[1] if len(sys.argv) > 1 else "0,0,4096,4096,32").split(","))
    r = post("/biomescan", {"x1": x1, "z1": z1, "x2": x2, "z2": z2, "step": step})
    legend = {int(v): k for k, v in r["legend"].items()}
    total = sum(len(row) for row in r["rows"])
    print(f"area blocks: {(x2 - x1)}x{(z2 - z1)} step {step}, samples: {total}")
    for want in (("thaumcraft:magical_forest",), ("thaumcraft:taint",)):
        sizes = components(r["rows"], legend, set(want), step)
        cov = sum(sizes)
        if sizes:
            sizes.sort()
            med = sizes[len(sizes) // 2] * step * step
            print(f"{want[0]}: patches={len(sizes)} coverage={cov / total:.3%} "
                  f"median~{med} blocks, max~{sizes[-1] * step * step} blocks")
        else:
            print(f"{want[0]}: none found")


if __name__ == "__main__":
    main()
