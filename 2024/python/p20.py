# Advent of Code - Day 20

import dataclasses
from typing import Iterator

type Coord = tuple[int, int]

with open("../input/20.txt") as f:
    raw_data = f.read()

raw_test_data = """###############
#...#...#.....#
#.#.#.#.#.###.#
#S#...#.#.#...#
#######.#.#.###
#######.#.#...#
#######.#.###.#
###..E#...#...#
###.#######.###
#...###...#...#
#.#####.#.###.#
#.#...#.#.#...#
#.#.#.#.#.#.###
#...#...#...###
###############
"""


@dataclasses.dataclass
class Race:
    walls: set[Coord]
    start: Coord
    end: Coord


def process(s: str):
    walls = set()
    start = None
    end = None
    for y, line in enumerate(s.splitlines()):
        for x, c in enumerate(line):
            loc = (x, y)
            match c:
                case "S":
                    start = loc
                case "E":
                    end = loc
                case "#":
                    walls.add(loc)

    assert start is not None
    assert end is not None

    return Race(walls, start, end)


data = process(raw_data)
test_data = process(raw_test_data)

## Logic


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


def neighbors(loc: Coord) -> Iterator[Coord]:
    for fn in (north, south, east, west):
        yield fn(loc)


def find_times(race: Race) -> dict[Coord, int]:
    """Compute the minimum number of steps needed to solve the puzzle."""

    end = race.end
    best_times = {}
    seen = set()
    frontier = [(end, 0)]

    while frontier:
        loc, best = frontier.pop(0)  # use a queue, breadth-first
        for neigh in neighbors(loc):
            if neigh not in seen and neigh not in race.walls:
                frontier.append((neigh, best + 1))
        seen.add(loc)
        best_times[loc] = best

    return best_times


def all_cheats(
    data: Race, best_times: dict[Coord, int]
) -> Iterator[tuple[tuple[Coord, Coord], int]]:
    for end, end_time in best_times.items():
        for neighbor in neighbors(end):
            if neighbor in data.walls:
                start = neighbor
                for neigh in neighbors(start):
                    if neigh != end and (start_time := best_times.get(neigh)):
                        diff = start_time - end_time - 2
                        yield ((start, end), diff)


def part1(data: Race, at_least: int) -> int:
    best_times = find_times(data)
    cheats = all_cheats(data, best_times)
    return sum(1 for (which, savings) in cheats if savings >= at_least)


assert part1(test_data, 64) == 1, "Failed part 1 test."
assert part1(test_data, 40) == 2, "Failed part 1 test."
ans1 = part1(data, 100)
assert ans1 == 1321, "Failed part 1"

## Part 2


def nearby(loc: Coord, size: int) -> Iterator[Coord]:
    (x, y) = loc
    for dx in range(-size, size + 1):
        dy_mag = size - abs(dx)
        for dy in range(-dy_mag, dy_mag + 1):
            assert dx + dy <= size
            yield (x + dx, y + dy)


def distance(start: Coord, end: Coord) -> int:
    (x1, y1) = start
    (x2, y2) = end
    return abs(x1 - x2) + abs(y1 - y2)


def all_big_cheats(
    best_times: dict[Coord, int], size: int = 20
) -> Iterator[tuple[tuple[Coord, Coord], int]]:
    for end, end_time in best_times.items():
        for start in nearby(end, size):
            if start_time := best_times.get(start):
                diff = start_time - end_time - distance(start, end)
                yield ((start, end), diff)


def part2(data: Race, at_least: int) -> int:
    best_times = find_times(data)
    cheats = {}
    for cheat, savings in all_big_cheats(best_times):
        cheats[cheat] = max(savings, cheats.get(cheat, 0))

    winners = 0
    for cheat, savings in cheats.items():
        if savings >= at_least:
            winners += 1
    return winners


assert part2(test_data, 76) == 3, "Failed part 2 test of 76"
assert (
    part2(test_data, 50)
    == 32 + 31 + 29 + 39 + 25 + 23 + 20 + 19 + 12 + 14 + 12 + 22 + 4 + 3
), "Failed part 2 test."
ans2 = part2(data, 100)
assert ans2 == 971737, "Failed part 2"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
