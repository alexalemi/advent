# Advent of Code 2024 - Day 2

from typing import Generator
import parser
import operator
import functools
import itertools

with open("../input/02.txt") as f:
    raw_data = f.read()

raw_test_data = """7 6 4 2 1
1 2 7 8 9
9 7 6 2 1
1 3 2 4 5
8 6 4 4 1
1 3 6 7 9"""

mapt = parser.mapt
parse = functools.partial(parser.parse, parser=parser.ints) 

data = parse(raw_data)
test_data = parse(raw_test_data)

def pairs(level: tuple[int]) -> tuple[tuple[int]]:
    return tuple(itertools.pairwise(level))

def unpack(fn):
    return lambda x: fn(*x)

def all_increasing(level: tuple[int]) -> bool:
    return all(map(unpack(operator.lt), pairs(level)))

def all_decreasing(level: tuple[int]) -> bool:
    return all(map(unpack(operator.gt), pairs(level)))

def diffs(level: tuple[int]) -> tuple[int]:
    return tuple(map(lambda x: -operator.sub(*x), pairs(level)))

def at_least_one_at_most_three_diffs(level: tuple[int]) -> bool:
    all_diffs = tuple(map(abs, diffs(level)))
    return (min(all_diffs) >= 1) and (max(all_diffs) <= 3)

def safe(level: tuple[int]) -> bool:
    return ((all_increasing(level) or all_decreasing(level)) 
            and (at_least_one_at_most_three_diffs(level)))

def part1(data: list[tuple[int]]) -> int:
    return sum(map(safe, data))

assert part1(test_data) == 2, "Part 1 test failed!"
ans1 = part1(data)
assert ans1 == 359, "Part 1 failed!"
print(f"Answer 1: {ans1}")

def dampenings(level: tuple[int]) -> Generator[tuple[int], None, None]:
    for i in range(len(level)):
        yield level[:i] + level[i+1:]

def safe_with_dampening(level: tuple[int]) -> bool:
    if safe(level):
        return True
    else:
        for dampened in dampenings(level):
            if safe(dampened):
                return True
    return False


def part2(data: list[tuple[int]]) -> int:
    return sum(map(safe_with_dampening, data))

assert part2(test_data) == 4, "Part 2 test failed!"
ans2 = part2(data)
assert ans2 == 418, "Part 2 failed!"
print(f"Answer 2: {ans2}")
