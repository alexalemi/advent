# Advent of Code - Day 19

import functools
import re

## Data

with open("../input/19.txt") as f:
    raw_data = f.read()

raw_test_data = """r, wr, b, g, bwu, rb, gb, br

brwrr
bggr
gbbr
rrbgbr
ubwu
bwurrg
brgr
bbrgwb"""


def process(s: str) -> tuple[set[str], list[str]]:
    towels, patterns = s.split("\n\n")
    towels = set(towels.split(", "))
    patterns = patterns.strip().splitlines()
    return towels, patterns


data = process(raw_data)
test_data = process(raw_test_data)

## Logic


def compile_pattern(towels: set[str]):
    patt = "^(" + "|".join(towels) + ")+$"
    return re.compile(patt)


def part1(data: tuple[set[str], list[str]]) -> int:
    towels, patterns = data
    patt = compile_pattern(towels)
    return sum(1 for x in patterns if patt.match(x))


assert part1(test_data) == 6, "Failed part 1 test!"
ans1 = part1(data)
assert ans1 == 233, "Failed Part 1!"

## Part 2


def make_matcher(towels: set[str]):
    @functools.cache
    def matcher(patt: str) -> int:
        if patt == "":
            return 1
        return sum(
            matcher(patt.removeprefix(towel))
            for towel in towels
            if patt.startswith(towel)
        )

    return matcher


def matches(towels: set[str], patt: str) -> int:
    """Return the number of matches of towels for patt."""
    return make_matcher(towels)(patt)


test_matcher = make_matcher(test_data[0])
assert test_matcher("brwrr") == 2
assert test_matcher("bggr") == 1
assert test_matcher("gbbr") == 4
assert test_matcher("rrbgbr") == 6
assert test_matcher("bwurrg") == 1
assert test_matcher("brgr") == 2
assert test_matcher("ubwu") == 0
assert test_matcher("bbrgwb") == 0


def part2(data: tuple[set[str], list[str]]) -> int:
    towels, patterns = data
    matcher = make_matcher(towels)
    return sum(matcher(patt) for patt in patterns)


assert part2(test_data) == 16, "Failed part 2 test!"
ans2 = part2(data)
assert ans2 == 691316989225259, "Failed part 2!"


## Main

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
