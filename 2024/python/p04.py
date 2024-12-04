# Advent of Code 2024 - Day 4

type Coord = tuple[int, int]
type Board = dict[Board, chr]

with open("../input/04.txt") as f:
    raw_data = f.read()

raw_test_data = """MMMSXXMASM
MSAMXMSMSA
AMXSXMAAMM
MSAMASMSMX
XMASAMXAMM
XXAMMXXAMA
SMSMSASXSS
SAXAMASAAA
MAMMMXMMMM
MXMXAXMASX"""

def process(s: str) -> Board:
    return {(x,y): c for y, line in enumerate(s.splitlines())
                     for x, c in enumerate(line)}

data = process(raw_data)
test_data = process(raw_test_data)

def compose(*funcs):
    def f(x):
        for f in reversed(funcs):
            x = f(x)
        return x
    return f


def north(loc: Coord) -> Coord:
    x, y = loc
    return (x, y-1)

def west(loc: Coord) -> Coord:
    x, y = loc
    return (x-1, y)

def east(loc: Coord) -> Coord:
    x, y = loc
    return (x+1, y)

def south(loc: Coord) -> Coord:
    x, y = loc
    return (x, y+1)

northwest = compose(north, west)
northeast = compose(north, east)
southwest = compose(south, west)
southeast = compose(south, east)

def match(board, loc, direction, query='XMAS'):
    for letter in query:
        if board.get(loc) != letter:
            return False
        loc = direction(loc)
    return True

def count_xmas(board: Board) -> int:
    count = 0
    for start in board:
        for direction in (north, south, east, west, northwest, northeast, southwest, southeast):
            if match(board, start, direction):
                count += 1
    return count

assert count_xmas(test_data) == 18, "Failed Part 1 test"
ans1 = count_xmas(data)
assert ans1 == 2591, "Failed part 1!"

## Part 2
## find the X-MASes

def match_x_mas(board: Board, start: Coord) -> bool:
    get = lambda fn: board.get(fn(start))
    identity = lambda x: x

    if get(identity) != 'A':
        return False

    if get(northwest) == 'M':
        if get(southeast) != 'S':
            return False
    elif get(southeast) == 'M':
        if get(northwest) != 'S':
            return False
    else:
        return False

    if get(northeast) == 'M':
        if get(southwest) != 'S':
            return False
    elif get(southwest) == 'M':
        if get(northeast) != 'S':
            return False
    else:
        return False

    return True


def count_x_mas(board: Board) -> int:
    count = 0
    for start in board:
        if match_x_mas(board, start):
            count += 1
    return count

assert count_x_mas(test_data) == 9, "Failed part 2 test!"
ans2 = count_x_mas(data)
assert ans2 == 1880, "Failed part 2!"



if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")



