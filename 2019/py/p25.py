import intcode
from utils import data19

data = data19(25)


def render(out):
  print(''.join(map(chr, filter(lambda x: x < 128, out))))


# In order to trigger the floor, you want the
# Hypercube
# the mouse
# the antenna
# and the semiconductor

# Answer for part 1 is 20483

if __name__ == "__main__":

  prog = intcode.getcodes(data)
  comp = intcode.Computer(prog)

  inp = ""

  while True:
    out = comp.run(*list(map(ord, inp)))
    render(out)
    inp = input() + "\n"
