# from utils import data19
from tqdm import tqdm

# data = data19(22)
with open('../input/22.txt', 'r') as f:
  data = f.read()

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

def inverse(a, n):
  t = 0
  r = n
  newt = 1
  newr = a
  while newr != 0:
    quotient = r // newr
    (t, newt) = (newt, t - quotient * newt)
    (r, newr) = (newr, r - quotient * newr)
  if t < 0:
    t = t + n
  return t % n

def back_ops(tot):
  def back_new_stack(pk):
    return tot - pk - 1

  def back_cut(n, pk):
    return (pk + n) % tot

  def back_deal(n, pk):
    assert gcd(n, tot) == 1
    return (inverse(n, tot) * pk) % tot

  return back_new_stack, back_cut, back_deal

def pk_shuffle(tot, inp):
  back_new_stack, back_cut, back_deal = back_ops(tot)
  funcs = []
  for line in reversed(inp.splitlines()):
    if line.startswith('cut'):
      n = int(line[4:].strip())
      funcs.append(lambda pk: back_cut(n, pk))
    elif line.startswith('deal with increment'):
      n = int(line[len('deal with increment '):].strip())
      funcs.append(lambda pk: back_deal(n, pk))
    elif line.startswith('deal into new stack'):
      funcs.append(back_new_stack)
  def f(pk):
    for f in funcs:
      pk = f(pk)
    return pk
  return f

def answer2(inp):
  tot = 119_315_717_514_047
  pk = 2020

  shuffles = 101_741_582_076_661
  shuffle = pk_shuffle(tot, inp)
  counter = 1
  pk = shuffle(pk)
  with tqdm() as pbar:
    while pk != 2020:
      prev, pk = pk, shuffle(pk)
      counter += 1
      pbar.update(1)
  print(f"Found recurrance in {counter} steps!")
  remainder = shuffles % counter
  pk = 2020
  for _ in range(remainder):
    pk = shuffle(pk)
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
