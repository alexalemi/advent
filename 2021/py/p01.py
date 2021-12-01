from utils import data21
from utils import threewise
import time

# data = open("../input/01.txt").read() # data21(1)
data = data21(1)

tests = [("""199
    200
    208
    210
    200
    207
    240
    269
    260
    263""", 7)]


def increased(vals):
    prev = 100000
    count = 0
    for x in vals:
        if x >= prev:
            count += 1
        prev = x
    return count

def answer1(inp):
    return increased(map(int, inp.splitlines()))


tests2 = [(tests[0][0], 5)]


def threeway(vals):
    prev = 1e120
    count = 0
    for guys in threewise(vals):
        tot = sum(guys)
        if tot > prev:
            count += 1
        prev = tot
    return count


def answer2(inp):
    return threeway(map(int, inp.splitlines()))


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
