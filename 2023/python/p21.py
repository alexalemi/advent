# Advent of Code 2023 - Day 21

from typing import NamedTuple
from collections.abc import Generator

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

def expand_infinite_frontier(data: Data, n: int) -> int:
    walls, start, extent = data
    frontier = set([start])
    seen_odd = set()
    seen_even = set([start])

    for t in range(n):
        # print(f"{frontier=}, {seen_even=}, {seen_odd=}")
        frontier = set([neighbor for loc in frontier for neighbor in neighbors(loc) if ((project(extent, neighbor) not in walls) and (neighbor not in seen_odd) and (neighbor not in seen_even))])
        if even(t):
            seen_odd |= frontier
        else:
            seen_even |= frontier
    
    if even(n):
        return len(seen_even)
    else:
        return len(seen_odd)

# assert expand_infinite_frontier(test_data, 6) == 16
# assert expand_infinite_frontier(test_data, 10) == 50
# assert expand_infinite_frontier(test_data, 50) == 1594
# assert expand_infinite_frontier(test_data, 100) == 6536
# assert expand_infinite_frontier(test_data, 500) == 167004
# assert expand_infinite_frontier(test_data, 1000) == 668697
# assert expand_infinite_frontier(test_data, 5000) == 16733044

ans1 = expand_infinite_frontier(data, 64)
print("Answer1:", ans1)


ans2 = expand_infinite_frontier(data, 26501365)
print("Answer2:", ans2)


