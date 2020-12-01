import intcode
from utils import data19

data = data19(21)


def render(out):
  print(''.join(map(chr, filter(lambda x: x < 128, out))))


def execute(p):
  prog = intcode.getcodes(data)
  comp = intcode.Computer(prog)

  out = comp.run()
  render(out)
  for line in p:
    print(line)
    out = comp.run(*map(ord, line + '\n'))
    render(out)

  return out[-1]


def answer1():
  # jump if (NOT A) OR (NOT B) OR (NOT C) AND D
  program = [
      "NOT A J", "NOT B T", "OR T J", "NOT C T", "OR T J", "AND D J", "WALK"
  ]
  return execute(program)


def answer2():
  program = [
      "NOT A J",
      "NOT B T",
      "OR T J",
      "NOT C T",
      "OR T J",
      "AND D J",  # J if (NOT A) OR (NOT B) OR (NOT C) AND D
      # need either E true, or H
      "NOT J T",
      "OR E T",
      "OR H T",
      "AND T J",
      "RUN",
  ]
  return execute(program)


if __name__ == "__main__":
  ans1 = answer1()
  print("Answer1:", ans1)

  ans2 = answer2()
  print("Answer2:", ans2)
