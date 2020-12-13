from collections import namedtuple
import time
import math
from utils import data20

data = data20(13)

tests = [("""939
7,13,x,x,59,x,31,19""", 295)]


def answer1(inp):
  start, buses = inp.strip().split("\n")
  start = int(start)
  buses = [int(x) for x in buses.split(",") if x != 'x']
  best = min((x - (start % x), x) for x in buses)
  return math.prod(best)


tests2 = [
    (tests[0][0], 1068781),
    ("17,x,13,19", 3417),
    ("67,7,59,61", 754018),
    ("67,x,7,59,61", 779210),
    ("67,7,x,59,61", 1261476),
    ("1789,37,47,1889", 1202161486),
]


def sieve(constraints):
  constraints = list(sorted(constraints, key=lambda x: x[1]))
  a, n = constraints.pop()
  cand = a
  inc = n
  while constraints:
    a, n = constraints.pop()
    while cand % n != a:
      cand += inc
    inc *= n
  return cand


Output = namedtuple('Output', 'coefficients gcd quotients')


def extended_gcd(a, b):
  (old_r, r) = (a, b)
  (old_s, s) = (1, 0)
  (old_t, t) = (0, 1)

  while r != 0:
    quotient = old_r // r
    (old_r, r) = (r, old_r - quotient * r)
    (old_s, s) = (s, old_s - quotient * s)
    (old_t, t) = (t, old_t - quotient * t)

  return Output((old_s, old_t), old_r, (t, s))


def solve(constraints):
  a, n = constraints.pop()
  while constraints:
    b, m = constraints.pop()
    r, s = extended_gcd(n, m).coefficients
    a, n = (r * b * n + s * a * m) % (n * m), n * m
  return a


def answer2(inp):
  constraints = [(int(x) - i, int(x))
                 for i, x in enumerate(inp.strip().splitlines()[-1].split(','))
                 if x != 'x']
  return solve(constraints)


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
