"""Advent of Code Day 13."""

from typing import Dict, Tuple
from dataclasses import dataclass
import numpy as np

example = """
/->-\        
|   |  /----\
| /-+--+-\  |
| | |  | v  |
\-+-/  \-+--/
  \------/   
"""

CARTS = {'^', 'v', '>', '<'}

TURN = ('left', 'straight', 'right')


@dataclass
class State:
  board: np.ndarray
  carts: Dict[Tuple[int, int], Tuple[str, int]]
  time: int


def make_state(string: str) -> State:
  board = np.array(list(map(list, string.strip('\n').splitlines())))
  carts = {}
  for row, line in enumerate(board):
    for col, char in enumerate(line):
      if char in CARTS:
        carts[(row, col)] = (char, 0)
        if char in ('>', '<'):
          board[row, col] = '-'
        else:
          board[row, col] = '|'
  return State(board, carts, 0)
