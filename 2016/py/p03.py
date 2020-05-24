from utils import data16
import itertools
import numpy as np

# data = data16(3)

with open("../input/03.txt") as f:
    data = f.read()

def valid_triangle(a, b, c):
    return ((a+b) > c) and ((b+c) > a) and ((a+c)>b)

tests = [("5 10 25", 0)]

def answer1(inp):
    return sum(1 for line in inp.splitlines() if valid_triangle(*list(map(int, line.split()))))

tests2 = []

def answer2(inp):
    arr = np.array([list(map(int,x.split())) for x in inp.splitlines()])
    arr = arr.T.reshape((3, -1, 3)).reshape((-1, 3))
    return sum(1 for x in arr if valid_triangle(*x))

if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
