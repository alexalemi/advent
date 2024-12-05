# Advent of Code 2024 - Day 5

from collections import defaultdict
import itertools
import parser
import functools

type Rule = tuple[int, int]
type Rules = tuple[Rule]
type Update = tuple[int]
type Updates = tuple[Update]
type Children = dict[int, set[int]]

parse = functools.partial(parser.parse, parser=parser.digits)

def process(s: str) -> tuple[Rules, Updates]:
    rules, sections = s.split('\n\n')
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

def satisfies(rule: Rule, update: Update) -> bool:
    """Tests whether the update satisfies the rule."""

def build_valid(update: Update):
    indices = {x: i for i, x in enumerate(update)}
    n = len(update)

    def valid(rule: Rule) -> bool:
        parent, child = rule
        return indices.get(parent, -1) < indices.get(child, n)

    return valid


def is_valid(update: Update, rules: Rules) -> bool:
    valid = build_valid(update)
    return all(valid(rule) for rule in rules)


def part1(data: tuple[Rules, Updates]) -> int:
    rules, updates = data
    total = 0
    for update in updates:
        if is_valid(update, rules):
            total += update[len(update)//2]
    return total


assert part1(test_data) == 143, "Part 1 test failed!"
ans1 = part1(data)
assert ans1 == 4957, "Part 1 failed!"

## Part 2


def part2(data: tuple[Rules, Updates]) -> int:
    rules, updates = data
    rules = set(rules)
    total = 0

    cmp = lambda x, y: 1 if (x, y) in rules else -1

    for update in updates:
        if not is_valid(update, rules):
            update = list(update)
            update.sort(key=functools.cmp_to_key(cmp))
            total += update[len(update)//2]
    return total

assert part2(test_data) == 123, "Part 2 test failed!"
ans2 = part2(data)
assert ans2 == 6938, "Part 2 failed!"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
