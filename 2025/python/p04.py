from typing import Generator


def coord(i: int, j: int) -> complex:
    return j + i * 1j


def neighbors(coord: complex) -> Generator[complex, None, None]:
    yield coord + 1
    yield coord - 1
    yield coord + 1j
    yield coord - 1j
    yield coord + 1 + 1j
    yield coord - 1 + 1j
    yield coord + 1 - 1j
    yield coord - 1 - 1j


def process(s) -> set[complex]:
    rolls = set()
    for i, line in enumerate(s):
        for j, x in enumerate(line):
            if x == "@":
                rolls.add(coord(i, j))
    return rolls


list(neighbors(1))

test_string = """..@@.@@@@.
@@@.@.@.@@
@@@@@.@.@@
@.@@@@..@.
@@.@@@@.@@
.@@@@@@@.@
.@.@.@.@@@
@.@@@.@@@@
.@@@@@@@@.
@.@.@@@.@."""

with open("../input/04.txt") as f:
    data = process(f)

test_data = process(test_string.splitlines())


def reachable(rolls: set[complex]) -> set[complex]:
    can_reach = set()
    for roll in rolls:
        if len(set(neighbors(roll)) & rolls) < 4:
            can_reach.add(roll)
    return can_reach


reachable(test_data)


def part1(data: set[complex]) -> int:
    return len(reachable(data))


assert part1(test_data) == 13, "Failed part 1 test!"


def part2(data: set[complex]) -> int:
    total = 0
    while can_reach := reachable(data):
        total += len(can_reach)
        data -= can_reach
    return total


assert part2(test_data) == 43, "Failed part 2 test!"


if __name__ == "__main__":
    ans1 = part1(data)
    print(f"Answer 1: {ans1}")

    ans2 = part2(data)
    print(f"Answer 2: {ans2}")
