# Advent of Code 2024 - Day 25

import dataclasses

type Lock = tuple[int, int, int, int, int]
type Key = tuple[int, int, int, int, int]

with open("../input/25.txt") as f:
    raw_data = f.read()


raw_test_data = """#####
.####
.####
.####
.#.#.
.#...
.....

#####
##.##
.#.##
...##
...#.
...#.
.....

.....
#....
#....
#...#
#.#.#
#.###
#####

.....
.....
#.#..
###..
###.#
###.#
#####

.....
.....
.....
#....
#.#..
#.#.#
#####"""


def process_lock(picture: str) -> Lock:
    lines = picture.splitlines()
    assert lines[0] == "#####"
    pins = lines[1:-1]
    lock = [0, 0, 0, 0, 0]
    for column in range(5):
        for depth in range(5):
            if pins[depth][column] != "#":
                lock[column] = depth
                break
        else:
            lock[column] = 5
    return tuple(lock)


def process_key(picture: str) -> Key:
    lines = picture.splitlines()
    assert lines[-1] == "#####"
    pins = lines[1:-1]
    key = [0, 0, 0, 0, 0]
    for column in range(5):
        for height in range(5):
            if pins[4 - height][column] != "#":
                key[column] = height
                break
        else:
            key[column] = 5
    return tuple(key)


@dataclasses.dataclass(frozen=True)
class Problem:
    locks: list[Lock]
    keys: list[Key]


def process(s: str) -> Problem:
    locks = []
    keys = []
    for picture in s.split("\n\n"):
        lines = picture.splitlines()
        if lines[0] == "#####":
            locks.append(process_lock(picture))
        elif lines[-1] == "#####":
            # is a key
            keys.append(process_key(picture))
        else:
            assert NotImplementedError(f"Didn't understand {picture}!")
    return Problem(locks, keys)


test_data = process(raw_test_data)
data = process(raw_data)


def overlap(lock: Lock, key: Key) -> bool:
    return any(map(lambda x, y: x + y > 5, lock, key))


def part1(problem: Problem) -> int:
    return sum(not overlap(lock, key) for lock in problem.locks for key in problem.keys)


assert part1(test_data) == 3, "Failed part 1 test"
ans1 = part1(data)
assert ans1 == 3356, "Failed part 1!"

## Part 2

## is a gimme if we got all the stars

ans2 = "Merry Christmas!"

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
