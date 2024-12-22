# Advent of Code - Day 22

import tqdm
import itertools
import toolz.curried as tz
import toolz

with open("../input/22.txt") as f:
    raw_data = f.read()

raw_test_data = """1
10
100
2024"""


def parse(s: str) -> tuple[int, ...]:
    return tuple(int(x) for x in s.splitlines())


test_data = parse(raw_test_data)
data = parse(raw_data)

MAX = 16777216


def evolve(x: int) -> int:
    x = (x ^ (x << 6)) % MAX
    x = (x ^ (x >> 5)) % MAX
    x = (x ^ (x << 11)) % MAX
    return x


assert tz.thread_last(123, tz.iterate(evolve), tz.drop(1), tz.take(10), tuple) == (
    15887950,
    16495136,
    527345,
    704524,
    1553684,
    12683156,
    11100544,
    12249484,
    7753432,
    5908254,
)


def nth(start: int, n: int = 2000) -> int:
    return tz.nth(n, tz.iterate(evolve, start))


assert tuple(map(nth, (1, 10, 100, 2024))) == (8685429, 4700978, 15273692, 8667524)


def part1(nums: list[int]) -> int:
    return sum(nth(x) for x in nums)


assert part1(test_data) == 37327623, "Failed part 1 test."
ans1 = part1(data)
assert ans1 == 15006633487, "Failed part 1"


## Part 2

type Changes = tuple[int, int, int, int]


def generate_payouts(num: int) -> dict[Changes, int]:
    nums = toolz.iterate(evolve, num)
    nums = toolz.take(2000, nums)
    last_digits = toolz.map(lambda x: x % 10, nums)
    last_digits, last_digits_copy = itertools.tee(last_digits, 2)
    last_digits_copy = toolz.drop(4, last_digits_copy)

    pairs = toolz.sliding_window(2, last_digits)
    diffs = toolz.map(lambda x: x[1] - x[0], pairs)
    quadruplets = toolz.sliding_window(4, diffs)

    payouts = {}
    for quad, val in zip(quadruplets, last_digits_copy):
        if quad not in payouts:
            payouts[quad] = val
    return payouts


def merge(dic: dict[Changes, int], other: dict[Changes, int]):
    for key, value in other.items():
        dic[key] = value + dic.get(key, 0)


def part2(nums: list[int]) -> int:
    payouts: dict[tuple[int, int, int, int], int] = {}
    for num in tqdm.tqdm(nums):
        merge(payouts, generate_payouts(num))
    return max(payouts.values())


test_data2 = [1, 2, 3, 2024]

assert part2(test_data2) == 23, "Failed part 2 test"
ans2 = part2(data)
assert ans2 == 1710, "Failed part 2"

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
