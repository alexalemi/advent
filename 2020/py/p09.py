import time
from utils import data20
import itertools

data = data20(9)

tests = [(("""35
20
15
25
47
40
62
55
65
95
102
117
150
182
127
219
299
277
309
576""", 5), 127)]


def presum(num, lst):
  for i, n in enumerate(lst):
    for j, m in enumerate(lst[i + 1:]):
      if n + m == num:
        return True
  return False


def answer1(inp, pre=25):
  nums = list(map(int, inp.splitlines()))
  for i, num in enumerate(nums[pre:]):
    if not presum(num, nums[i:pre + i]):
      return num


tests2 = [((tests[0][0][0], 127), 62)]


def answer2(inp, target=None):
  nums = list(map(int, inp.splitlines()))
  if target is None:
    target = answer1(inp)
  cumsums = list(itertools.accumulate(nums))
  for i, n in enumerate(cumsums):
    for j, m in enumerate(cumsums[i + 1:]):
      if m - n == target:
        low = min(nums[i + 1:i + j + 1])
        high = max(nums[i + 1:i + j + 1])
        return low + high


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(*inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  start = time.time()
  ans1 = answer1(data)
  end = time.time()
  print("Answer1:", ans1, f" in {end - start:0.3e} secs")

  for inp, ans in tests2:
    myans = answer2(*inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  start = time.time()
  ans2 = answer2(data)
  end = time.time()
  print("Answer2:", ans2, f" in {end - start:0.3e} secs")
