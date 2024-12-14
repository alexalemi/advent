# Advent of Code 2024 - Day 14

import tqdm
import math
from collections import defaultdict, Counter
import parser

with open("../input/14.txt") as f:
    raw_data = f.read()

raw_test_data = """p=0,4 v=3,-3
p=6,3 v=-1,-3
p=10,3 v=-1,2
p=2,0 v=2,-1
p=0,0 v=1,3
p=3,0 v=-2,-2
p=7,6 v=-1,-3
p=3,0 v=-1,-2
p=9,3 v=2,3
p=7,3 v=-1,2
p=2,4 v=2,-3
p=9,5 v=-3,-3
"""

WIDTH = 101
HEIGHT = 103
EXTENT = (WIDTH, HEIGHT)
TEST_EXTENT = (11, 7)


def process(s: str):
    info = parser.parse(s, parser=parser.ints)
    output = []
    for line in info:
        (x, y, vx, vy) = line
        output.append(((x, y), (vx, vy)))
    return output


test_data = process(raw_test_data)
data = process(raw_data)


def mod(a, b):
    x1, y1 = a
    x2, y2 = b
    return (x1 % x2, y1 % y2)


def times(v, t):
    vx, vy = v
    return (t * vx, t * vy)


def plus(pos, vel):
    x, y = pos
    vx, vy = vel
    return (x + vx, y + vy)


def step(data, extent, t=1):
    return [(mod(plus(x, times(v, t)), extent), v) for (x, v) in data]


def quadrant(pos, extent):
    (x, y) = pos
    (width, height) = extent

    if (x < ((width - 1) / 2)) and (y < ((height - 1) / 2)):
        return 0
    elif (x < ((width - 1) / 2)) and (y > ((height - 1) / 2)):
        return 1
    elif (x > ((width - 1) / 2)) and (y < ((height - 1) / 2)):
        return 2
    elif (x > ((width - 1) / 2)) and (y > ((height - 1) / 2)):
        return 3
    return None


def score(data, extent):
    quad_counts = [0, 0, 0, 0]
    for x, v in data:
        if (quad := quadrant(x, extent)) is not None:
            quad_counts[quad] += 1
    return math.prod(quad_counts)


def part1(data, extent=EXTENT, rounds=100) -> int:
    data = step(data, extent, 100)
    return score(data, extent)


assert part1(test_data, TEST_EXTENT) == 12, "Failed part 1 test"
ans1 = part1(data)
assert ans1 == 224357412, "Failed part 1"

## Part 2


def printer(data, extent=EXTENT, file=None):
    counts = Counter(x[0] for x in data)
    width, height = extent
    for y in range(height):
        for x in range(width):
            print(counts[(x, y)] or ".", end="", file=file)
        print(file=file)


# 1_000 too low
# 1_000_000  is too high


def is_symmetric(data, extent):
    width, height = extent
    obs = {x[0] for x in data}
    rev = {(width - x - 1, y) for (x, y) in obs}
    return obs == rev


def part2(data, extent):
    width, height = extent
    min_seen = 224357412
    at_min = 0

    for t in tqdm.trange(10_000):
        data = step(data, extent)
        # obs = {x[0] for x in data}
        # rev = {(width - x - 1, y) for (x, y) in obs}
        local_score = score(data, extent)
        if local_score < min_seen:
            print(t, local_score)
            print()
            print(t + 1)
            printer(data, EXTENT)
            print()
            at_min = t + 1
        min_seen = min(local_score, min_seen)
    return at_min


ans2 = part2(data, EXTENT)
assert ans2 == 7083, "Failed part 2"

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
