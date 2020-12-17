import dataclasses
import itertools
import time
import tqdm
from typing import Dict, Tuple, NamedTuple, Sequence, Set
from utils import data20


class Coord(NamedTuple):
  x: int
  y: int
  z: int
  w: int = 0


class Board(NamedTuple):
  state: Set[Coord]
  time: int


def neighbors(coord: Coord, d: int = 3) -> Sequence[Coord]:
  for diff in itertools.product([-1, 0, 1], repeat=d):
    d = Coord(*diff)
    if d != Coord(0, 0, 0):
      yield coord._replace(
          x=coord.x + d.x, y=coord.y + d.y, z=coord.z + d.z, w=coord.w + d.w)


class Bounds(NamedTuple):
  xmin: int
  ymin: int
  zmin: int
  wmin: int
  xmax: int
  ymax: int
  zmax: int
  wmax: int


def get_bounds(board: Board):
  places = list(board.state)
  xmin = min(places, key=lambda p: p.x).x
  ymin = min(places, key=lambda p: p.y).y
  zmin = min(places, key=lambda p: p.z).z
  wmin = min(places, key=lambda p: p.w).w
  xmax = max(places, key=lambda p: p.x).x
  ymax = max(places, key=lambda p: p.y).y
  zmax = max(places, key=lambda p: p.z).z
  wmax = max(places, key=lambda p: p.w).w
  return Bounds(xmin, ymin, zmin, wmin, xmax, ymax, zmax, wmax)


def step(board: Board, d: int = 3) -> Board:
  newstate = set()
  consider = board.state.union(*[set(neighbors(p, d)) for p in board.state])
  for coord in consider:
    n = sum(1 if n in board.state else 0 for n in neighbors(coord, d=d))
    if coord in board.state and n in (2, 3):
      newstate.add(coord)
    elif (not coord in board.state) and n == 3:
      newstate.add(coord)
  return board._replace(state=newstate, time=board.time + 1)


def process(inp):
  state = set()
  for y, line in enumerate(inp.strip().splitlines()):
    for x, c in enumerate(line):
      if c == '#':
        state.add(Coord(x, y, 0))
  return Board(state=state, time=0)


data = data20(17)

tests = [(""".#.
..#
###""", 112)]


def answer1(inp):
  board = process(inp)
  for _ in range(6):
    board = step(board)

  return len(board.state)


tests2 = [(tests[0][0], 848)]


def answer2(inp):
  board = process(inp)
  for _ in tqdm.trange(6):
    board = step(board, 4)

  return len(board.state)


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  start = time.time()
  ans1 = answer1(data)
  end = time.time()
  print("Answer1:", ans1, f" in {end - start:0.3e} secs")

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  start = time.time()
  ans2 = answer2(data)
  end = time.time()
  print("Answer2:", ans2, f" in {end - start:0.3e} secs")
