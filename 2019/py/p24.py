import library
from utils import data19
from collections import defaultdict
from copy import deepcopy

data = data19(24)

tests = [
    (
        """....#
#..#.
#..##
..#..
#....""",
        2129920,
    )
]


def process(inp):
    vals = {}
    for y, line in enumerate(inp.splitlines()):
        for x, val in enumerate(line):
            if val == "#":
                vals[complex(x, y)] = 1
    return vals


def advance(vals, n=5):
    new = vals.copy()
    for x in range(5):
        for y in range(5):
            loc = complex(x, y)
            friends = 0
            for n in library.neighbors(loc):
                if vals.get(n) == 1:
                    friends += 1
            if vals.get(loc):
                if friends != 1:
                    del new[loc]
            else:
                if friends in (1, 2):
                    new[loc] = 1
    return new


def freeze(v):
    return tuple(sorted(v.items(), key=lambda x: (x[0].real, x[0].imag)))


def biodiversity(board, n=5):
    tot = 0
    for key, val in board.items():
        if val == 1:
            x, y = library.wrap(key)
            pk = int(x) + int(y) * n
            tot += 1 << pk
    return tot


def render(board, n=5):
    print(
        "\n".join(
            "".join("#" if board.get(complex(x, y)) else "." for x in range(n))
            for y in range(n)
        )
    )


def answer1(inp, n=5):
    seen = set()
    board = process(inp)
    while (frozen := freeze(board)) not in seen:
        seen.add(frozen)
        board = advance(board)
    return biodiversity(board)


tests2 = [
    (
        (
            """....#
#..#.
#.?##
..#..
#....""",
            10,
        ),
        99,
    ),
]


def neighbors2(lvl, loc):
    x, y = loc.real, loc.imag
    if loc == complex(1, 2):
        yield (lvl, complex(0, 2))
        yield (lvl, complex(1, 1))
        yield (lvl, complex(1, 3))
        for ny in range(5):
            yield (lvl + 1, complex(0, ny))
    elif loc == complex(3, 2):
        yield (lvl, complex(3, 1))
        yield (lvl, complex(4, 2))
        yield (lvl, complex(3, 3))
        for ny in range(5):
            yield (lvl + 1, complex(4, ny))
    elif loc == complex(2, 1):
        yield (lvl, complex(2, 0))
        yield (lvl, complex(1, 1))
        yield (lvl, complex(3, 1))
        for nx in range(5):
            yield (lvl + 1, complex(nx, 0))
    elif loc == complex(2, 3):
        yield (lvl, complex(1, 3))
        yield (lvl, complex(3, 3))
        yield (lvl, complex(2, 4))
        for nx in range(5):
            yield (lvl + 1, complex(nx, 4))

    else:
        if x == 0:
            yield (lvl - 1, complex(1, 2))
            yield (lvl, loc + 1)
        elif x == 4:
            yield (lvl, loc - 1)
            yield (lvl - 1, complex(3, 2))
        else:
            yield (lvl, loc + 1)
            yield (lvl, loc - 1)

        if y == 0:
            yield (lvl - 1, complex(2, 1))
            yield (lvl, loc + 1j)
        elif y == 4:
            yield (lvl, loc - 1j)
            yield (lvl - 1, complex(2, 3))
        else:
            yield (lvl, loc + 1j)
            yield (lvl, loc - 1j)


def advance2(vals, n=5):
    new = defaultdict(dict)
    lvs = list(vals.keys())
    for lvl in range(min(lvs) - 1, max(lvs) + 2):
        for x in range(5):
            for y in range(5):
                if (x, y) == (2, 2):
                    continue
                loc = complex(x, y)
                friends = 0
                # sum neighbors
                for nl, n in neighbors2(lvl, loc):
                    if vals.get(nl, {}).get(n):
                        friends += 1
                # if currently alive
                if vals.get(lvl, {}).get(loc):
                    # must have exactly 1 friend
                    if friends == 1:
                        new[lvl][loc] = 1
                else:
                    # if dead and 1 or 2 friends:
                    if friends in (1, 2):
                        new[lvl][loc] = 1
    return new


def render2(boards, n=5):
    for lvl, board in sorted(boards.items(), key=lambda x: x[0]):
        print(f"LVL = {lvl}")
        render(board)


def answer2(inp, steps=200):
    seen = set()
    boards = defaultdict(dict)
    boards[0] = process(inp)
    for i in range(steps):
        boards = advance2(boards)
    tot = 0
    for lvl, board in boards.items():
        for k, v in board.items():
            if v == 1:
                tot += 1
    return tot


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    ans1 = answer1(data)
    print("Answer1:", ans1)

    for inp, ans in tests2:
        myans = answer2(*inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    ans2 = answer2(data)
    print("Answer2:", ans2)
