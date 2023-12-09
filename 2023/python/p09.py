# Advent of Code 2023 - Day 9

from collections.abc import Iterator, Generator, Callable
from typing import Optional

with open("../input/09.txt") as f:
    data_string = f.read()

test_string = """0 3 6 9 12 15
1 3 6 10 15 21
10 13 16 21 30 45"""


def process(s: str) -> list[tuple[int, ...]]:
    return [tuple(map(int, line.split())) for line in s.splitlines()]


test_data = process(test_string)
data = process(data_string)


def diff(s: Iterator[int]) -> Generator[int, None, None]:
    prev = next(s)
    for x in s:
        yield x - prev
        prev = x


def differences(s: tuple[int, ...]) -> tuple[int, ...]:
    return tuple(diff(iter(s)))


def accumulator(x: int) -> Generator[int, int | None, None]:
    x = x
    while True:
        x = x + (yield x)


def predict(seq: tuple[int, ...]) -> int:
    diffs = seq
    acc = accumulator(diffs[-1])
    acc.send(None)
    diffs = differences(seq)
    accs = [acc]
    while any(diffs):
        acc = accumulator(diffs[-1])
        acc.send(None)
        accs.append(acc)
        diffs = differences(diffs)

    prediction = 0
    for acc in reversed(accs):
        prediction = acc.send(prediction)
    return prediction


def part1(data: list[tuple[int, ...]]) -> int:
    return sum(map(predict, data))


assert part1(test_data) == 114, "Failed part1 test"
ans1 = part1(data)
assert ans1 == 1938800261, "Failed ans1"

## Part 2


def retrodict(seq: tuple[int, ...]) -> int:
    diffs = seq
    acc = accumulator(diffs[0])
    acc.send(None)
    diffs = differences(seq)
    accs = [acc]
    while any(diffs):
        acc = accumulator(diffs[0])
        acc.send(None)
        accs.append(acc)
        diffs = differences(diffs)

    prediction = 0
    for acc in reversed(accs):
        prediction = acc.send(-prediction)
    return prediction


def part2(data: list[tuple[int, ...]]) -> int:
    return sum(map(retrodict, data))


assert part2(test_data) == 2, "Failed part2 test"
ans2 = part2(data)
assert ans2 == 1112, "Failed ans2"

if __name__ == "__main__":
    print("Answer 1:", ans1)
    print("Answer 2:", ans2)
