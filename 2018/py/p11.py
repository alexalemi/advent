"""Advent Day 11"""

import numpy as np
from numpy.lib.stride_tricks import as_strided
from jax import jit

serial = 8561

SIZE = 300


def make_grid(serial):
  ks, ls = np.mgrid[0:SIZE, 0:SIZE]
  xcoord = ks + 1
  ycoord = ls + 1
  rackid = xcoord + 10
  power = rackid * ycoord
  power = power + serial
  power = power * rackid
  hundreds = power // 100 % 10
  ans = hundreds - 5
  return ans


assert make_grid(8)[2, 4] == 4

# Fuel cell at  122,79, grid serial number 57: power level -5.
# Fuel cell at 217,196, grid serial number 39: power level  0.
# Fuel cell at 101,153, grid serial number 71: power level  4.

assert make_grid(57)[121, 78] == -5
assert make_grid(39)[216, 195] == 0
assert make_grid(71)[100, 152] == 4


def window(arr, width=3):
  return as_strided(arr, (arr.shape[0] - (width - 1), arr.shape[1] -
                          (width - 1)) + (width, width),
                    arr.strides + arr.strides)


def max_coord(arr, width=3):
  sums = window(arr, width).sum(-1).sum(-1)
  loc = np.unravel_index(sums.argmax(), sums.shape[::-1])
  return loc[0] + 1, loc[1] + 1, sums[loc]


assert max_coord(make_grid(18)) == (33, 45, 29)
assert max_coord(make_grid(42)) == (21, 61, 30)

grid = make_grid(serial)
ans1 = max_coord(grid)

print('Answer1:', ans1)


def finder(grid, stop=None):
  bestcoord = (None, None, -float('inf'))
  bestsize = None
  size = stop or grid.shape[0]

  for s in range(1, size):
    coord = max_coord(grid, s)
    if coord[2] > bestcoord[2]:
      bestcoord = coord
      bestsize = s

  return bestcoord[0], bestcoord[1], bestsize


assert finder(make_grid(18), 20) == (90, 269, 16)
assert finder(make_grid(42), 20) == (232, 251, 12)

ans2 = finder(make_grid(serial), 50)

print('Answer2:', ans2)
