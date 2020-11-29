from utils import data19
import intcode
import string

data = data19(17)

tests = []

def neighbors(loc):
  return [loc + 1, loc - 1, loc + 1j, loc - 1j]

def answer1(inp):
  prog = intcode.getcodes(inp)
  comp = intcode.Computer(prog)
  out = comp.run()
  s = "".join(chr(x) for x in out)
  print(s)
  vals = {}
  for y, line in enumerate(s.splitlines()):
    for x, c in enumerate(line):
      vals[x + y*1j] = c
  tot = 0
  for loc, c in vals.items():
    if c == "#":
      for n in neighbors(loc):
        if vals.get(n) != '#':
          break
      else:
        tot += int(loc.real) * int(loc.imag)
  return tot, vals
      

tests2 = []

#    L,12,L,12,R,4,
# R,4,
#    6,R,6,R,4,R,4,
#
#    L,12,L,12,R,4,
# R,6,
#    L,12,L,12,R,4,
#    6,R,6,R,4,R,4,
#    L,12,L,12,R,4,
# R,4,
#    6,R,6,R,4,R,4,
# R,6,
# L,12,L,12,R,6,
#     L,12,L,12,R,4,
#     6,R,6,R,4,R,4"



def proglength(commands):
  return len(",".join(commands))

def split(commands, funcs):
  """Attempt to split the list of commands into the given funcs."""
  output = []
  def startswith(commands, func):
    return commands[:len(func)] == func
  while commands:
    for i,func in enumerate(funcs):
      if startswith(commands, func):
        output.append(string.ascii_uppercase[i])
        commands = commands[len(func):]
        break
    else:
      return None
  return output

def consume(commands, funcs):
  def startswith(commands, func):
    return commands[:len(func)] == func
  while True:
    for i, func in enumerate(funcs):
      if startswith(commands, func):
        commands = commands[len(func):]
        break
    else:
      return commands


scommands = "L,12,L,12,R,4,R,10,R,6,R,4,R,4,L,12,L,12,R,4,R,6,L,12,L,12,R,10,R,6,R,4,R,4,L,12,L,12,R,4,R,10,R,6,R,4,R,4,R,6,L,12,L,12,R,6,L,12,L,12,R,10,R,6,R,4,R,4"
# scommands = "L,12,L,12,R,4,R,4,6,R,6,R,4,R,4,L,12,L,12,R,4,R,6,L,12,L,12,R,4,6,R,6,R,4,R,4,L,12,L,12,R,4,R,4,6,R,6,R,4,R,4,R,6,L,12,L,12,R,6,L,12,L,12,R,4,6,R,6,R,4,R,4"
commands = scommands.split(",")

def factor(commands):
  alen = 1
  a = commands[:alen]
  while proglength(a) <= 20:
    blen = 1
    b = consume(commands, [a])[:blen]
    while proglength(b) <= 20:
      clen = 1
      c = consume(commands, [a,b])[:clen]
      while proglength(c) <= 20:
        if a == b or b == c or a == c:
          print(f"a={a}, b={b}, c={c}")
        if split(commands, [a,b,c]):
          return [a,b,c]
        clen += 1
        c = consume(commands, [a,b])[:clen]
      blen += 1
      b = consume(commands, [a])[:blen]
    alen += 1
    a = commands[:alen]


#    ....................############^....
#    ....................#................
#    ................#######..............
#    ................#...#.#..............
#    ................#...#.#..............
#    ................#...#.#..............
#    ................#.#####..............
#    ................#.#.#................
#    ..........#############..............
#    ..........#.....#.#.#.#..............
#    ..........#.....#.#.#.#..............
#    ..........#.....#.#.#.#..............
#    ..........#.....#####.#..............
#    ..........#.......#...#..............
#    #####.....#.......#...#############..
#    #...#.....#.......#...............#..
#    #...#.....#.......#...............#..
#    #...#.....#.......#...............#..
#    #...#.....#.......#############...#..
#    #.........#...................#...#..
#    ###########...................#...#..
#    ..............................#...#..
#    ........................#######...#..
#    ........................#.........#..
#    ......................###########.#..
#    ......................#.#.......#.#..
#    ......................#.#...#######..
#    ......................#.#...#...#....
#    ......................#############..
#    ........................#...#...#.#..
#    ........................#...#####.#..
#    ........................#.........#..
#    ........................#.........#..
#    ........................#.........#..
#    ........................#############
#    ..................................#.#
#    ..................................#.#
#    ..................................#.#
#    ..................................#.#
#    ..................................#.#
#    ..............................#####.#
#    ..............................#.....#
#    ..............................#.....#
#    ..............................#.....#
#    ..............................#######


def answer2(inp):
  prog = intcode.getcodes(inp)
  prog[0] = 2
  funcs = factor(commands)
  main = split(commands, funcs)

  smain = ",".join(main) + '\n'
  sfuncs = [",".join(f)+'\n' for f in funcs]
  scontinuous = 'n'
  prog = intcode.getcodes(data)
  prog[0] = 2
  comp = intcode.Computer(prog)
  out = comp.run(*list(map(ord, smain)))
  out = comp.run(*list(map(ord, sfuncs[0])))
  out = comp.run(*list(map(ord, sfuncs[1])))
  out = comp.run(*list(map(ord, sfuncs[2])))
  out = comp.run(*list(map(ord, "n\n")))
  return out[-1]


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  ans1, world = answer1(data)
  print("Answer1:", ans1)

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  ans2 = answer2(data)
  print("Answer2:", ans2)


