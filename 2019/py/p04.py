from utils import data19
from utils import pairwise

data = data19(4)


def inrange(x, low=-1, high=1000000):
    return low < x < high


def adjacent(x):
    return any([x == y for (x, y) in pairwise(str(x))])


def increasing(x):
    return all([int(y) >= int(x) for (x, y) in pairwise(str(x))])


def valid(x, low=-1, high=1000000):
    return inrange(x) and adjacent(x) and increasing(x)


assert valid(111111)
assert not valid(223450)
assert not valid(123789)

tests = []


def answer1(inp):
    low, high = map(int, inp.strip().split("-"))
    return sum([valid(x, low, high) for x in range(low, high)])


tests2 = []


def strict_adjacent(x):
    prev = None
    for i, c in enumerate(str(x)):
        if c == prev:
            continue
        prev = c
        run = 0
        for c2 in str(x)[i:]:
            if c2 == c:
                run += 1
            else:
                break
        if run == 2:
            return True
    return False


def valid2(x, low=-1, high=1000000):
    return inrange(x) and strict_adjacent(x) and increasing(x)


assert valid2(112233)
assert not valid2(123444)
assert valid2(111122)


def answer2(inp):
    low, high = map(int, inp.strip().split("-"))
    return sum([valid2(x, low, high) for x in range(low, high)])


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    print("Answer1:", answer1(data))

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    print("Answer2:", answer2(data))
