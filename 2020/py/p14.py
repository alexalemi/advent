import itertools
import time
from utils import data20

data = data20(14)

tests = [("""mask = XXXXXXXXXXXXXXXXXXXXXXXXXXXXX1XXXX0X
mem[8] = 11
mem[7] = 101
mem[8] = 0""", 165)]


def read_mask(mask_line, keep=('1', '0')):
  assert mask_line.startswith('mask')
  mask_str = mask_line.split(' = ')[1].strip()
  assert len(mask_str) == 36
  mask = {}
  for i, c in enumerate(mask_str[::-1]):
    if c in keep:
      mask[i] = c
  return mask


def read_line(write_line):
  assert write_line.startswith('mem['), f"Bad start: {write_line}"
  addr = int(write_line[4:].split(']')[0])
  value = int(write_line.split(' = ')[1].strip())
  return addr, value


def apply_mask(mask, value):
  bits = {i: c for i, c in enumerate(bin(value)[2:][::-1])}
  for b, v in mask.items():
    bits[b] = v
  return int("".join(bits.get(i, '0') for i in range(36))[::-1], 2)


def answer1(inp):
  lines = inp.strip().splitlines()
  mem = {}
  for line in lines:
    if line.startswith('mask'):
      mask = read_mask(line)
    else:
      addr, value = read_line(line)
      mem[addr] = apply_mask(mask, value)

  return sum(v for v in mem.values())


tests2 = [("""mask = 000000000000000000000000000000X1001X
mem[42] = 100
mask = 00000000000000000000000000000000X0XX
mem[26] = 1""", 208)]


def apply_memmask(mask, addr):
  bits = {i: c for i, c in enumerate(bin(addr)[2:][::-1])}
  for b, v in mask.items():
    if v == 'X':
      v = '01'
    bits[b] = v
  for x in itertools.product(*[bits.get(i, '0') for i in range(36)]):
    yield int("".join(x)[::-1], 2)


def answer2(inp):
  lines = inp.strip().splitlines()
  mem = {}
  for line in lines:
    if line.startswith('mask'):
      mask = read_mask(line, keep=('X', '1'))
    else:
      addr, value = read_line(line)
      for addr in apply_memmask(mask, addr):
        mem[addr] = value

  return sum(v for v in mem.values())


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
