# Advent of Code - Day 11

from typing import Callable

with open("../input/11.txt") as f:
    data_string = f.read()

test_string = """...#......
.......#..
#.........
..........
......#...
.#........
.........#
..........
.......#..
#...#....."""

type Coord = tuple[int, int]


def process(s: str) -> set[Coord]:
    return set(
        (row, col)
        for row, line in enumerate(s.splitlines())
        for col, c in enumerate(line)
        if c == "#"
    )


test_data = process(test_string)
data = process(data_string)


def manhattan_distance(a: Coord, b: Coord) -> int:
    y1, x1 = a
    y2, x2 = b
    return abs(y1 - y2) + abs(x1 - x2)


def distance(data: set[Coord], factor: int = 2) -> Callable[[Coord, Coord], int]:
    Y = max(y for (y, x) in data)
    X = max(x for (y, x) in data)
    rows = set(y for (y, x) in data)
    cols = set(x for (y, x) in data)

    def between(a, b):
        return range(min(a, b), max(a, b) + 1)

    def dist(a: Coord, b: Coord):
        [y1, x1] = a
        [y2, x2] = b
        return (
            manhattan_distance(a, b)
            + (factor - 1) * sum(1 for row in between(y1, y2) if row not in rows)
            + (factor - 1) * sum(1 for col in between(x1, x2) if col not in cols)
        )

    return dist


def sum_of_distances(data: set[Coord], factor: int = 2) -> int:
    pts = list(data)
    dist = distance(data, factor)
    return sum(dist(a, b) for i, a in enumerate(pts) for b in pts[:i])


def part1(data: set[Coord]) -> int:
    return sum_of_distances(data, 2)


assert part1(test_data) == 374
ans1 = part1(data)
assert ans1 == 9648398

## Part 2


def part2(data: set[Coord]) -> int:
    return sum_of_distances(data, 1_000_000)


assert sum_of_distances(test_data, 10) == 1030
assert sum_of_distances(test_data, 100) == 8410
ans2 = part2(data)
assert ans2 == 618800410814

if __name__ == "__main__":
    print("Answer1", ans1)
    print("Answer2", ans2)
