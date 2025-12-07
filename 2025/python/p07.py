from collections import defaultdict
from typing import NamedTuple, Self


with open("../input/07.txt") as f:
    data_string = f.read()

test_string = """.......S.......
...............
.......^.......
...............
......^.^......
...............
.....^.^.^.....
...............
....^.^...^....
...............
...^.^...^.^...
...............
..^...^.....^..
...............
.^.^.^.^.^...^.
..............."""


class Loc(NamedTuple):
    row: int
    col: int

    def up(self) -> Self:
        return Loc(self.row - 1, self.col)

    def down(self) -> Self:
        return Loc(self.row + 1, self.col)

    def split(self) -> set[Self]:
        return {Loc(self.row, self.col + 1), Loc(self.row, self.col - 1)}


class Lab(NamedTuple):
    start: Loc
    splitters: set[Loc]


def process(inp: str) -> Lab:
    start = None
    splitters = set()

    for row, line in enumerate(inp.splitlines()):
        for col, chr in enumerate(line):
            match chr:
                case "S":
                    start = Loc(row, col)
                case "^":
                    splitters.add(Loc(row, col))

    return Lab(start, splitters)


test_data = process(test_string)
data = process(data_string)


def simulate(lab: Lab):
    # Track each location and how many paths led to this point
    tachyons = defaultdict(int, {lab.start: 1})
    last_row = max(loc.row for loc in lab.splitters)
    splitters = set()

    for _ in range(last_row):
        new_tachyons = defaultdict(int)

        for tachyon, count in tachyons.items():
            if (new_loc := tachyon.down()) in lab.splitters:
                splitters.add(new_loc)
                for side in new_loc.split():
                    new_tachyons[side] += count
            else:
                new_tachyons[new_loc] += count

        tachyons = new_tachyons

    return (len(splitters), sum(tachyons.values()))


assert simulate(test_data) == (21, 40), "Failed the test example!"


if __name__ == "__main__":
    answers = simulate(data)

    print(f"Answer 1: {answers[0]}")
    print(f"Answer 2: {answers[1]}")

    assert answers == (1622, 10357305916520)
