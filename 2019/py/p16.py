from utils import data19
from typing import List
import itertools
from functools import lru_cache
import numpy as np

data = data19(16)

debug = lambda *args, **kwargs: None
cycle = itertools.cycle


def repeater(iter, n=1):
  for x in iter:
    for _ in range(n):
      yield x


drop1 = lambda iter: itertools.islice(iter, 1, None)


def splitnum(x):
  return list(map(int, str(x)))


def fft1(nums: List[int]) -> List[int]:
  base_pattern = [0, 1, 0, -1]
  output = []
  for i, x in enumerate(nums):
    tot = 0
    for val, mul in zip(nums, drop1(cycle(repeater(base_pattern, i + 1)))):
      # debug(f"{val} * {mul} + ", end="")
      tot += val * mul
    # debug(f" = {tot}")
    output.append(abs(tot) % 10)
  return output


def fft2(nums: List[int]) -> List[int]:
  base_pattern = [0, 1, 0, -1]
  output = []
  n = len(nums)
  for i, x in enumerate(nums):
    tot = 0
    for j in range((i + 1) * 1, n + 1, 4 * (i + 1)):
      for k in range(i + 1):
        pk = j + k - 1
        if pk < n:
          # debug(f"+{nums[j+k-1]}", end="")
          tot += nums[pk]
    for j in range((i + 1) * 3, n + 1, 4 * (i + 1)):
      for k in range(i + 1):
        pk = j + k - 1
        if pk < n:
          # debug(f"-{nums[j+k-1]}", end="")
          tot -= nums[pk]
        else:
          break
    # debug(f" = {tot}")
    output.append(abs(tot) % 10)
  return output


pattern = [0, 1, 0, -1]


def kfft1(nums: List[int], i: int) -> int:
  tot = sum(
      pattern[((j + 1) // (i + 1)) % 4] * nums[j] for j in range(len(nums)))
  return abs(tot) % 10


def kfft(nums: List[int], i: int, mul: int = 1, off: int = 1) -> int:
  return sum(pattern[((mul * j + off) // (i + 1)) % 4] * nums[j]
             for j in range(len(nums)))


def ones(x):
  return abs(x) % 10


@lru_cache
def kfft3(nums: List[int], i: int, mul: int = 1, off: int = 1) -> int:
  if len(nums) == 0:
    return 0
  if len(nums) == 1:
    return pattern[(off // (i + 1)) % 4] * nums[0]
  a = kfft3(nums[::2], i, mul * 2, off)
  b = kfft3(nums[1::2], i, mul * 2, off + mul)
  print(f"kfft3({nums[0]}:{len(nums)}, {i}, {mul}, {off}) = {a+b}")
  return a + b


def fft(nums: List[int]) -> List[int]:
  return tuple([ones(kfft(nums, i)) for i in range(len(nums))])


sn = splitnum

tests = [
    ("80871224585914546619083218645595", "24176176"),
    ("19617804207202209144916044189917", "73745418"),
    ("69317163492948606335995924319873", "52432133"),
]


def answer1(inp):
  x = tuple(map(int, inp.strip()))
  for _ in range(100):
    x = fft(x)
  return "".join(str(x) for x in x[:8])


tests2 = [
    ("03036732577212944063491565474664", "84462026"),
    ("02935109699940807407585447034323", "78725270"),
    ("03081770884921959731165446850517", "53553731"),
]


def answer2(inp):
  x = tuple(map(int, inp.strip()))
  x = x * 10_000
  offset = int("".join(str(x) for x in x[:7]))
  assert offset / len(x) > 0.5, "offset not more than halfway through."
  x = np.array(x[offset:])
  for _ in range(100):
    y = x[::-1].cumsum()[::-1]
    x = np.abs(y) % 10
  return "".join(str(x) for x in x[:8])


if __name__ == "__main__":
  foo = list(map(int, "12345678"))
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  ans1 = answer1(data)
  print("Answer1:", ans1)

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  ans2 = answer2(data)
  print("Answer2:", ans2)
