import time
from utils import data20
from typing import NamedTuple


class Seat(NamedTuple):
  row: int
  column: int
  id: int


data = data20(5)

tests = [("BFFFBBFRRR", Seat(70, 7, 567)), ("FFFBBBFRRR", Seat(14, 7, 119)),
         ("BBFFBBFRLL", Seat(102, 4, 820))]


def make(line):
  row = int(line[:7].replace("F", "0").replace("B", "1"), 2)
  column = int(line[-3:].replace("L", "0").replace("R", "1"), 2)
  id = row * 8 + column
  return Seat(row, column, id)


def answer1(inp):
  return max((make(line) for line in inp.splitlines()), key=lambda x: x.id).id


tests2 = []


def answer2(inp):
  seats = [make(line) for line in inp.splitlines()]
  ids = set(x.id for x in seats)
  for pk in range(max(ids)):
    if pk not in ids:
      if pk + 1 in ids and pk - 1 in ids:
        return pk


if __name__ == "__main__":
  for inp, ans in tests:
    myans = make(inp)
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
