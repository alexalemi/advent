# Advent of Code 2024 - Day 6

import tqdm

with open("../input/06.txt") as f:
    raw_data = f.read()

raw_test_data = """....#.....
.........#
..........
..#.......
.......#..
..........
.#..^.....
........#.
#.........
......#..."""


def process(s: str):
    walls = set()
    board = set()
    start = None
    for y, line in enumerate(s.splitlines()):
        for x, c in enumerate(line):
            loc = x + y * 1j
            if c == "#":
                walls.add(loc)
            elif c == "^":
                start = loc
            board.add(loc)
    return start, walls, board


test_data = process(raw_test_data)
data = process(raw_data)


def collect_seen(data):
    start, walls, board = data
    direction = -1j
    seen = set()
    loc = start

    while loc in board:
        seen.add(loc)
        new_loc = loc + direction
        if new_loc in walls:
            direction *= 1j
        else:
            loc = new_loc

    return seen


def part1(data):
    return len(collect_seen(data))


assert part1(test_data) == 41
ans1 = part1(data)
assert ans1 == 5331

## Part 2


def detect_loop(data, new_pos):
    start, walls, board = data
    direction = -1j
    loc = start
    walls = walls.copy()
    walls.add(new_pos)

    states = set()

    while loc in board:
        state = (loc, direction)
        if state in states:
            return True
        states.add(state)

        new_loc = loc + direction
        if new_loc in walls:
            direction *= 1j
        else:
            loc = new_loc

    return False


def part2(data):
    start, walls, board = data
    seen = collect_seen(data)

    places = 0
    for loc in tqdm.tqdm(seen):
        if loc != start:
            if detect_loop(data, loc):
                places += 1

    return places


assert part2(test_data) == 6
ans2 = part2(data)
assert ans2 == 1812

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
