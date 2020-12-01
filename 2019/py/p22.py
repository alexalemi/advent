from utils import data19
from parse import parse
from tqdm import tqdm

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


def gcd(x, y):
  while y:
    x, y = y, x % y
  return x 

def power(x, y, m): 
  if (y == 0):
    return 1
  p = power(x, y // 2, m) % m 
  p = (p * p) % m 
  if y % 2 == 0:
    return p 
  else: 
    return ((x * p) % m) 

def back_ops(tot):
  def back_new_stack(pk):
    return tot - pk

  def back_cut(n, pk):
    return (pk + n) % tot

  def back_deal(n, pk):
    assert gcd(n, tot) == 1
    return power(n, tot - 2, tot)

  return back_new_stack, back_cut, back_deal


def answer2(inp):
  tot = 119_315_717_514_047
  back_new_stack, back_cut, back_deal = back_ops(tot)
  pk = 2020
  for _ in tqdm(range(101_741_582_076_661)):
    for line in reversed(inp.splitlines()):
      if line.startswith('cut'):
        n = int(line[4:].strip())
        pk = back_cut(n, pk)
      elif line.startswith('deal with increment'):
        n = int(line[len('deal with increment '):].strip())
        pk = back_deal(n, pk)
      elif line.startswith('deal into new stack'):
        pk = back_new_stack(pk)
  return pk


if __name__ == "__main__":
  deck = list(range(10))
  for inp, ans in tests:
    myans = shuffle(inp, deck)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  ans1 = answer1(data)
  print("Answer1:", ans1)

  ans2 = answer2(data)
  print("Answer2:", ans2)
  deck = shuffle(data, list(range(10_007)))
