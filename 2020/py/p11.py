import time
from utils import data20
from functools import partial, lru_cache

data = data20(11)

tests = [("""L.LL.LL.LL
LLLLLLL.LL
L.L.L..L..
LLLL.LL.LL
L.LL.LL.LL
L.LLLLL.LL
..L.L.....
LLLLLLLLLL
L.LLLLLL.L
L.LLLLL.LL""", 37)]


def process(inp):
  board = {}
  for y, row in enumerate(inp.splitlines()):
    for x, c in enumerate(row):
      if c == 'L':
        board[(x, y)] = False
      else:
        board[(x, y)] = None
  return board


def render(board):
  xmax = max(z[0] for z in board.keys())
  ymax = max(z[0] for z in board.keys())
  s = ""
  for y in range(ymax + 1):
    for x in range(xmax + 1):
      state = board.get((x, y))
      if state is None:
        s += '.'
      elif state:
        s += "#"
      else:
        s += 'L'
    s += '\n'
  print(s)


def unpack(z):
  return (int(z.real), int(z.imag))


@lru_cache
def neighbors(pos):
  pos = complex(*pos)
  ns = []
  for diff in [1, -1, 1j, -1j, 1 + 1j, 1 - 1j, -1 + 1j, -1 - 1j]:
    new = pos + diff
    ns.append(unpack(new))
  return tuple(ns)


def count_neighbors(board, pos, neighbor_func=neighbors):
  return sum(1 if board.get(n) else 0 for n in neighbor_func(pos))


def step(board):
  new = board.copy()
  for pos, state in board.items():
    if state is False and count_neighbors(board, pos) == 0:
      new[pos] = True
    elif state is True and count_neighbors(board, pos) >= 4:
      new[pos] = False
    else:
      new[pos] = state
  return new


def fixed_point(f, x):
  prev = None
  while (x := f(x)) != prev:
    prev = x
  return x


def answer1(inp):
  board = process(inp)
  board = fixed_point(step, board)
  return sum(1 for pos in board if board[pos])


tests2 = [(tests[0][0], 26)]


def long_neighbors(board, pos):
  start = complex(*pos)
  for direction in [1, -1, 1j, -1j, 1 + 1j, 1 - 1j, -1 + 1j, -1 - 1j]:
    pos = start
    while unpack(pos := pos + direction) in board:
      if board[unpack(pos)] is not None:
        yield unpack(pos)
        break


def step2(board, neighbors):
  new = board.copy()

  def count_neighbors(pos):
    return sum(1 if board.get(n) is True else 0 for n in neighbors[pos])

  for pos, state in board.items():
    if state is False and count_neighbors(pos) == 0:
      new[pos] = True
    elif state is True and count_neighbors(pos) >= 5:
      new[pos] = False
    else:
      new[pos] = state
  return new


def make_neighbors(board):
  neighbors = {}
  for pos in board:
    neighbors[pos] = tuple(long_neighbors(board, pos))
  return neighbors


def answer2(inp):
  board = process(inp)
  neighbors = make_neighbors(board)
  f = partial(step2, neighbors=neighbors)
  board = fixed_point(f, board)
  return sum(1 for pos in board if board[pos])


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  start = time.time()
  ans1 = answer1(data)
  end = time.time()
  print("Answer1:", ans1, f" in {end - start:0.3e} secs")

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  start = time.time()
  ans2 = answer2(data)
  end = time.time()
  print("Answer2:", ans2, f" in {end - start:0.3e} secs")
