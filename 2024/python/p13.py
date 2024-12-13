# Advent of Code 2024 - Day 13

from functools import partial
import parser

with open("../input/13.txt") as f:
    raw_data = f.read()

raw_test_data = """Button A: X+94, Y+34
Button B: X+22, Y+67
Prize: X=8400, Y=5400

Button A: X+26, Y+66
Button B: X+67, Y+21
Prize: X=12748, Y=12176

Button A: X+17, Y+86
Button B: X+84, Y+37
Prize: X=7870, Y=6450

Button A: X+69, Y+23
Button B: X+27, Y+71
Prize: X=18641, Y=10279"""

parse = partial(parser.parse, parser=parser.digits)


def process(s: str):
    parts = s.split("\n\n")
    return tuple(map(parse, parts))


data = process(raw_data)
test_data = process(raw_test_data)


def solve_problem(problem):
    ((Ax, Ay), (Bx, By), (x, y)) = problem

    btop = y * Ax - x * Ay
    bbot = By * Ax - Bx * Ay

    atop = y * Bx - x * By
    abot = Ay * Bx - Ax * By

    a, arem = divmod(atop, abot)
    b, brem = divmod(btop, bbot)
    if arem == 0 and brem == 0:
        return 3 * a + b
    return 0


def part1(data) -> int:
    return sum(map(solve_problem, data))


assert part1(test_data) == 480, "Failed part 1 test"
ans1 = part1(data)
assert ans1 == 32041, "Failed part 1"

## Part 2

BIG = 10_000_000_000_000


def embiggen(example):
    ((Ax, Ay), (Bx, By), (x, y)) = example
    return ((Ax, Ay), (Bx, By), (BIG + x, BIG + y))


def part2(data) -> int:
    data = map(embiggen, data)
    return sum(map(solve_problem, data))


ans2 = part2(data)
assert ans2 == 95843948914827, "Failed part 2"

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
