# from utils import data19
import itertools
from copy import deepcopy
import functools
import dataclasses
from typing import Dict, List, FrozenSet, Sequence
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
  loc: Location
  moves: int
  walls: FrozenSet[Location]
  keys: Dict[Location, chr]
  doors: Dict[chr, Location]

  def __lt__(self, other):
    return self.moves < other.moves

  def __hash__(self):
    return hash((self.loc, self.moves, self.walls, tuple(self.keys.items()),
                 tuple(self.doors.items())))


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
  loc = list(board.keys())[list(board.values()).index('@')]
  moves = 0
  walls = frozenset(loc for loc, c in board.items() if c == '#')
  keys = {
      let: c for let, c in board.items() if c in set(string.ascii_lowercase)
  }
  doors = {
      c: let for let, c in board.items() if c in set(string.ascii_uppercase)
  }
  return World(loc, moves, walls, keys, doors)


def render(world: World):
  board = {}
  for loc in world.walls:
    board[loc] = '#'
  for d, loc in world.doors.items():
    board[loc] = d
  for loc, k in world.keys.items():
    board[loc] = k
  board[world.loc] = '@'
  bounds = library.get_bounds(board)
  library.render(bounds, board, default='.')
  print(f"Moves = {world.moves} keys={','.join(world.keys.values())}")


def neighbors(world: World) -> Sequence[World]:
  """Do a sort of flood fill to find the available keys."""

  def valid(x):
    return not (x in world.walls) and not (x in world.keys) and not (x in set(
        world.doors.values()))

  frontier = [(world.moves, library.wrap(world.loc))]
  seen = set()
  while frontier:
    d, x = heapq.heappop(frontier)
    x = library.unwrap(x)
    seen.add(x)
    for n in library.neighbors(x):
      if n in world.keys:
        seen.add(n)
        key = world.keys[n]
        yield dataclasses.replace(
            world,
            loc=n,
            moves=d + 1,
            keys={loc: x for loc, x in world.keys.items() if loc != n},
            doors={
                d: loc for d, loc in world.doors.items() if d.lower() != key
            })
      elif valid(n) and n not in seen:
        heapq.heappush(frontier, (d + 1, library.wrap(n)))


def solve(world: World) -> int:
  """Solve a world."""
  counter = itertools.count()
  frontier = [(world.moves, -next(counter), world)]
  seen = set()
  while frontier:
    _, _, x = heapq.heappop(frontier)
    seen.add(x)
    if len(x.keys) == 0:
      return x.moves
    for n in neighbors(x):
      if n not in seen:
        heapq.heappush(frontier, (n.moves, -next(counter), n))


def heuristic(world):
  """Try to design a greedy heuristic."""
  if world.keys:
    return max(library.distance(world.loc, loc) for loc in world.keys)
    # return max(n.moves for n in neighbors(world)) - world.moves 
  return 0

def answer1(inp):
  world = make(inp)
  # return solve(world)
  path = library.astar(
      start=world,
      goal=lambda world: len(world.keys) == 0,
      cost=lambda w1, w2: w2.moves - w1.moves,
      neighbors=neighbors,
      heuristic=heuristic)
  return path[-1].moves


tests2 = []

# def distance(world, loc):
#   def cost(x, y):
#     return 1
#   def neighbors(x):
#     x = library.unwrap(x)
#     for n in library.neighbors(x):
#       if n not in world.walls and n not in set(world.doors.values()):
#         yield library.wrap(n)
#   final, came_from, cost_so_far = library.astar(
#       library.wrap(world.loc),
#       goal = lambda x: x == library.wrap(loc),
#       cost = cost,
#       neighbors = neighbors,
#       heuristic = lambda x: library.distance(library.unwrap(x), loc))
#   return cost_so_far[final] if library.unwrap(final) == loc else float('inf')


def answer2(inp):
  return None


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

  ans2 = answer2(data)
  print("Answer2:", ans2)

  world = make(data)
