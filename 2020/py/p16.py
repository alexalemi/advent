import itertools
import time
from utils import data20

data = data20(16)

tests = [
    (
        """class: 1-3 or 5-7
row: 6-11 or 33-44
seat: 13-40 or 45-50

your ticket:
7,1,14

nearby tickets:
7,3,47
40,4,50
55,2,20
38,6,12""",
        71,
    )
]


def generate_checks(inp):
    parts = inp.split("\n\n")[0]
    checks = {}
    for line in parts.splitlines():
        leader, rest = line.split(": ")
        ranges = rest.split(" or ")
        checks[leader] = [tuple(map(int, interval.split("-"))) for interval in ranges]
    return checks


def or_ranges(ranges):
    def test(x):
        for left, right in ranges:
            if left <= x <= right:
                return True
        return False

    return test


def answer1(inp):
    checks = generate_checks(inp)
    test = or_ranges(list(itertools.chain(*checks.values())))
    nearbys = inp.split("\n\n")[-1].splitlines()[1:]
    results = filter(
        lambda x: not test(x), (int(x) for line in nearbys for x in line.split(","))
    )
    return sum(results)


tests2 = [
    (
        """departure class: 0-1 or 4-19
row: 0-5 or 8-19
seat: 0-13 or 16-19

your ticket:
11,12,13

nearby tickets:
3,9,18
15,1,5
5,14,9""",
        12,
    )
]


def answer2(inp):
    checks = generate_checks(inp)
    full_test = or_ranges(list(itertools.chain(*checks.values())))

    def valid_line(line):
        return all(full_test(int(x)) for x in line.split(","))

    nearbys = inp.split("\n\n")[-1].splitlines()[1:]
    valid_nearbys = list(filter(valid_line, nearbys))

    nearbys = [[int(x) for x in line.split(",")] for line in valid_nearbys]
    check_tests = {key: or_ranges(value) for key, value in checks.items()}
    fields = list(zip(*nearbys))
    potentials = [
        set(f for f, test in check_tests.items() if all(map(test, field)))
        for field in fields
    ]

    z = potentials
    final = {}
    while len(final) < len(checks):
        (pk, fields) = min(
            filter(lambda x: x[1], enumerate(z)), key=lambda x: len(x[1])
        )
        if len(z[pk]) == 1:
            field = fields.pop()
            final[pk] = field
            z = [z - set([field]) for z in z]

    # read in my ticket
    my_ticket = list(map(int, inp.split("\n\n")[1].splitlines()[1].split(",")))

    result = 1
    for pk, field in final.items():
        if field.startswith("departure"):
            result *= my_ticket[pk]

    return result


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
