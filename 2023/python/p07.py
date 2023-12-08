from collections import Counter

with open("../input/07.txt") as f:
    data_string = f.read()

test_string = """32T3K 765
T55J5 684
KK677 28
KTJJT 220
QQQJA 483"""

type Hand = tuple[int, ...]
type Bid = int

card_to_num = {c: i + 2 for i, c in enumerate("23456789TJQKA")}


def process_line(s: str) -> tuple[Hand, Bid]:
    hand_str, bid_str = s.split()
    return tuple(map(lambda x: card_to_num[x], hand_str)), int(bid_str)


def process(s: str) -> list[tuple[Hand, Bid]]:
    return list(map(process_line, s.splitlines()))


data = process(data_string)
test_data = process(test_string)

hand_types = {
    (5,): 700,  # five-of-a-kind
    (1, 4): 600,  # four-of-a-kind
    (2, 3): 500,  # full-house
    (1, 1, 3): 400,  # three-of-a-kind
    (1, 2, 2): 300,  # two-pair
    (1, 1, 1, 2): 200,  # one-pair
    (1, 1, 1, 1, 1): 100,  # high-card
    (): 700,  # all-joker-five-of-a-kind
}


def hand_type(hand: Hand) -> int:
    sketch = tuple(sorted(Counter(hand).values()))
    return hand_types[sketch]


def part1(data: list[tuple[Hand, Bid]]) -> int:
    hands_in_order = sorted((hand_type(hand), hand, bid) for (hand, bid) in data)
    return sum(
        (rankm1 + 1) * bid for (rankm1, (_, _, bid)) in enumerate(hands_in_order)
    )


assert part1(test_data) == 6440, "Failed part 1 test!"
ans1 = part1(data)
assert ans1 == 250232501, "Failed part 1!"

## Part 2


def set_joker(hand: Hand) -> Hand:
    return tuple(0 if h == card_to_num["J"] else h for h in hand)


def replace_jokers(hand: Hand) -> Hand:
    no_jokers = filter(lambda x: x > 0, hand)
    most_common_card = (Counter(no_jokers).most_common(1) or [(0, 0)])[0][0]
    return tuple(most_common_card if h == 0 else h for h in hand)


def part2(data: list[tuple[Hand, Bid]]) -> int:
    jokerfied = ((set_joker(hand), bid) for (hand, bid) in data)
    hands_in_order = sorted(
        (hand_type(replace_jokers(hand)), hand, bid) for (hand, bid) in jokerfied
    )
    return sum(
        (rankm1 + 1) * bid for (rankm1, (_, _, bid)) in enumerate(hands_in_order)
    )


assert part2(test_data) == 5905, "Failed part 2 test!"
ans2 = part2(data)
assert ans2 == 249138943, "Failed part 2!"

if __name__ == "__main__":
    print("Answer1: ", ans1)
    print("Answer2: ", ans2)
