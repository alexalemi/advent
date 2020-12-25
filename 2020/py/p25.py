import time
from utils import data20

data = data20(25)

tests = [("5764801\n17807724\n", 14897079)]


def answer1(inp, subject=7, n=20201227):
  card, door = map(int, inp.strip().split())
  times = 0
  value = 1
  while value not in (card, door):
    value = (value * subject) % n
    times += 1

  subject = door if value == card else card

  value = 1
  for _ in range(times):
    value = (value * subject) % n
  return value


tests2 = []


def answer2(inp):
  return None


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  start = time.time()
  ans1 = answer1(data)
  end = time.time()
  print("Answer1:", ans1, f" in {end - start:0.3e} secs")
