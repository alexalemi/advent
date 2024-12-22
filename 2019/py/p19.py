import intcode
from utils import data19

data = data19(19)

tests = []


def answer1(inp):
    prog = intcode.getcodes(inp)
    results = {}
    for x in range(50):
        for y in range(50):
            result = intcode.Computer(prog).run(x, y)[0]
            results[(x, y)] = result
    return sum(results.values())


tests2 = []

prog = intcode.getcodes(data)
results = {}


def query(x, y):
    if (x, y) in results:
        return results[(x, y)]
    else:
        result = intcode.Computer(prog).run(x, y)[0]
        results[(x, y)] = result
        return result


def fits(x, y, s=100):
    return (
        query(x, y)
        and query(x + s - 1, y)
        and query(x, y + s - 1)
        and query(x + s - 1, y + s - 1)
    )


def answer2(inp, s=100):
    pos = (770, 1000)
    startpos = (0, 0)
    fitdict = {(x, y): fits(x, y) for x in range(650, 730) for y in range(850, 930)}
    best = min((key for key, val in fitdict.items() if val), key=lambda x: x[0] + x[1])
    return best[0] * 10_000 + best[1]


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    ans1 = answer1(data)
    print("Answer1:", ans1)

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    ans2 = answer2(data)
    print("Answer2:", ans2)
