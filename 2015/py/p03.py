from utils import data15
from collections import defaultdict

data = data15(3)

tests = [
    (">", 2),
    ("^>v<", 4),
    ("^v^v^v^v^v", 2),
]


def convert(c):
  if c == ">":
    return 1 + 0j
  elif c == "<":
    return -1 + 0j
  elif c == "^":
    return 0 + 1j
  elif c == "v":
    return 0 - 1j


def answer1(inp):
  visited = defaultdict(int)
  loc = 0 + 0j
  visited[loc] += 1
  for diff in map(convert, inp):
    loc += diff
    visited[loc] += 1
  return len(visited)


tests2 = [
    ("^v", 3),
    ("^>v<", 3),
    ("^v^v^v^v^v", 11),
]


def answer2(inp):
  visited = defaultdict(int)
  loc = 0 + 0j
  robo_loc = 0 + 0j
  visited[loc] += 1
  visited[robo_loc] += 1
  for i, diff in enumerate(map(convert, inp)):
    if i % 2 == 0:
      loc += diff
      visited[loc] += 1
    else:
      robo_loc += diff
      visited[robo_loc] += 1
  return len(visited)


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
