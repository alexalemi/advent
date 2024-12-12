# Advent of Code 2024 - Day 12

from typing import Generator
from dataclasses import dataclass

type Coord = complex
type Edge = tuple[Coord, Coord]
type Board = dict[Coord, str]

with open("../input/12.txt") as f:
    raw_data = f.read()

raw_test_data = """AAAA
BBCD
BBCC
EEEC"""

raw_test_data2 = """RRRRIICCFF
RRRRIICCCF
VVRRRCCFFF
VVRCCCJFFF
VVVVCJJCFE
VVIVCCJJEE
VVIIICJJEE
MIIIIIJJEE
MIIISIJEEE
MMMISSJEEE"""


def process(s: str) -> Board:
    board = {}
    for y, line in enumerate(s.strip().splitlines()):
        for x, c in enumerate(line):
            board[x + 1j * y] = c
    return board


data = process(raw_data)
test_data = process(raw_test_data)
test_data2 = process(raw_test_data2)


@dataclass
class Region:
    coords: set[Coord]
    edges: set[tuple[Coord, Coord]]
    area: int
    perimeter: int


north = -1j
south = 1j
east = -1
west = 1


def neighbors(loc: Coord) -> tuple[Coord, ...]:
    return (loc + north, loc + south, loc + east, loc + west)


def remove_keys(gone, dic):
    return {k: v for k, v in dic.items() if k not in gone}


def extract_region(board: Board, neighbors=neighbors) -> tuple[Region, Board]:
    board = board.copy()
    loc, which = board.popitem()
    frontier = {loc}
    seen = {loc}
    edges = set()

    area = 1
    perimeter = 0

    while frontier:
        pos = frontier.pop()
        neighs = neighbors(pos)
        for neigh in neighs:
            if board.get(neigh) == which and neigh not in seen:
                frontier.add(neigh)
                seen.add(neigh)
                area += 1
            elif neigh not in seen:
                perimeter += 1
                edges.add((pos, neigh))

    board = remove_keys(seen, board)

    return Region(coords=seen, edges=edges, area=area, perimeter=perimeter), board


region_cache = {}


def regions(board: Board, neighbors=neighbors) -> Generator[Region, None, None]:
    if key := hash(frozenset(board.items())) in region_cache:
        yield from region_cache[key]
    else:
        output = []
        while board:
            region, board = extract_region(board, neighbors)
            yield region
            output.append(region)
        region_cache[key] = tuple(output)


def score(region: Region) -> int:
    return region.area * region.perimeter


def part1(data: Board) -> int:
    return sum(score(region) for region in regions(data))


assert part1(test_data) == 140, "Failed part 1 test 1"
assert part1(test_data2) == 1930, "Failed part 1 test 2"
ans1 = part1(data)
assert ans1 == 1363682, "Failed part 1"

## Part 2


def edge_neighbors(edge: Edge) -> tuple[Edge, ...]:
    (inside, outside) = edge
    if inside.imag == outside.imag:
        # horizontal edge
        return ((inside + north, outside + north), (inside + south, outside + south))
    else:
        # vertical
        return ((inside + west, outside + west), (inside + east, outside + east))


def sides(region: Region) -> int:
    return len(tuple(regions({x: -1 for x in region.edges}, edge_neighbors)))


def modified_score(region: Region) -> int:
    return region.area * sides(region)


def part2(data: Board) -> int:
    return sum(modified_score(region) for region in regions(data))


test_data3 = process("""EEEEE
EXXXX
EEEEE
EXXXX
EEEEE""")

test_data4 = process("""AAAAAA
AAABBA
AAABBA
ABBAAA
ABBAAA
AAAAAA""")


assert part2(test_data) == 80, "Failed part 2 test 1"
assert part2(test_data2) == 1206, "Failed part 2 test 2"
assert part2(test_data3) == 236, "Failed part 2 test 3"
assert part2(test_data4) == 368, "Failed part 2 test 4"
ans2 = part2(data)
assert ans2 == 787680, "Failed Part 2"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
