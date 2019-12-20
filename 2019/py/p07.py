from utils import data19
import io
import itertools
import intcode
import pintcode
import threading
import queue

data = data19(7)

tests = [
        (("3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0", "4,3,2,1,0"), 43210),
        (("3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0", "0,1,2,3,4"), 54321),
        (("3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0", "1,0,4,3,2"), 65210),
        ]

def answer1(inp):
    prog, inps = inp
    prog = intcode.getcodes(prog)
    inps = intcode.getcodes(inps)
    out = 0
    for i, inp in enumerate(inps):
        # print(i, inp, out)
        outs = intcode.run(prog[:], [inp, out])
        # print("outs = ", outs)
        out = outs[0]
    return out

tests2 = [
        (("3,26,1001,26,-4,26,3,27,1002,27,2,27,1,27,26,27,4,27,1001,28,-1,28,1005,28,6,99,0,0,5", "9,8,7,6,5"), 139629729),
        (("3,52,1001,52,-5,52,3,53,1,52,56,54,1007,54,5,55,1005,55,26,1001,54,-5,54,1105,1,12,1,53,54,53,1008,54,0,55,1001,55,1,55,2,53,55,53,4,53,1001,56,-1,56,1005,56,6,99,0,0,0,0,10", "9,7,8,5,6"), 18216)]

def seeded_gen(seeds, g):
    for s in seeds:
        yield s
    yield from g

def answer2(inp):
    prog, inps = inp
    prog = intcode.getcodes(prog)
    inps = intcode.getcodes(inps)

    computers = [intcode.Computer(prog[:], [i]) for i in inps]
    current = 0
    out = [0]
    while not all(c.finished for c in computers):
        out = computers[current].run(*out)
        current = (current + 1) % 5
    return computers[-1].outputs[-1]

if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"

  print("Answer1:", max([answer1((data, ','.join(map(str,x)))) for x in itertools.permutations(range(5))]))

  for inp, ans in tests2:
     myans = answer2(inp)
     assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", max([answer2((data, ','.join(map(str,x)))) for x in itertools.permutations([5,6,7,8,9])]))
