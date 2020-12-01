from utils import data19
import intcode
from collections import defaultdict

data = data19(23)


def answer1(inp):
  prog = intcode.getcodes(data)
  computers = [intcode.Computer(prog) for i in range(50)]
  # boot up
  queues = defaultdict(list)
  for i in range(50):
    queues[i].append(i)
  while True:
    outs = [computers[i].run(*queues.get(i, [-1])) for i in range(50)]
    queues = defaultdict(list)
    for out in outs:
      while out:
        (who, x, y), out = out[:3], out[3:]
        if who == 255:
          return y
        queues[who].extend([x, y])


def answer2(inp):
  prog = intcode.getcodes(data)
  computers = [intcode.Computer(prog) for i in range(50)]
  # boot up
  queues = defaultdict(list)
  for i in range(50):
    queues[i].append(i)
  nat = (None, None)
  seen = set()
  step = 0
  while True:
    outs = [computers[i].run(*queues.get(i, [-1])) for i in range(50)]
    queues = defaultdict(list)
    for out in outs:
      while out:
        (who, x, y), out = out[:3], out[3:]
        if who == 255:
          print(f"NAT REcieved ({x}, {y})")
          nat = (x, y)
        else:
          queues[who].extend([x, y])
    if len(queues) == 0 and step > 0:
      print(f"EMPTY, {nat} on step {step}")
      if nat[1] in seen:
        return nat[1]
      seen.add(nat[1])
      queues[0] = list(nat)
    step += 1


if __name__ == "__main__":
  ans1 = answer1(data)
  print("Answer1:", ans1)

  ans2 = answer2(data)
  print("Answer2:", ans2)
