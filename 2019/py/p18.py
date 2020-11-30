# from utils import data19
import itertools
from copy import deepcopy
import functools
import dataclasses
from typing import Dict, Tuple, FrozenSet, Sequence
import string
import heapq

import library

# data = data19(18)
with open('../input/18.txt', 'r') as f:
  data = f.read()

Location = complex


@functools.total_ordering
@dataclasses.dataclass(eq=True, frozen=True)
class World:
  locs: Tuple[Location]
  moves: int
  walls: FrozenSet[Location]
  keys: Dict[Location, chr]
  doors: Dict[chr, Location]

  def __lt__(self, other):
    return self.moves < other.moves

  def __hash__(self):
    return hash((self.locs, self.walls,
                 tuple(sorted((n, x) for x, n in self.keys.items()))))


tests = [
    ("""#########
#b.A.@.a#
#########""", 8),
    ("""########################
#f.D.E.e.C.b.A.@.a.B.c.#
######################.#
#d.....................#
########################""", 86),
    ("""########################
#...............b.C.D.f#
#.######################
#.....@.a.B.c.d.A.e.F.g#
########################""", 132),
    ("""#################
#i.G..c...e..H.p#
########.########
#j.A..b...f..D.o#
########@########
#k.E..a...g..B.n#
########.########
#l.F..d...h..C.m#
#################""", 136),
    ("""########################
#@..............ac.GI.b#
###d#e#f################
###A#B#C################
###g#h#i################
########################""", 81),
]


def process(inp: str) -> Dict[Location, chr]:
  """Given the string representation, make into a dictionary."""
  vals = {}
  for y, line in enumerate(inp.splitlines()):
    for x, c in enumerate(line):
      vals[complex(x, y)] = c
  return vals


def make(inp: str) -> World:
  board = process(inp)
  locs = tuple(loc for loc, c in board.items() if c == '@')
  moves = 0
  walls = frozenset(loc for loc, c in board.items() if c == '#')
  keys = {
      let: c for let, c in board.items() if c in set(string.ascii_lowercase)
  }
  doors = {
      c: let for let, c in board.items() if c in set(string.ascii_uppercase)
  }
  return World(locs, moves, walls, keys, doors)


def render(world: World):
  board = {}
  for loc in world.walls:
    board[loc] = '#'
  for d, loc in world.doors.items():
    board[loc] = d
  for loc, k in world.keys.items():
    board[loc] = k
  for loc in world.locs:
    board[loc] = '@'
  bounds = library.get_bounds(board)
  library.render(bounds, board, default='.')
  print(f"Moves = {world.moves} keys={','.join(world.keys.values())}")


def tup_replace(tup, idx, val):
  lst = list(tup)
  lst[idx] = val
  return tuple(lst)


def neighbors(world: World) -> Sequence[World]:
  """Do a sort of flood fill to find the available keys."""

  def valid(x):
    return not (x in world.walls) and not (x in world.keys) and not (x in set(
        world.doors.values()))

  frontier = [
      (world.moves, i, library.wrap(x)) for i, x in enumerate(world.locs)
  ]
  seen = set()
  while frontier:
    d, i, x = heapq.heappop(frontier)
    x = library.unwrap(x)
    seen.add(x)
    for n in library.neighbors(x):
      if n in world.keys:
        seen.add(n)
        key = world.keys[n]
        yield dataclasses.replace(
            world,
            locs=tup_replace(world.locs, i, n),
            moves=d + 1,
            keys={loc: x for loc, x in world.keys.items() if loc != n},
            doors={
                d: loc for d, loc in world.doors.items() if d.lower() != key
            })
      elif valid(n) and n not in seen:
        heapq.heappush(frontier, (d + 1, i, library.wrap(n)))


def solve(world: World) -> int:
  """Solve a world."""
  counter = itertools.count()
  frontier = [(world.moves, -next(counter), world)]
  seen = set()
  while frontier:
    _, _, x = heapq.heappop(frontier)
    if hash(x) not in seen:
      # print(x.moves, ":", x.loc, ":", ','.join(x.keys.values()), "->", hash(x))
      seen.add(hash(x))
      if len(x.keys) == 0:
        return x.moves
      for n in neighbors(x):
        if hash(n) not in seen:
          heapq.heappush(frontier, (n.moves, -next(counter), n))


def answer1(inp):
  world = make(inp)
  return solve(world)


tests2 = [
    ("""#######
#a.#Cd#
##@#@##
#######
##@#@##
#cB#Ab#
#######""", 8),
    ("""###############
#d.ABC.#.....a#
######@#@######
###############
######@#@######
#b.....#.....c#
###############""", 24),
    ("""#############
#DcBa.#.GhKl#
#.###@#@#I###
#e#d#####j#k#
###C#@#@###J#
#fEbA.#.FgHi#
#############""", 32),
    ("""#############
#g#f.D#..h#l#
#F###e#E###.#
#dCba@#@BcIJ#
#############
#nK.L@#@G...#
#M###N#H###.#
#o#m..#i#jk.#
#############""", 72),
]


def replace_center(inp):
  vals = process(inp)
  locs = [loc for loc, c in vals.items() if c == '@']
  assert len(locs) == 1, "Wrong input."
  loc = locs[0]
  vals[loc] = '#'
  vals[loc + 1] = '#'
  vals[loc - 1] = '#'
  vals[loc + 1j] = '#'
  vals[loc - 1j] = '#'
  vals[loc + 1 + 1j] = '@'
  vals[loc + 1 - 1j] = '@'
  vals[loc - 1 + 1j] = '@'
  vals[loc - 1 - 1j] = '@'
  bounds = library.get_bounds(vals.keys())
  return '\n'.join(''.join(vals[x + y * 1j]
                           for x in range(bounds.xmin, bounds.xmax + 1))
                   for y in range(bounds.ymin, bounds.ymax + 1))


def answer2(inp):
  return answer1(inp)


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    print(f"Got {ans}!")
  print("Running part1...", flush=True)
  ans1 = answer1(data)
  print("Answer1:", ans1)

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"
    print(f"Got {ans}!")

  print("Running part2...", flush=True)
  ans2 = answer2(replace_center(data))
  print("Answer2:", ans2)
