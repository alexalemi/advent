# Advent of Code 2024 - Day 3

import re
import parser
import operator
import functools
import itertools

with open("../input/03.txt") as f:
    data = f.read()

test_data = """xmul(2,4)%&mul[3,7]!@^do_not_mul(5,5)+mul(32,64]then(mul(11,8)mul(8,5))"""

def part1(data: str) -> int:
    total = 0
    for a, b in re.findall(r"mul\((\d+),(\d+)\)", data):
        total += int(a) * int(b)
    return total

assert part1(test_data) == 161, "Part 1 test failed!"
ans1 = part1(data)
assert ans1 == 174561379, "Part 1 failed!"
print(f"Answer 1: {ans1}")


test_data2 = "xmul(2,4)&mul[3,7]!^don't()_mul(5,5)+mul(32,64](mul(11,8)undo()?mul(8,5))"

def part2(data: str) -> int:
    total = 0
    valid = True
    while data:
        if valid:
            first, *rest = re.split(r"don't\(\)", data, maxsplit=1)
            total += part1(first)
            valid = False
            data = rest[0] if rest else ""
        else:
            first, *rest = re.split(r"do\(\)", data, maxsplit=1)
            valid = True
            data = rest[0] if rest else ""

    return total

assert part2(test_data2) == 48, "Part 2 test failed!"
ans2 = part2(data)
assert ans2 == 106921067, "Part 2 failed!"
print(f"Answer 2: {ans2}")
