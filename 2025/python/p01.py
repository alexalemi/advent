import operator
from functools import partial
from typing import Callable

with open("../input/01.txt") as f:
    data_string = f.read()

test_string = """L68
L30
R48
L5
R60
L55
L1
L99
R14
L82"""


SIZE = 100
INIT = 50


def update(op: Callable[(int, int), int], amount: int, state: int) -> int:
    for x in range(1, amount + 1):
        yield (op(state, x)) % SIZE


left = partial(update, operator.sub)
right = partial(update, operator.add)


def process(inp: str):
    instructions = []
    for line in inp.splitlines():
        direction, amount_str = line[0], line[1:].strip()
        amount = int(amount_str)
        match direction:
            case "L":
                instructions.append(partial(left, amount))
            case "R":
                instructions.append(partial(right, amount))
            case _:
                raise NotImplemented(f"Don't understand {direction}!")
    return instructions


data = process(data_string)
test_data = process(test_string)


def last(it):
    tmp = None
    for x in it:
        tmp = x
    return tmp


def part1(data) -> int:
    state = INIT
    zeros = 0
    for inst in data:
        state = last(inst(state))
        if state == 0:
            zeros += 1
    return zeros


assert part1(test_data) == 3, f"Failed part1 test!"


def part2(data) -> int:
    state = INIT
    zeros = 0
    for inst in data:
        for x in inst(state):
            state = x
            if state == 0:
                zeros += 1
    return zeros


assert part2(test_data) == 6, f"Failed part2 test!"


if __name__ == "__main__":
    ans1 = part1(data)
    print(f"Answer 1: {ans1}")

    ans2 = part2(data)
    print(f"Answer 2: {ans2}")
