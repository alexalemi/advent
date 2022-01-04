import time
from utils import data18
from typing import NamedTuple, Tuple, Mapping, Set, Sequence

data = data18(15)

Loc = Tuple[int, int]

AttackPower = 3

class Character(NamedTuple):
    loc: Loc
    hp: int = 200

class State(NamedTuple):
    board: Set[Loc]
    goblins: Sequence[Character]
    elves: Sequence[Character]

def process(data):
    walls = set()
    goblins = []
    elves = []
    for row, line in enumerate(data):
        for col, c in enumerate(line):
            if c == '#':
                walls.add((row, col))
            elif c == 'G':
                goblins.append((row, col))
            elif c == 'E':
                elves.append((row, col))
    return walls, goblins, elves



tests = []


def answer1(inp):
  return None


tests2 = []


def answer2(inp):
  return None


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
