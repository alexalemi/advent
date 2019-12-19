from utils import data19
from intcode import run

data = data19(7)

tests = [
        (("3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0", "4,3,2,1,0"), 43210),
        (("3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0", "0,1,2,3,4"), 54321),
        (("3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0", "1,0,4,3,2"), 65210),
        ]

def answer1(inp):
    prog, inps = inp
    prog = [int(x) for x in prog.split(",")]
    out = 0
    for i, inp in enumerate(inps.split(",")):
        print(i, inp, out)
        outs = run(prog[:], [int(inp), out])
        print("outs = ", outs)
        out = outs[0]
    return out

tests2 = []

def answer2(inp):
    return None

if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
