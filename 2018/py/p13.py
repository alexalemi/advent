"""Advent of Code Day 13."""

from utils import data18
from typing import Dict, Tuple
from dataclasses import dataclass, replace
import numpy as np

data = data18(13)

example = """
/->-\        
|   |  /----\
| /-+--+-\  |
| | |  | v  |
\-+-/  \-+--/
  \------/   
"""

CARTS = {"^", "v", ">", "<"}

TURN = ("left", "straight", "right")


def pack(x, y):
    return complex(x, y)


def unpack(z):
    return (int(z.real), int(z.imag))


def add(pos, num):
    return unpack(pack(*pos) + num)


@dataclass
class State:
    board: Dict[Tuple[int, int], chr]
    carts: Dict[Tuple[int, int], Tuple[str, int]]
    time: int


def make_state(inp: str) -> State:
    board = {}
    carts = {}
    for y, line in enumerate(inp.splitlines()):
        for x, char in enumerate(line):
            if char in CARTS:
                carts[(x, y)] = (char, 0)
                if char in (">", "<"):
                    board[(x, y)] = "-"
                else:
                    board[(x, y)] = "|"
            elif char != " ":
                board[(x, y)] = char
    return State(board, carts, 0)


MOTIONS = {
    "<": -1,
    ">": 1,
    "^": -1j,
    "v": 1j,
}


TURNS = {  # \
    "\\": {">": "v", "<": "^", "v": ">", "^": "<"},
    "/": {
        ">": "^",
        "<": "v",
        "v": "<",
        "^": ">",
    },
    "left": {
        ">": "^",
        "<": "v",
        "v": ">",
        "^": "<",
    },
    "right": {
        ">": "v",
        "<": "^",
        "v": "<",
        "^": ">",
    },
}

JUNCTION = "+"


def render(state: State):
    maxx = max(x[0] for x in state.board)
    maxy = max(x[1] for x in state.board)
    s = ""
    for y in range(maxy + 1):
        for x in range(maxx + 1):
            if (x, y) in state.carts:
                s += state.carts[(x, y)][0]
            else:
                s += state.board.get((x, y), " ")
        s += "\n"
    print(s)


def update(state: State) -> State:
    time = state.time + 1
    new_carts = {}
    for pos, (cart, n) in sorted(state.carts.items(), key=lambda x: (x[0][1], x[0][0])):
        # handle the carts in order (x,y)
        old_position = pos
        new_position = add(old_position, MOTIONS[cart])
        assert new_position in state.board, (
            f"New position missing from board @ {state.time}"
        )

        if new_position in new_carts:
            # Collision
            return new_position, state
        elif state.board[new_position] in TURNS:
            new_carts[new_position] = (TURNS[state.board[new_position]][cart], n)
            continue
        elif state.board[new_position] == JUNCTION:
            # at an intersection.
            if n % 3 == 0:  # left
                new_carts[new_position] = (TURNS["left"][cart], n + 1)
                continue
            elif n % 3 == 1:  # straight
                new_carts[new_position] = (cart, n + 1)
                continue
            elif n % 3 == 2:  # right
                new_carts[new_position] = (TURNS["right"][cart], n + 1)
                continue
            else:
                raise ValueError
        else:
            new_carts[new_position] = (cart, n)
            continue
    assert len(new_carts) == len(state.carts)
    return None, replace(state, time=time, carts=new_carts)


def answer1(inp):
    state = make_state(inp)
    while True:
        collision, state = update(state)
        if collision is not None:
            return collision


tests = [
    (
        r"""/->-\        
|   |  /----\
| /-+--+-\  |
| | |  | v  |
\-+-/  \-+--/
  \------/   """,
        (7, 3),
    )
]


if __name__ == "__main__":
    for inp, exp in tests:
        out = answer1(inp)
        assert out == exp, f"Failed test {out} != {exp}"
    print("Answer1:", answer1(data))
