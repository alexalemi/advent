import time
import string
from utils import data20

data = data20(6)

tests = [
    (
        """abc

a
b
c

ab
ac

a
a
a
a

b
""",
        11,
    )
]


def answer1(inp):
    total = 0
    for group in inp.split("\n\n"):
        groupset = set()
        for person in group.splitlines():
            for question in person:
                groupset.add(question)
        total += len(groupset)
    return total


tests2 = [(tests[0][0], 6)]


def answer2(inp):
    total = 0
    for group in inp.split("\n\n"):
        groupset = set(string.ascii_lowercase)
        for person in group.splitlines():
            personset = set(person)
            groupset = groupset.intersection(personset)
        total += len(groupset)
    return total


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
