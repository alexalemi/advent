from utils import data15
import hashlib
import itertools

data = data15(4).strip()

tests = [
    ("abcdef", 609043),
    ("pqrstuv", 1048970),
]


def md5(x):
  hobj = hashlib.md5()
  hobj.update(x.encode('utf8'))
  return hobj.digest()


def mine(i, head, difficulty=5):
  return md5(head + f"{i:06d}").hex().startswith('0' * difficulty)


def answer1(inp, difficulty=5):
  for i in itertools.count():
    if mine(i, inp, difficulty):
      return i


tests2 = []


def answer2(inp):
  return answer1(inp, 6)


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
