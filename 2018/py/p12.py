"""Advent of Code Day 12"""

import itertools
import re
import numpy as np
from typing import Dict, Tuple

data = """initial state: #..#.#..##......###...###

...## => #
..#.. => #
.#... => #
.#.#. => #
.#.## => #
.##.. => #
.#### => #
#.#.# => #
#.### => #
##.#. => #
##.## => #
###.. => #
###.# => #
####. => #"""

State = str
Rules = Dict[State, chr]


def nwise(iterable, n):
    iters = itertools.tee(iterable, n)
    for i in range(1, n):
        for iter in iters[i:]:
            next(iter)
    return zip(*iters)


def consume(state, rules):
    rawnewstate = "".join(
        [rules.get("".join(s), ".") for s in nwise("...." + state + "....", 5)]
    )
    leftstrip = rawnewstate.lstrip(".")
    return leftstrip.rstrip("."), (len(rawnewstate) - len(leftstrip) - 2)


def consumen(state, rules, n):
    total = 0
    for i in range(n):
        state, dtotal = consume(state, rules)
        # print(i, total, '\t', state)
        total += dtotal
    return state, total


def score(state, offset):
    return sum([i + offset for i, s in enumerate(state) if s == "#"])


def read_data(data: str) -> Tuple[State, Rules]:
    lines = data.splitlines()

    initial_state = re.match("initial state: ([#\.]+)", lines[0]).groups()[0]

    rules: Rules = {}
    for line in lines[2:]:
        state, to = re.match("([#\.]+) => ([#\.])", line).groups()
        rules[state] = to

    return initial_state, rules


test_initial_state, test_rules = read_data(data)
assert score(*consumen(test_initial_state, test_rules, 20)) == 325

with open("../input/12.txt") as f:
    data = f.read()

initial_state, rules = read_data(data)
print("Answer1:", score(*consumen(initial_state, rules, 20)))


def findfast(state, rules, final):
    seen = {}
    total = 0
    time = 0
    while True:
        state, dtotal = consume(state, rules)
        total += dtotal
        time += 1

        if state in seen:
            break
        seen[state] = (time, total)

    starttime, starttotal = seen[state]
    delta = total - starttotal
    return score(state, (final - starttime) * delta + starttotal)


print("Answer2:", findfast(initial_state, rules, 50_000_000_000))
