# Advent of Code 2024 - Day 7

from typing import Sequence

type Ints = list[int]

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


def possibly_true(target: int, nums: Ints) -> bool:
    # test final case
    if len(nums) == 1:
        return target == nums[0]

    nums = nums[:]
    last_num = nums.pop()
    return possibly_true(target - last_num, nums) or (
        possibly_true(target // last_num, nums) if target % last_num == 0 else False
    )


def part1(data: Sequence[tuple[int, Ints]]) -> int:
    return sum(example[0] for example in data if possibly_true(*example))


assert part1(test_data) == 3749, "Failed part 1 test"
ans1 = part1(data)
assert ans1 == 1399219271639, "Failed part 1"

## Part 2


def unconcat(x, y):
    if y >= x:
        return False
    x_string = str(x)
    y_string = str(y)
    if x_string.endswith(y_string):
        return int(x_string.removesuffix(y_string))
    else:
        return False


def possibly_true2(target: int, nums: Ints) -> bool:
    # test final case
    if len(nums) == 1:
        return target == nums[0]

    nums = nums[:]
    last_num = nums.pop()
    if last_num < target and possibly_true2(target - last_num, nums):
        return True
    if target % last_num == 0 and possibly_true2(target // last_num, nums):
        return True
    if (unconcatted := unconcat(target, last_num)) and possibly_true2(
        unconcatted, nums
    ):
        return True
    return False


def part2(data: Sequence[tuple[int, Ints]]) -> int:
    return sum(example[0] for example in data if possibly_true2(*example))


assert part2(test_data) == 11387, "Failed Part 2 test"
ans2 = part2(data)
assert ans2 == 275791737999003, "Failed Part 2"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
