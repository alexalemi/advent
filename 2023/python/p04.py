from utils import data23
import re

data = data23(4)

test_data = """Card 1: 41 48 83 86 17 | 83 86  6 31 17  9 48 53
Card 2: 13 32 20 16 61 | 61 30 68 82 17 32 24 19
Card 3:  1 21 53 59 44 | 69 82 63 72 16 21 14  1
Card 4: 41 92 73 84 69 | 59 84 76 51 58  5 54 83
Card 5: 87 83 26 28 32 | 88 30 70 12 93 22 82 36
Card 6: 31 18 13 56 72 | 74 77 10 23 35 67 36 11"""


def process(s: str) -> dict[int, tuple[frozenset[int], frozenset[int]]]:
    result = {}
    for line in s.splitlines():
        if match := re.match(r"Card\s+(\d+): ([ \d]+)\|([ \d]+)", line):
            groups = match.groups()
            card_id = int(groups[0])
            target = frozenset(map(int, groups[1].split()))
            card = frozenset(map(int, groups[2].split()))
            result[card_id] = (target, card)
    return result


def matches(target: frozenset[int], card: frozenset[int]) -> int:
    return len(target.intersection(card))


def points(x: int) -> int:
    return 2 ** (x - 1) if x > 0 else 0


def part1(data: str) -> int:
    cards = process(data)
    the_matches = (matches(*v) for v in cards.values())
    return sum(points(m) for m in the_matches)


assert part1(test_data) == 13

ans1 = part1(data)

assert ans1 == 22193

# Part 2
# Now we need to treat the cards differently, each card
# tells us how many copies of the subsequent cards we win.


def part2(data: str) -> int:
    cards = process(data)
    the_matches = {i: matches(*v) for i, v in cards.items()}
    copies = {i: 1 for i in the_matches}
    for i, m in the_matches.items():
        for j in range(i + 1, i + 1 + m):
            copies[j] += copies[i]
    return sum(copies.values())


assert part2(test_data) == 30

ans2 = part2(data)

assert ans2 == 5625994


if __name__ == "__main__":
    print("Answer1:", ans1)
    print("Answer2:", ans2)
