from utils import data15
import re

data = data15(5)

tests = [('ugknbfddgicrmopn', 1), ('aaa', 1), ('jchzalrnumimnmhp', 0),
         ('haegwjzuvuyypxyu', 0), ('dvszwmarrgswjxmb', 0)]


def valid(inp):
  return bool((not re.findall('ab|cd|pq|xy', inp)) and
              (len(re.findall('a|e|i|o|u', inp)) >= 3) and
              (re.findall(r'([a-z])\1', inp)))


def answer1(inp):
  return sum(list(map(valid, inp.splitlines())))


tests2 = [
    ("qjhvhtzxzqqjkmpb", 1),
    ("xxyxx", 1),
    ("uurcxstgmygtbstg", 0),
    ("ieodomkazucvgmuy", 0),
]


def pairtwice(inp):
  return bool(re.findall(r'([a-z][a-z]).*?\1', inp))
  prev = None
  count = 0
  for c in inp:
    if c == prev:
      count += 1
    else:
      if count == 1:
        return True
      else:
        count = 0
    prev = c
  return False


def sandwich(inp):
  return bool(re.findall(r'([a-z]).\1', inp))


def valid2(inp):
  return pairtwice(inp) and sandwich(inp)


def answer2(inp):
  return sum(list(map(valid2, inp.splitlines())))


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
