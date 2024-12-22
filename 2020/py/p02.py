from collections import Counter
from utils import data20

data = data20(2).strip()

tests = [
    (
        """1-3 a: abcde
1-3 b: cdefg
2-9 c: ccccccccc""",
        2,
    )
]


def parse(line):
    rule, password = line.strip().split(": ")
    times, let = rule.split()
    low, high = times.split("-")
    low = int(low)
    high = int(high)
    return (low, high, let, password)


def isvalid(line):
    low, high, let, password = parse(line)
    return low <= Counter(password)[let] <= high


def answer1(inp):
    return sum(map(isvalid, inp.splitlines()))


tests2 = [(tests[0][0], 1)]


def isvalid2(line):
    low, high, let, password = parse(line)
    return (password[low - 1] == let) ^ (password[high - 1] == let)


def answer2(inp):
    return sum(map(isvalid2, inp.splitlines()))


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
