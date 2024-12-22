import time
from utils import data18

data = data18(14)

tests = []


def answer1(inp):
    return None


tests2 = []


def answer2(inp):
    return None


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
