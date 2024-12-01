# Advent of Code 2024 - Day 1

from collections import Counter

with open("../input/01.txt") as f:
    data = f.read()


def process(s: str) -> list[list[int], list[int]]:
    lines = s.splitlines()
    pairs = [tuple(map(int, line.split())) for line in lines]
    two_lists = list(zip(*pairs))
    return two_lists


def part1(s: str) -> tuple[list[int], list[int]]:
    two_lists = process(s)
    sorted_lists = map(sorted, two_lists)
    diffs = map(lambda x, y: abs(x-y), *sorted_lists)
    return sum(diffs)

test_data = """3   4
4   3
2   5
1   3
3   9
3   3"""


assert part1(test_data) == 11, "Failed the part 1 test"

ans1 = part1(data)

assert ans1 == 1222801, "Failed part1"


def part2(s: str) -> int:
    first_list, second_list = process(s)
    counts = Counter(second_list)
    return sum( x * counts.get(x, 0) for x in first_list )


assert part2(test_data) == 31, "Failed the part 2 test"

ans2 = part2(data)


if __name__ == "__main__":
    print(f"Answer1: {ans1}")
    print(f"Answer2: {ans2}")

