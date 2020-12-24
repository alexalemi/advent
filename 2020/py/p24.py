import time
from utils import data20
from typing import NamedTuple, Set

data = data20(24)

tests = [("""sesenwnenenewseeswwswswwnenewsewsw
neeenesenwnwwswnenewnwwsewnenwseswesw
seswneswswsenwwnwse
nwnwneseeswswnenewneswwnewseswneseene
swweswneswnenwsewnwneneseenw
eesenwseswswnenwswnwnwsewwnwsene
sewnenenenesenwsewnenwwwse
wenwwweseeeweswwwnwwe
wsweesenenewnwwnwsenewsenwwsesesenwne
neeswseenwwswnwswswnw
nenwswwsewswnenenewsenwsenwnesesenew
enewnwewneswsewnwswenweswnenwsenwsw
sweneswneswneneenwnewenewwneswswnese
swwesenesewenwneswnwwneseswwne
enesenwswwswneneswsenwnewswseenwsese
wnwnesenesenenwwnenwsewesewsesesew
nenewswnwewswnenesenwnesewesw
eneswnwswnwsenenwnwnwwseeswneewsenese
neswnwewnwnwseenwseesewsenwsweewe
wseweeenwnesenwwwswnew""", 10)]

AXIAL = {
    'ne': (1, -1),
    'nw': (0, -1),
    'e': (1, 0),
    'w': (-1, 0),
    'sw': (-1, 1),
    'se': (0, 1),
}


class Coord(NamedTuple):
  x: int
  y: int

  @property
  def z(self):
    return 0 - x - y

  def __add__(self, y):
    return Coord(self.x + y.x, self.y + y.y)

  @property
  def neighbors(self):
    for d in AXIAL.values():
      yield self + Coord(*d)


def split(inp: str) -> str:
  # e, se, sw, w, nw, and ne
  while inp:
    for d in ('se', 'sw', 'nw', 'ne', 'e', 'w'):
      if inp.startswith(d):
        yield d
        inp = inp[len(d):]


def process(inp: str) -> Set[Coord]:
  board = set()
  for line in inp.strip().splitlines():
    loc = Coord(0, 0)
    for direction in split(line):
      loc = loc + Coord(*AXIAL[direction])
    if loc in board:
      board.remove(loc)
    else:
      board.add(loc)
  return board


def answer1(inp):
  board = process(inp)
  return len(board)


tests2 = [(tests[0][0], 2208)]


def evolve(board: Set[Coord]) -> Set[Coord]:
  result = set()
  to_consider = set(neighbor for site in board for neighbor in site.neighbors)
  for site in to_consider:
    neighbors = sum(1 for x in site.neighbors if x in board)
    if site in board and neighbors in (1, 2):
      result.add(site)
    elif site not in board and neighbors == 2:
      result.add(site)
  return result


def answer2(inp):
  board = process(inp)
  for _ in range(100):
    board = evolve(board)
  return len(board)


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
