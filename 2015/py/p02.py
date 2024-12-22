from utils import data15

data = data15(2)


def process(x):
    return list(map(int, x.strip().split("x")))


def surface_area(l, w, h):
    return 2 * l * w + 2 * w * h + 2 * h * l


def extra(l, w, h):
    return min(l * w, l * h, w * h)


tests = [
    ("2x3x4", 58),
    ("1x1x10", 43),
]


def answer1(ses):
    total = 0
    for s in ses.splitlines():
        dims = process(s)
        total += surface_area(*dims) + extra(*dims)
    return total


tests2 = [
    ("2x3x4", 34),
    ("1x1x10", 14),
]


def ribbon(l, w, h):
    return min(2 * l + 2 * w, 2 * w + 2 * h, 2 * l + 2 * h)


def bow(l, w, h):
    return l * w * h


def answer2(ses):
    total = 0
    for s in ses.splitlines():
        dims = process(s)
        total += ribbon(*dims) + bow(*dims)
    return total


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    print("Answer1:", answer1(data))

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    print("Answer2:", answer2(data))
