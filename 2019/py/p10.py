import math
import numpy as np
from utils import data19
import fractions
from collections import defaultdict
import bisect

data = data19(10)

tests = [
    (
        """.#..#
.....
#####
....#
...##""",
        (3, 4, 8),
    ),
    (
        """......#.#.
#..#.#....
..#######.
.#.#.###..
.#..#.....
..#....#.#
#..#....#.
.##.#..###
##...#..#.
.#....####""",
        (5, 8, 33),
    ),
    (
        """#.#...#.#.
.###....#.
.#....#...
##.#.#.#.#
....#.#.#.
.##..###.#
..#...##..
..##....##
......#...
.####.###.""",
        (1, 2, 35),
    ),
    (
        """.#..#..###
####.###.#
....###.#.
..###.##.#
##.##.#.#.
....###..#
..#.#..#.#
#..#.#.###
.##...##.#
.....#.#..""",
        (6, 3, 41),
    ),
    (
        """.#..##.###...#######
##.############..##.
.#.######.########.#
.###.#######.####.#.
#####.##.#.##.###.##
..#####..#.#########
####################
#.####....###.#.#.##
##.#################
#####.##.###..####..
..######..##.#######
####.##.####...##..#
.#####..#.######.###
##...#.##########...
#.##########.#######
.####.#.###.###.#.##
....##.##.###..#####
.#.#.###########.###
#.#.#.#####.####.###
###.##.####.##.#..##""",
        (11, 13, 210),
    ),
]


def gcd(x, y):
    while y:
        x, y = y, x % y
    return x


def blocked(b, x, y):
    guys = set()
    tot = 0
    for i, row in enumerate(b):
        for j, elem in enumerate(row):
            if elem == "#":
                if (i != x) or (j != y):
                    tot += 1
                    f = (i - x, j - y)
                    m = abs(gcd(*f))
                    f = (f[0] / m, f[1] / m)
                    guys.add(f)
    return len(guys)


def board(s):
    return [list(row) for row in s.split("\n")]


# 16 min 41 s


def answer1(inp):
    b = board(inp)
    best = 0
    loc = None
    for i, row in enumerate(b):
        for j, elem in enumerate(row):
            if elem == "#":
                num = blocked(b, i, j)
                if num > best:
                    best = num
                    loc = (i, j)
    return best, loc


tests2 = """.#....#####...#..
##...##.#####..##
##...#...#.#####.
..#.....X...###..
..#.#.....#....##"""

tests2 = [
    (0, (11, 12)),
    (1, (12, 1)),
    (2, (12, 2)),
    (9, (12, 8)),
    (19, (16, 0)),
    (49, (16, 9)),
    (99, (10, 16)),
    (198, (9, 6)),
    (199, (8, 2)),
    (200, (10, 9)),
    (298, (11, 1)),
]


def genremoval(inp, loc=(23, 20)):
    b = board(inp)
    locs = [(i, j)
            for i, row in enumerate(b)
            for j, elem in enumerate(row)
            if elem == "#"]

    x, y = loc

    toremove = defaultdict(list)

    for i, row in enumerate(b):
        for j, elem in enumerate(row):
            if elem == "#" and ((i != loc[1]) or (j != loc[0])):
                # this is a candidate asteroid
                f = (i - y, j - x)
                m = abs(gcd(*f)) or 1
                ff = (f[0] / m, f[1] / m)
                d = f[0]**2 + f[1]**2

                candlist = toremove.get(ff, [])
                bisect.insort_left(candlist, (d, (i, j)))
                toremove[ff] = candlist

    return toremove


def score(frac, base=0.0):
    angle = (math.atan2(frac[0], frac[1]) + 2 * math.pi - base +
             math.pi / 2) % (2 * math.pi)
    if angle < 1e-6:
        return 2 * math.pi
    else:
        return angle


def removeone(toremove, init_angle=0.0):
    nextfrac = min(toremove, key=lambda x: score(x, base=init_angle))
    cands = toremove[nextfrac]
    (d, (i, j)) = cands[0]
    left = cands[1:]
    if len(left) > 0:
        toremove[nextfrac] = left
    else:
        del toremove[nextfrac]
    return (i, j), nextfrac


def genplan(inp, loc=(23, 20), initangle=-1e-3):
    toremove = genremoval(inp, loc)
    x, y = loc
    current_angle = initangle

    removed = []

    while toremove:
        nxt, frac = removeone(toremove, current_angle)
        removed.append(nxt)
        current_angle = score(frac)

    return removed


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans[0] == ans[2], f"Failed on {inp} == {ans}, got {myans}"
    print("Answer1:", answer1(data))

    testplan = genplan(tests[-1][0], (11, 13))
    for inp, ans in tests2:
        assert (tuple(reversed(testplan[inp])) == ans
               ), f"Failed on {inp} == {ans}, got {testplan[inp]}!"

    ans2 = genplan(data, (23, 20))
    print("Answer2:", ans2[199][1] * 100 + ans2[199][0])
