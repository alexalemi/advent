# Advent of Code 2024 - Day 11

from typing import TypeVar, Generator, Callable
import itertools
from functools import partial
from collections import defaultdict

type Counts = dict[int, int]

with open("../input/11.txt") as f:
    raw_data = f.read()

raw_test_data = "125 17"


def process(s: str) -> Counts:
    counts = defaultdict(int)
    for part in s.strip().split():
        num = int(part)
        counts[num] += 1
    return counts


data = process(raw_data)
test_data = process(raw_test_data)  # None
# defaultdict(<class 'int'>, {125: 1, 17: 1})


def even(x: int) -> bool:
    return x % 2 == 0


def change(x: int) -> list[int]:
    if x == 0:
        return [1]
    elif even(len(x_str := str(x))):
        left = x_str[: len(x_str) // 2]
        right = x_str[len(x_str) // 2 :]
        return [int(left), int(right)]
    else:
        return [x * 2024]


def generation(counts: Counts) -> Counts:
    output = defaultdict(int)
    for num, count in counts.items():
        for x in change(num):
            output[x] += count
    return output


def take(n, iterable):
    return list(itertools.islice(iterable, n))


def nth(n, iterable):
    return list(itertools.islice(iterable, n + 1))[-1]


T = TypeVar("T")


def iterate(fn: Callable[[T], T], x: T) -> Generator[T, None, None]:
    while True:
        yield x
        x = fn(x)


def count(counter: Counts) -> int:
    return sum(counter.values())


def part1(data: Counts, n: int = 25) -> int:
    return count(nth(n, iterate(generation, data)))


assert part1(test_data, 6) == 22, "Failed part 1 simple test."
assert part1(test_data) == 55312, "Failed part 1 test."
ans1 = part1(data)
assert ans1 == 193899, "Failed part 1"

## Part 2

part2 = partial(part1, n=75)

ans2 = part2(data)
assert ans2 == 229682160383225, "Failed part 2!"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
