# Advent of Code 2024 - Day 7

from functools import partial
from typing import Sequence, Callable

type Ints = list[int]
type UnFunc = Callable[[int, int], int | None]

with open("../input/07.txt") as f:
    raw_data = f.read()

raw_test_data = """190: 10 19
3267: 81 40 27
83: 17 5
156: 15 6
7290: 6 8 6 15
161011: 16 10 13
192: 17 8 14
21037: 9 7 18 13
292: 11 6 16 20"""


def process(s: str) -> Sequence[tuple[int, Ints]]:
    output = []
    for line in s.splitlines():
        target, nums = line.split(":")
        output.append((int(target), list(map(int, nums.split()))))
    return output


data = process(raw_data)
test_data = process(raw_test_data)


def unadd(target: int, num: int) -> int | None:
    if num < target:
        return target - num


def unmul(target: int, num: int) -> int | None:
    if target % num == 0:
        return target // num


def possibly_true(
    target: int, nums: Ints, branches: Sequence[UnFunc] = (unadd, unmul)
) -> bool:
    # test final case
    if len(nums) == 1:
        return target == nums[0]

    nums = nums[:]
    last_num = nums.pop()
    for branch in branches:
        if new_num := branch(target, last_num):
            if possibly_true(new_num, nums, branches):
                return True
    else:
        return False


def part1(data: Sequence[tuple[int, Ints]]) -> int:
    return sum(example[0] for example in data if possibly_true(*example))


assert part1(test_data) == 3749, "Failed part 1 test"
ans1 = part1(data)
assert ans1 == 1399219271639, "Failed part 1"

## Part 2


def unconcat(x, y):
    x_string = str(x)
    y_string = str(y)
    if x_string.endswith(y_string):
        return int(x_string.removesuffix(y_string) or 0)


def part2(data: Sequence[tuple[int, Ints]]) -> int:
    possibly_true2 = partial(possibly_true, branches=(unadd, unmul, unconcat))
    return sum(example[0] for example in data if possibly_true2(*example))


assert part2(test_data) == 11387, "Failed Part 2 test"
ans2 = part2(data)
assert ans2 == 275791737999003, "Failed Part 2"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
