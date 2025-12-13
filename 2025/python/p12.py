
from functools import partial
from concurrent.futures import ProcessPoolExecutor, as_completed
from tqdm import tqdm
from typing import NamedTuple, Iterable
from functools import cache, lru_cache


test_string = """0:
###
##.
##.

1:
###
##.
.##

2:
.##
###
##.

3:
##.
###
##.

4:
###
#..
###

5:
###
.#.
###

4x4: 0 0 0 0 2 0
12x5: 1 0 1 0 2 2
12x5: 1 0 1 0 3 2"""

type Coord = complex
type Shape = frozenset[Coord]

def coord(x: int, y: int) -> Coord:
    return x + 1j * y

class Board(NamedTuple):
    dimensions: tuple[int, int]
    counts: tuple[int]

class Data(NamedTuple):
    shapes: tuple[Shape]
    boards: tuple[Board]

def process(f: str) -> Data:
    shapes = []
    *shapes_str, board_str = f.split('\n\n')

    for i, shape_str in enumerate(shapes_str):
        shape = set()
        lines = shape_str.splitlines()
        assert i == int(lines[0].split(':')[0])
        for row, line in enumerate(lines[1:]):
            for col, c in enumerate(line):
                if c == '#':
                    shape.add(coord(col, row))
        shapes.append(frozenset(shape))

    boards = []
    for line in board_str.splitlines():
        dims_str, counts_str = line.split(':')
        dims = tuple(map(int, dims_str.split('x')))
        counts = tuple(map(int, counts_str.split()))
        boards.append(Board(dimensions=dims, counts=counts))

    return Data(tuple(shapes), tuple(boards))


test_data = process(test_string)
with open("../input/12.txt") as f:
    data = process(f.read())


def first_nonzero(counts: tuple[int]) -> int | None:
    for i, x in enumerate(counts):
        if x > 0:
            return i


def dec(counts: tuple[int], pk: int) -> tuple[int]:
    tmp = list(counts)
    tmp[pk] -= 1
    return tuple(tmp)

def move(shape: Shape, loc: Coord) -> Shape:
    return frozenset(x + loc for x in shape)

def horizontal_mirror(shape: Shape) -> Shape:
    return frozenset(coord(-z.real, z.imag) for z in shape)

def vertical_mirror(shape: Shape) -> Shape:
    return frozenset(coord(z.real, -z.imag) for z in shape)

def rot(shape: Shape) -> Shape:
    return frozenset(z * 1j for z in shape)

def root(shape: Shape) -> Shape:
    return move(shape, -min(shape, key=lambda z: (z.real, z.imag)))

@cache
def transformed(shape: Shape) -> Iterable[Shape]:
    i = shape
    h = horizontal_mirror(i)
    v = vertical_mirror(i)
    hv = horizontal_mirror(v)
    r = rot(i)
    hr = horizontal_mirror(r)
    vr = vertical_mirror(r)
    hvr = horizontal_mirror(vr)
    return frozenset(map(root, [i, h, v, hv, r, hr, vr, hvr]))

def print_shape(shape: Shape, invert: bool = False):
    minx = min(z.real for z in shape)
    miny = min(z.imag for z in shape)
    moved = move(shape, -coord(minx, miny))

    if invert:
        on, off = '.#'
    else:
        on, off = '#.'

    maxx = int(max(z.real for z in moved))
    maxy = int(max(z.imag for z in moved))
    for row in range(maxy+1):
        for col in range(maxx+1):
            if coord(col, row) in moved:
                print(on, end='')
            else:
                print(off, end='')
        print()


def make_has_room(shapes: list[Shape]):
    zeros = tuple(0 for _ in shapes)

    def has_room(spots: frozenset[Coord], counts: tuple[int]) -> bool:
        # print_shape(spots, invert=True)
        # print(counts)
        # print(flush=True)

        # base case
        if counts == zeros:
            return True
        # easy no
        if len(spots) < sum(n * len(shape) for n, shape in zip(counts, shapes)):
            return False
        else:
            pk = first_nonzero(counts)
            for shape in transformed(shapes[pk]):
                for loc in spots:
                    moved = move(shape, loc)
                    if moved <= spots:
                        if has_room(spots - moved, dec(counts, pk)):
                            return True

        return False

    return has_room


def can_fit(board: Board, shapes: Iterable[Shape]) -> bool:
    (w, h) = board.dimensions

    # easy no, not enough spaces
    if (w * h) < sum(n * len(shape) for n, shape in zip(board.counts, shapes)):
        return False

    # easy yes
    elif ((w // 3) * (h // 3)) > sum(board.counts):
        return True

    else:
        spots = frozenset(coord(x, y) for x in range(w) for y in range(h))
        has_room = lru_cache(1024)(make_has_room(shapes))
        return has_room(spots, board.counts)


def part1(data: Data) -> int:
    test = partial(can_fit, shapes=data.shapes)
    solveable = 0
    with ProcessPoolExecutor() as executor:
        futures = {executor.submit(test, board) for board in data.boards}
        for future in tqdm(as_completed(futures), total=len(data.boards)):
            if future.result():
                solveable += 1
    return solveable



if __name__ == "__main__":
    pass

    # test_ans = part1(test_data)
    # print(f"Test Answer: {test_ans}")
    # assert test_ans == 2, "Failed part 1 test!"

    ans1 = part1(data)
    print(f"Answer 1: {ans1}")

