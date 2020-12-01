import library
from utils import data19

data = data19(24)

tests = [
  ("""....#
#..#.
#..##
..#..
#....""", 2129920)    
]

def process(inp):
  vals = {}
  for y,line in enumerate(inp.splitlines()):
    for x,val in enumerate(line):
      if val == '#':
        vals[complex(x,y)] = 1
      else:
        vals[complex(x,y)] = 0
  return vals


def advance(vals, n=5):
  new = vals.copy()
  for x in range(5):
    for y in range(5):
      loc = complex(x,y)
      friends = 0
      for n in library.neighbors(loc):
        if vals.get(n) == 1:
          friends += 1
      if vals[loc]:
        if friends != 1:
          new[loc] = 0
      else:
        if friends in (1,2):
          new[loc] = 1
  return new

def freeze(v):
  return tuple(sorted(v.items(), key=lambda x: (x[0].real, x[0].imag)))

def biodiversity(board, n=5):
  tot = 0
  for key, val in board.items():
    if val == 1:
      x, y = library.wrap(key)
      pk = int(x) + int(y)*n
      tot += 1 << pk
  return tot

def render(board, n=5):
  print('\n'.join(''.join('#' if board[complex(x,y)] else '.' for x in range(n)) for y in range(n)))

def answer1(inp, n=5):
  seen = set()
  board = process(inp)
  while (frozen := freeze(board)) not in seen:
    seen.add(frozen)
    board = advance(board)
  render(board)
  return biodiversity(board)


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
