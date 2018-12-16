"""Advent of Code Day 12"""

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


def read_data(data: str) -> Tuple[State, Rules]:
    lines = data.splitlines()
    
    initial_state = re.match('initial state: ([#\.]+)', lines[0]).groups()[0]

    rules: Rules = {}
    for line in lines[2:]:
        state, to = re.match('([#\.]+) => ([#\.])', line).groups()
        rules[state] = to


    return initial_state, 

read_data(data)
