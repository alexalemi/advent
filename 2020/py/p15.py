import time
from collections import defaultdict, deque
from utils import data20
from tqdm import tqdm

data = data20(15)

tests = [("0,3,6", 436), ("1,3,2", 1), ("2,1,3", 10), ("1,2,3", 27),
         ("2,3,1", 78), ("3,2,1", 438), ("3,1,2", 1836)]


def answer1(inp, n=2020, debug=False):
  seen = {}
  prev = None
  turn = 1
  for i, num in enumerate(list(map(int, inp.split(",")))):
    seen[num] = turn
    prev = num
    turn += 1

  # with tqdm(total=n - turn) as pbar:
  while turn <= n:
    new = (turn - 1) - seen.get(prev, turn - 1)
    seen[prev] = turn - 1
    if debug:
      print(f"{turn}: {new}")
    # pbar.update(1)
    turn += 1
    prev = new

  return prev


tests2 = [("0,3,6", 175594), ("1,3,2", 2578), ("2,1,3", 3544142),
          ("1,2,3", 261214), ("2,3,1", 6895259), ("3,2,1", 18), ("3,1,2", 362)]


def answer2(inp, debug=False):
  return answer1(inp, 30_000_000, debug=debug)


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  start = time.time()
  ans1 = answer1(data)
  end = time.time()
  print("Answer1:", ans1, f" in {end - start:0.3e} secs")

  # for inp, ans in tests2:
  #   myans = answer2(inp)
  #   print(f"{myans} ?= {ans}", flush=True)
  #   assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  start = time.time()
  ans2 = answer2(data)
  end = time.time()
  print("Answer2:", ans2, f" in {end - start:0.3e} secs")
