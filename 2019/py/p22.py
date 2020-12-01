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


def shuffle_params(prog, tot):
  m, b = (1, 0)
  for line in prog.splitlines():
    if line.startswith('cut'):
      n = int(line[4:].strip())
      # pk -> ( pk - n ) % tot
      (m, b) = (m, (b - n) % tot)
    elif line.startswith('deal with increment'):
      n = int(line[len('deal with increment '):].strip())
      # pk -> ( n * pk ) % tot
      (m, b) = ((n * m) % tot, (n * b) % tot)
    elif line.startswith('deal into new stack'):
      # pk -> (tot-1-pk) % tot
      (m, b) = (-m, (-1 - b) % tot)
  return (m, b)


def apply(m, b, tot):
  return lambda pk: (m * pk + b) % tot


def shuffle(prog, tot):
  m, b = shuffle_params(prog, tot)
  return apply(m, b, tot)


def render(s, tot):
  out = list(range(tot))
  for i in range(tot):
    out[s(i)] = i
  return out


def answer1(inp):
  tot = 10_007
  return shuffle(inp, tot)(2019)


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


def answer2(inp):
  tot = 119_315_717_514_047
  shuffles = 101_741_582_076_661
  m, b = shuffle_params(inp, tot)
  forward = apply(m, b, tot)
  invm = inverse(m, tot)
  reverse_params = (invm, (-invm * b) % tot)

  # need to invert the shuffle.
  def product(s1, s2):
    m1, b1 = s1
    m2, b2 = s2
    return ((m2 * m1) % tot, (m2 * b1 + b2) % tot)

  def square(s):
    return product(s, s)

  def power(s, n):
    if n == 0:
      return 1
    elif n == 1:
      return s
    elif n % 2 == 0:
      return power(product(s, s), n // 2)
    else:
      return product(s, power(product(s, s), (n - 1) // 2))

  fm, fb = power(reverse_params, shuffles)
  return apply(fm, fb, tot)(2020)


if __name__ == "__main__":
  ans1 = answer1(data)
  print("Answer1:", ans1)

  ans2 = answer2(data)
  print("Answer2:", ans2)
