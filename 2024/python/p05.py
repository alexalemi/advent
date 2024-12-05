# Advent of Code 2024 - Day 5

from typing import Callable
import parser
import functools

type Rule = tuple[int, int]
type Rules = tuple[Rule, ...]
type Update = tuple[int, ...]
type Updates = tuple[Update, ...]
type Children = dict[int, set[int]]

parse = functools.partial(parser.parse, parser=parser.digits)


def process(s: str) -> tuple[Rules, Updates]:
    rules, sections = s.split("\n\n")
    return (parse(rules), parse(sections))


with open("../input/05.txt") as f:
    data = process(f.read())

test_string = """47|53
97|13
97|61
97|47
75|29
61|13
75|53
29|13
97|29
53|29
61|53
97|53
61|29
47|13
75|47
97|75
47|61
75|61
47|29
75|13
53|13

75,47,61,53,29
97,61,53,29,13
75,29,13
75,97,47,61,53
61,13,29
97,13,75,29,47"""

test_data = process(test_string)


def build_sort(rules: Rules) -> Callable[[tuple[int, ...]], tuple[int, ...]]:
    rule_set = set(rules)

    def cmp(x, y):
        return 1 if (x, y) in rule_set else -1

    key = functools.cmp_to_key(cmp)

    def sort(xs: tuple[int, ...]) -> tuple[int, ...]:
        return tuple(sorted(xs, key=key, reverse=True))

    return sort


def middle(xs: tuple[int, ...]) -> int:
    return xs[len(xs) // 2]


def part1(data: tuple[Rules, Updates]) -> int:
    rules, updates = data
    sort = build_sort(rules)
    total = 0
    for update in updates:
        if update == sort(update):
            total += middle(update)
    return total


assert part1(test_data) == 143, "Part 1 test failed!"
ans1 = part1(data)
assert ans1 == 4957, "Part 1 failed!"

## Part 2


def part2(data: tuple[Rules, Updates]) -> int:
    rules, updates = data
    sort = build_sort(rules)
    total = 0
    for update in updates:
        if update != (sorted_update := sort(update)):
            total += middle(sorted_update)
    return total


assert part2(test_data) == 123, "Part 2 test failed!"
ans2 = part2(data)
assert ans2 == 6938, "Part 2 failed!"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
