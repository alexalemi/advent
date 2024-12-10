# Advent of Code 2024 - Day 10

type Coord = complex
type Board = dict[Coord, int]

with open("../input/10.txt") as f:
    raw_data = f.read()

raw_test_data = """89010123
78121874
87430965
96549874
45678903
32019012
01329801
10456732"""


def process(s: str) -> Board:
    board = {}
    for y, line in enumerate(s.splitlines()):
        for x, c in enumerate(line):
            loc = x + 1j * y
            board[loc] = int(c)
    return board


data = process(raw_data)
test_data = process(raw_test_data)

north = -1j
south = 1j
east = -1
west = 1


def find_trailheads(board: Board) -> set[Coord]:
    return {loc for loc, height in board.items() if height == 0}


def neighbors(loc: Coord) -> set[Coord]:
    return {loc + north, loc + south, loc + west, loc + east}


def score(loc: Coord, board: Board) -> int:
    seen = set()
    board_locs = set(board)

    def is_valid(old, new):
        return board.get(new, -1) - board.get(old, -1) == 1

    frontier = {loc}
    while frontier:
        cand = frontier.pop()
        seen.add(cand)
        neighs = neighbors(cand) & board_locs
        neighs = {x for x in neighs if is_valid(cand, x)}
        frontier |= neighs - seen

    return sum(1 for x in seen if board[x] == 9)


def part1(board: Board) -> int:
    trailheads = find_trailheads(board)
    return sum(score(trailhead, board) for trailhead in trailheads)


assert part1(test_data) == 36, "Failed part 1 test"
ans1 = part1(data)
assert ans1 == 611, "Failed part 1"


## Part 2


def rating(loc: Coord, board: Board) -> int:
    hikes: set[tuple[Coord, ...]] = set()
    paths: set[tuple[Coord, ...]] = set()
    board_locs = set(board)

    def is_valid(old, new):
        return board.get(new, -1) - board.get(old, -1) == 1

    paths = {(loc,)}
    while paths:
        cand = paths.pop()
        loc = cand[-1]
        if len(cand) == 10:
            hikes.add(cand)

        neighs = neighbors(loc) & board_locs
        neighs = {x for x in neighs if is_valid(loc, x)}

        for step in neighs:
            paths.add(cand + (step,))

    return len(hikes)


def part2(board: Board) -> int:
    trailheads = find_trailheads(board)
    return sum(rating(trailhead, board) for trailhead in trailheads)


assert part2(test_data) == 81, "Failed part 2 test"
ans2 = part2(data)
assert ans2 == 1380, "Failed part 2"

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
