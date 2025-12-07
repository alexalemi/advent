import operator
from functools import reduce

with open("../input/06.txt") as f:
    data_string = f.read()


test_string = """123 328  51 64 
 45 64  387 23 
  6 98  215 314
*   +   *   +  """


def safeint(s: str) -> int:
    return int(s.strip() or -1)


def split_on(seq, sentinel):
    groups = []
    part = []
    for elem in seq:
        if elem == sentinel:
            groups.append(tuple(part))
            part = []
        else:
            part.append(elem)
    else:
        groups.append(tuple(part))
    return tuple(groups)


def process(inp: str):
    """Process the input into problems and columns and operators."""
    lines = inp.splitlines()
    op_row = lines[-1]
    op_lookup = {"+": operator.add, "*": operator.mul}
    ops = (op_lookup.get(op) for op in op_row.split())

    problems = zip(*(map(int, line.split()) for line in lines[:-1]))

    cols = split_on((safeint("".join(col)) for col in zip(*lines[:-1])), -1)

    return tuple(zip(ops, problems, cols))


test_data = process(test_string)
data = process(data_string)


def part1(data) -> int:
    return sum(reduce(op, problem) for op, problem, _ in data)


assert part1(test_data) == 33210 + 490 + 4243455 + 401, "Failed part 1 test"


def part2(data) -> int:
    return sum(reduce(op, column) for op, _, column in data)


assert part2(test_data) == 1058 + 3253600 + 625 + 8544, "Failed part 2 test"


if __name__ == "__main__":
    ans1 = part1(data)
    print(f"Answer 1: {ans1}")

    ans2 = part2(data)
    print(f"Answer 2: {ans2}")
