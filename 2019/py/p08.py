import numpy as np
import matplotlib.pyplot as plt

from utils import data19

data = data19(8)

tests = []

shape = (6, 25)


def toimg(s, w, h):
  return np.array(list(map(int, list(s.strip())))).reshape((-1, w, h))


def answer1(inp):
  planes = toimg(data, *shape)
  pk = (planes == 0).sum(1).sum(1).argmin()
  return (planes[pk] == 1).sum() * (planes[pk] == 2).sum()


tests2 = []


def color(pix):
  loc = 0
  while pix[loc] == 2:
    loc += 1
  return pix[loc]


def answer2(inp):
  planes = toimg(data, *shape)
  img = np.zeros(shape)
  for i in range(shape[0]):
    for j in range(shape[1]):
      img[i, j] = color(planes[:, i, j])

  s = "\n\n" + "―" * shape[1] + "\n"
  for row in img:
    for col in row:
      if col == 1:
        s += "█"
      else:
        s += " "
    s += "\n"
  s = s + "―" * shape[1] + "\n"
  return s


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
