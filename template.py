from utils import data19

data = data19(4)

tests = []


def answer1(inp):
  return None


tests2 = []


def answer2(inp):
  return None


if __name__ == "__main__":
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
