# Advent of Code 2023 - Day 21

from typing import NamedTuple
from collections.abc import Generator
from tqdm import trange

with open("../input/21.txt") as f:
    data_string = f.read()

test_string = """...........
.....###.#.
.###.##..#.
..#.#...#..
....#.#....
.##..S####.
.##..#...#.
.......##..
.##.#.####.
.##..##.##.
..........."""

type Coord = tuple[int, int]


class Data(NamedTuple):
    walls: set[Coord]
    start: Coord
    extent: Coord


def process(s: str) -> Data:
    walls = set()
    start = None
    for row, line in enumerate(s.splitlines()):
        for col, c in enumerate(line):
            if c == "#":
                walls.add((row, col))
            elif c == "S":
                start = (row, col)
    extent = (len(s.splitlines()), len(s.splitlines()[0]))
    return Data(walls, start, extent)


test_data = process(test_string)
data = process(data_string)


def up(loc: Coord) -> Coord:
    y, x = loc
    return (y - 1), x


def down(loc: Coord) -> Coord:
    y, x = loc
    return (y + 1), x


def left(loc: Coord) -> Coord:
    y, x = loc
    return y, (x - 1)


def right(loc: Coord) -> Coord:
    y, x = loc
    return y, (x + 1)


def neighbors(loc: Coord) -> Generator[Coord, None, None]:
    yield up(loc)
    yield down(loc)
    yield left(loc)
    yield right(loc)


def project(extent: Coord, loc: Coord) -> Coord:
    (Y, X) = extent
    (y, x) = loc
    return (y % Y, x % X)


def even(t: int) -> bool:
    return (t % 2) == 0


def expand_naive_frontier(data: Data, frontier: set[Coord]) -> set[Coord]:
    walls, start, extent = data
    return set(
        neighbor
        for loc in frontier
        for neighbor in neighbors(loc)
        if project(extent, neighbor) not in walls
    )


def run_forward(data: Data, n: int) -> set[Coord]:
    seen = set([data.start])
    for i in trange(n):
        seen = expand_naive_frontier(data, seen)
    return seen


import numpy as np


def edge(data, step):
    y, x = data.start
    return (
        set((y + (step - i), x - i) for i in range(step + 1))
        | set((y + (step - i), x + i) for i in range(step + 1))
        | set((y - (step - i), x + i) for i in range(step + 1))
        | set((y - (step - i), x - i) for i in range(step + 1))
    )


def visualize(data: Data, seen: set[Coord], n=1):
    walls, start, extent = data
    Y, X = extent
    YY = Y * n
    XX = X * n
    pic = np.zeros((YY, XX))

    def offset(loc: Coord) -> Coord:
        y, x = loc
        k = n // 2
        return y + k * Y, x + k * X

    for x in seen:
        try:
            pic[offset(x)] = 1
        except:
            pass

    for y in range(YY):
        for x in range(XX):
            if project(extent, (y, x)) in walls:
                pic[y, x] = -1

    return pic


def expand_infinite_frontier(data: Data, n: int, frontier=None, prev=None) -> int:
    walls, start, extent = data
    frontier = frontier or set([start])
    prev = prev or set()
    seen_odd = 0
    seen_even = 0

    for t in trange(n):
        new = set(
            neighbor
            for loc in frontier
            for neighbor in neighbors(loc)
            if ((project(extent, neighbor) not in walls) and (neighbor not in prev))
        )
        if even(t):
            seen_odd += len(prev)
        else:
            seen_even += len(prev)

        frontier, prev = new, frontier

    if even(n):
        return seen_even + len(frontier)
    else:
        return seen_odd + len(frontier)


# assert expand_infinite_frontier(test_data, 6) == 16
# assert expand_infinite_frontier(test_data, 10) == 50
# assert expand_infinite_frontier(test_data, 50) == 1594
# assert expand_infinite_frontier(test_data, 100) == 6536
# assert expand_infinite_frontier(test_data, 500) == 167004
# # assert expand_infinite_frontier(test_data, 1000) == 668697
# assert expand_infinite_frontier(test_data, 5000) == 16733044

ans1 = expand_infinite_frontier(data, 64)
assert ans1 == 3748

# The thing is quadratic

target = 26501365


def part2(data: Data, target: int = target) -> int:
    Y, X = data.extent
    assert Y == X, "Grid isn't square!"
    y0, x0 = data.start
    assert (y0 == Y // 2) and (x0 == X // 2), "Doesn't start in middle!"

    offset = y0
    size = Y

    iters = (target - offset) // size
    assert iters * size + offset == target, "Doesn't evenly divide out!"

    def step(n: int) -> int:
        return offset + n * size

    f0 = expand_infinite_frontier(data, step(0))
    f2 = expand_infinite_frontier(data, step(2))
    f4 = expand_infinite_frontier(data, step(4))

    # f(n) = x * ( a x + b ) + c
    # f(0) = c
    # f(2) = 4a + 2b + c
    # f(4) = 16a + 4b + c
    # f(2) - f(0) = 4a + 2b
    # f(4) - f(2) = 12a + 2b
    # f(4) - 2f(2) + f(0) = 8a

    c = f0
    a = (f4 - 2 * f2 + f0) // 8
    b = (f2 - 4 * a - c) // 2

    return iters * (a * iters + b) + c


ans2 = part2(data)
assert ans2 == 616951804315987


if __name__ == "__main__":
    print("Answer1:", ans1)
    print("Answer2:", ans2)
