from utils import data19
import intcode

data = data19(9)

tests = [
    (
        "109,1,204,-1,1001,100,1,100,1008,100,16,101,1006,101,0,99",
        [
            109, 1, 204, -1, 1001, 100, 1, 100, 1008, 100, 16, 101, 1006, 101,
            0, 99
        ],
    ),
    ("1102,34915192,34915192,7,4,7,99,0", [1219070632396864]),
    ("104,1125899906842624,99", [1125899906842624]),
]


def answer1(inp, x=None):
  return intcode.Computer(intcode.getcodes(inp)).run(x)


tests2 = []

if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data, 1))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer1(data, 2))
