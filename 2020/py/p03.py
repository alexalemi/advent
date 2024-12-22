import time
from utils import data20
import math

data = data20(3)

tests = [
    (
        """..##.......
#...#...#..
.#....#..#.
..#.#...#.#
.#...##..#.
..#.##.....
.#.#.#....#
.#........#
#.##...#...
#...##....#
.#..#...#.#""",
        7,
    )
]


def process(inp):
    vals = {}
    for y, line in enumerate(inp.splitlines()):
        for x, c in enumerate(line):
            vals[complex(x, y)] = c
    return vals


def answer1(inp, slope=(3, 1)):
    vals = process(inp)
    bottom = max(z.imag for z in vals)
    right = max(z.real for z in vals)
    start = complex(0, 0)
    pos = start
    total = 0
    while pos.imag < bottom:
        pos = pos + complex(*slope)
        pos = complex(pos.real % (right + 1), pos.imag)
        if vals[pos] == "#":
            total += 1
    return total


tests2 = [(tests[0][0], 336)]


def answer2(inp):
    return math.prod(
        answer1(inp, slope) for slope in [(1, 1), (3, 1), (5, 1), (7, 1), (1, 2)]
    )


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    start = time.time()
    ans1 = answer1(data)
    end = time.time()
    print("Answer1:", ans1, f" in {end - start:0.3e} secs")

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    start = time.time()
    ans2 = answer2(data)
    end = time.time()
    print("Answer2:", ans2, f" in {end - start:0.3e} secs")
