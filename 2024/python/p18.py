# Advent of Code - Day 18

from functools import partial
from typing import Sequence
import parser
from maps import astar

type Coord = tuple[int, int]

with open("../input/18.txt") as f:
    raw_data = f.read()

extent = (70, 70)

raw_test_data = """5,4
4,2
4,5
3,0
2,1
6,3
2,4
1,5
0,6
3,3
2,6
5,1
1,2
5,5
2,5
6,5
1,4
0,4
6,4
1,1
6,1
1,0
0,5
1,6
2,0"""

test_extent = (6, 6)

parse = partial(parser.parse, parser=parser.ints)

data = parse(raw_data)
test_data = parse(raw_test_data)


def north(loc: Coord) -> Coord:
    (x, y) = loc
    return (x, y - 1)


def south(loc: Coord) -> Coord:
    (x, y) = loc
    return (x, y + 1)


def east(loc: Coord) -> Coord:
    (x, y) = loc
    return (x - 1, y)


def west(loc: Coord) -> Coord:
    (x, y) = loc
    return (x + 1, y)


def solve(data: list[Coord], step: int, extent: Coord) -> int:
    """Compute the minimum number of steps needed to solve the puzzle."""
    walls = set(data[:step])
    start = (0, 0)

    def goal(loc: Coord) -> bool:
        return loc == extent

    def cost(start: Coord, end: Coord) -> int:
        return 1

    def inside(loc: Coord) -> bool:
        (x, y) = loc
        (X, Y) = extent
        return (x >= 0) and (y >= 0) and (x <= X) and (y <= Y)

    def neighbors(loc: Coord) -> Sequence[Coord]:
        for fn in (north, south, east, west):
            new = fn(loc)
            if inside(new) and (new not in walls):
                yield new

    def heuristic(loc: Coord) -> int:
        (x, y) = loc
        (X, Y) = extent
        return abs(x - X) + abs(y - Y)

    return astar(start, goal, cost, neighbors, heuristic)


def part1(data: list[Coord], step: int, extent: Coord) -> int:
    _, cost = solve(data, step, extent)
    return cost


assert part1(test_data, 12, test_extent) == 22, "Failed part 1 test!"
ans1 = part1(data, 1024, extent)
assert ans1 == 320, "Failed part 1!"

## Part 2


def part2(data: list[Coord], extent: Coord, start: int = 1024) -> int:
    step = start
    while result := solve(data, step, extent):
        path, cost = result
        seen = set(path)
        step += 1
        while data[step - 1] not in seen:
            step += 1
    return ",".join(map(str, data[step - 1]))


assert part2(test_data, test_extent, 12) == "6,1", "Failed part 2 test!"
ans2 = part2(data, extent, 1024)
assert ans2 == "34,40", "Failed part 2"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
