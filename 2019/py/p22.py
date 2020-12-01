from utils import data19
from parse import parse

data = data19(22)

tests = [
    ("""deal with increment 7
deal into new stack
deal into new stack""", [0, 3, 6, 9, 2, 5, 8, 1, 4, 7]),
    ("""cut 6
deal with increment 7
deal into new stack""", [3, 0, 7, 4, 1, 8, 5, 2, 9, 6]),
    ("""deal with increment 7
deal with increment 9
cut -2""", [6, 3, 0, 7, 4, 1, 8, 5, 2, 9]),
    ("""deal into new stack
cut -2
deal with increment 7
cut 8
cut -4
deal with increment 7
cut 3
deal with increment 9
deal with increment 3
cut -1""", [9, 2, 5, 8, 1, 4, 7, 0, 3, 6]),
]


def new_stack(deck):
  return list(reversed(deck))


def cut(n, deck):
  return deck[n:] + deck[:n]


def deal(n, deck):
  out = deck[:]
  pk = 0
  N = len(deck)
  for c in deck:
    out[pk] = c
    pk = (pk + n) % N
  return out


def shuffle(prog, deck):
  for line in prog.splitlines():
    if line.startswith('cut'):
      n = int(line[4:].strip())
      deck = cut(n, deck)
    elif line.startswith('deal with increment'):
      n = int(line[len('deal with increment '):].strip())
      deck = deal(n, deck)
    elif line.startswith('deal into new stack'):
      deck = new_stack(deck)
  return deck


def answer1(inp):
  deck = list(range(10_007))
  return shuffle(inp, deck).index(2019)


def answer2(inp):
  deck = list(range(119_315_717_514_047))
  for _ in range(101_741_582_076_661):
    deck = shuffle(inp, deck)
  return deck[2020]


if __name__ == "__main__":
  deck = list(range(10))
  for inp, ans in tests:
    myans = shuffle(inp, deck)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  ans1 = answer1(data)
  print("Answer1:", ans1)

  # for inp, ans in tests2:
  #   myans = answer2(inp)
  #   assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  # ans2 = answer2(data)
  # print("Answer2:", ans2)
  deck = shuffle(data, list(range(10_007)))
