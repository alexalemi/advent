from utils import data15

data = data15(1)

tests = [
    ("(())", 0),
    ("()()", 0),
    ("(((", 3),
    ("(()(()(", 3),
    ("))(((((", 3),
    ("())", -1),
    ("))(", -1),
    (")))", -3),
    (")())())", -3),
]


def convert(s):
    return [(1 if f == "(" else -1) for f in s]


def floor(s):
    return sum(convert(s))


def cumsum(floors):
    current = 0
    for f in floors:
        current += f
        yield current


def first_basement(s):
    for i, floor in enumerate(cumsum(convert(s))):
        if floor < 0:
            return i + 1


tests2 = [(")", 1), ("()())", 5)]

if __name__ == "__main__":
    for inp, ans in tests:
        assert floor(inp) == ans, f"Failed on {inp} == {ans}, got {floor(inp)}"
    print("Answer1:", floor(data))

    for inp, ans in tests2:
        assert (
            first_basement(inp) == ans
        ), f"Failed on {inp} == {ans}, got {first_basement(inp)}"

    print("Answer2:", first_basement(data))
