import time
import itertools
import numpy as np
from utils import data20
from collections import defaultdict, Counter

data = data20(10)

tests = [("""16
10
15
5
1
11
7
19
6
12
4""", 7 * 5),
         ("""28
33
18
42
31
14
46
20
48
47
24
23
49
45
19
38
39
11
1
32
25
35
8
17
7
9
4
2
34
10
3""", 22 * 10)]


def is_valid(lst, left, right):
  prev = left[-1]
  lst = list(lst)
  lst.append(right[0])
  for i, x in enumerate(lst[:-1]):
    if not ((1 <= (x - prev) <= 3) and (1 <= (lst[i + 1] - x) <= 3)):
      return False
    prev = x
  return True


def answer1(inp):
  nums = list(sorted(map(int, inp.splitlines())))
  arr = np.array([0] + nums + [nums[-1] + 3])
  foo = Counter(np.diff(arr))
  return foo[1] * foo[3]


tests2 = [(tests[0][0], 8), (tests[1][0], 19208)]


def answer2(inp):
  nums = tuple(sorted(map(int, inp.splitlines())))
  nums = (0,) + nums + (nums[-1] + 3,)
  sols = defaultdict(int)
  sols[0] = 1
  for num in nums[1:]:
    sols[num] = sols[num - 1] + sols[num - 2] + sols[num - 3]
  return sols[nums[-1]]


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
    print("done", flush=True)

  start = time.time()
  ans2 = answer2(data)
  end = time.time()
  print("Answer2:", ans2, f" in {end - start:0.3e} secs")
