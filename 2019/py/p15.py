from utils import data19
import heapq
import library
import intcode
import heapq
import sys
from typing import NamedTuple, Optional, Mapping, Set, Dict
from dataclasses import dataclass, field

data = data19(15)
Infinity = float("inf")

tests = []


def debug(*args, **kwargs):
  return
  return print(*args, **kwargs)


def neighbors(loc):
  return [loc + 1, loc - 1, loc + 1j, loc - 1j]


def get_move(frm, to):
  """Given two positions generate the move."""
  moves = {1j: 1, -1j: 2, -1: 3, 1: 4}
  return moves[to - frm]


Cell = complex


def wrap(loc: Cell):
  return (loc.real, loc.imag)


def unwrap(a):
  return a[0] + 1j * a[1]


def gen_heuristic(b):

  def heuristic(a):
    return abs(a[0] - b[0]) + abs(a[1] - b[1])

  return heuristic


def wrapped_neighbors(x):
  return [wrap(y) for y in neighbors(unwrap(x))]


@dataclass
class World:
  comp: intcode.Computer
  loc: Cell = 0 + 0j
  oxygen: Optional[Cell] = None
  walls: Set[Cell] = field(default_factory=set)
  distance: Dict[Cell, int] = field(default_factory=dict)

  def __post_init__(self):
    self.distance[self.loc] = 0

  def shortest_path(self, start, new):

    def cost(cur, nxt):
      if unwrap(nxt) in self.walls:
        return float("inf")
      elif unwrap(nxt) in self.distance:
        return 0
      else:
        return 1_000_000

    _, came_from, cost_so_far = library.astar(
        wrap(start),
        lambda x: x == wrap(new),
        cost,
        wrapped_neighbors,
        gen_heuristic(wrap(new)),
    )
    path = library.reconstruct_path(came_from, wrap(start), wrap(new))
    debug(f"shortest from {start} to {new}: {path} with {self.walls}")
    return path

  def move_to(self, new):
    """Attempt to move to the location: new
    filling in our data along the way."""
    debug(f"Move_to {self.loc} to {new}")
    assert new not in self.walls, "asked to go to wall."
    if new == self.loc:
      return self

    path = self.shortest_path(self.loc, new)
    debug(f"path = {path}")
    assert unwrap(path[0]) == self.loc, "Location doesn't match start of path."
    for next in path[1:]:
      next = unwrap(next)
      move = get_move(self.loc, next)
      status = self.comp.run(move)[0]
      debug(f"moved move={move}, at {self.loc} got status={status}")
      if status == 0:
        # hit a wall...
        self.walls.add(next)
        if next != new:
          render(self, path)
          assert (
              next == new
          ), f"Hit wall too early. {self.loc} -> {next} -> {new}, {self.walls}, {path}"
      elif status == 1:
        if next not in self.distance:
          self.distance[next] = self.distance[self.loc] + 1
        self.loc = next
      elif status == 2:
        if next not in self.distance:
          self.distance[next] = self.distance[self.loc] + 1
        self.oxygen = next
        self.loc = next
    return self


def run(inp, cond=None):
  prog = intcode.getcodes(inp)
  comp = intcode.Computer(prog)

  world = World(comp)
  frontier = []
  for n in neighbors(world.loc):
    heapq.heappush(frontier, (world.distance[world.loc] + 1, wrap(n)))

  if cond is None:
    cond = lambda world: not world.oxygen
  while frontier and cond(world):
    d, new = heapq.heappop(frontier)
    new = unwrap(new)
    debug(f"new frontier point: {new} from {frontier}")
    if new not in world.walls and new not in world.distance:
      world = world.move_to(new)
      if world.loc == new:
        for x in neighbors(new):
          if x not in world.walls and x not in world.distance:
            heapq.heappush(frontier, (world.distance[world.loc] + 1, wrap(x)))

  return world


def render(world, path=[]):
  xmin = int(min(x.real for x in world.walls)) - 1
  xmax = int(max(x.real for x in world.walls)) + 1
  ymin = int(min(x.imag for x in world.walls)) - 1
  ymax = int(max(x.imag for x in world.walls)) + 1
  for y in range(ymin, ymax + 1):
    for x in range(xmin, xmax + 1):
      print(
          "#" if x + 1j * y in world.walls else
          ("o" if x + 1j * y == world.oxygen else
           ("x" if x + 1j * y == 0 else ("@" if x + 1j * y == world.loc else
                                         ("." if
                                          (x, y) in set(path) else " ")))),
          end="",
      )
    print()


def answer1(inp):
  world = run(inp)
  return world, world.distance[world.oxygen]


tests2 = []


def answer2(inp):
  world = run(inp, cond=lambda world: True)
  world.move_to(world.oxygen)

  max_dist = 1
  seen = set()
  frontier = []
  for n in neighbors(world.oxygen):
    if n in world.distance:
      heapq.heappush(frontier, [1, wrap(n)])
  seen.add(world.oxygen)

  while frontier:
    d, pt = heapq.heappop(frontier)
    pt = unwrap(pt)
    seen.add(pt)
    for n in neighbors(pt):
      if n not in seen and n in world.distance:
        heapq.heappush(frontier, [d + 1, wrap(n)])
        max_dist = max(max_dist, d + 1)

  return max_dist


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"

  world, ans1 = answer1(data)
  print("Answer1:", ans1)

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  ans2 = answer2(data)
  print("Answer2:", ans2)
