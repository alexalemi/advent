# Advent of Code 2024 - Day 8

from collections import defaultdict
import itertools
import string

type Loc = tuple[int, int]
type Board = set[Loc]
type Antenna = dict[str, set[Loc]]

with open("../input/08.txt") as f:
    raw_data = f.read()

raw_test_data = """............
........0...
.....0......
.......0....
....0.......
......A.....
............
............
........A...
.........A..
............
............"""


def process(s: str) -> tuple[Antenna, Board]:
    chrs = set(string.ascii_uppercase + string.ascii_lowercase + string.digits)
    antenna = defaultdict(set)
    board = set()
    for y, line in enumerate(s.splitlines()):
        for x, c in enumerate(line):
            loc = (x, y)
            board.add(loc)
            if c in chrs:
                antenna[c].add(loc)

    return antenna, board


data = process(raw_data)
test_data = process(raw_test_data)


def antinodes(a: Loc, b: Loc) -> set[Loc]:
    x1, y1 = a
    x2, y2 = b
    dx = x2 - x1
    dy = y2 - y1

    return {(x1 - dx, y1 - dy), (x2 + dx, y2 + dy)}


def build_antinodes(data: tuple[Antenna, Board]) -> set[Loc]:
    antenna, board = data
    nodes = set()
    for freq, locs in antenna.items():
        for a, b in itertools.combinations(locs, 2):
            nodes |= antinodes(a, b) & board
    return nodes


def part1(data: tuple[Antenna, Board]) -> int:
    return len(build_antinodes(data))


assert part1(test_data) == 14, "Failed Part 1 test."
ans1 = part1(data)
assert ans1 == 280, "Failed Part 1"

## Part 2


def all_antinodes(a: Loc, b: Loc, board: Board) -> set[Loc]:
    x1, y1 = a
    x2, y2 = b
    dx = x2 - x1
    dy = y2 - y1

    antinodes = {a, b}
    n = 1
    while (loc := (x2 + n * dx, y2 + n * dy)) in board:
        antinodes.add(loc)
        n += 1
    n = 1
    while (loc := (x1 - n * dx, y1 - n * dy)) in board:
        antinodes.add(loc)
        n += 1

    return antinodes


def build_all_antinodes(data: tuple[Antenna, Board]) -> set[Loc]:
    antenna, board = data
    nodes = set()
    for freq, locs in antenna.items():
        for a, b in itertools.combinations(locs, 2):
            nodes |= all_antinodes(a, b, board)
    return nodes


def part2(data: tuple[Antenna, Board]) -> int:
    return len(build_all_antinodes(data))


assert part2(test_data) == 34, "Failed Part 2 test."
ans2 = part2(data)
assert ans2 == 958, "Failed part 2."


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
