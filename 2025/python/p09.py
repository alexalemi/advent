from itertools import combinations, pairwise
from typing import NamedTuple, Callable
from collections import defaultdict

test_string = """7,1
11,1
11,7
9,7
9,5
2,5
2,3
7,3"""


class Coord(NamedTuple):
    x: int
    y: int


def process(s: str) -> list[Coord]:
    return [Coord(*map(int, line.split(","))) for line in s.splitlines()]


with open("../input/09.txt") as f:
    data = process(f.read())

test_data = process(test_string)


def area(coord1: Coord, coord2: Coord) -> int:
    return (1 + abs(coord1.x - coord2.x)) * (1 + abs(coord1.y - coord2.y))


def part1(data: list[Coord]) -> int:
    return max(area(*pair) for pair in combinations(data, 2))


assert part1(test_data) == 50, "Failed part 1 test"

ans1 = part1(data)
print(f"Answer 1: {ans1}")
assert ans1 == 4790063600, "Failed part 1 answer"


def contains(a: int, edge: tuple[int, int], strict: bool = False) -> bool:
    l, r = edge
    if strict:
        return l < a < r
    else:
        return l <= a <= r


def make_valid_test(data: list[Coord]) -> Callable[[tuple[Coord, Coord]], bool]:
    red_tiles = set(data)
    horizontal_lines: dict[int, tuple[int, int]] = defaultdict(set)
    vertical_lines: dict[int, tuple[int, int]] = defaultdict(set)

    for a, b in pairwise(data + [data[0]]):
        if a.x == b.x:
            # they have the same x, so its a vertical line
            vertical_lines[a.x].add(tuple(sorted((a.y, b.y))))
        elif a.y == b.y:
            # this is a horizontal line
            horizontal_lines[a.y].add(tuple(sorted((a.x, b.x))))

    horizontal_lines = dict(sorted(horizontal_lines.items(), key=lambda item: item[0]))

    def in_vertical_line(a: Coord) -> bool:
        for edge in vertical_lines[a.x]:
            if contains(a.y, edge):
                return True
        return False

    def vertical_crossings(a: Coord) -> int:
        crossings = 0
        for key, edges in horizontal_lines.items():
            if key > a.y:
                break
            for edge in edges:
                if contains(a.x + 0.5, edge):
                    crossings += 1
        return crossings

    def inside(a: Coord) -> bool:
        return (
            (a in red_tiles) or in_vertical_line(a) or (vertical_crossings(a) % 2 == 1)
        )

    def vertical_intersection(y: int, xs: tuple[int, int]) -> bool:
        for x, edges in vertical_lines.items():
            if contains(x, xs, strict=True):
                for edge in edges:
                    if contains(y, edge, True):
                        return True
        return False

    def horizontal_intersection(x: int, ys: tuple[int, int]) -> bool:
        for y, edges in horizontal_lines.items():
            if contains(y, ys, strict=True):
                for edge in edges:
                    if contains(x, edge, True):
                        return True
        return False

    def valid(pair: tuple[Coord, Coord]) -> bool:
        a, b = pair
        x1, y1 = a
        x2, y2 = b

        xs = tuple(sorted((x1, x2)))
        ys = tuple(sorted((y1, y2)))

        return (
            inside(Coord(x1, y2))
            and inside(Coord(x2, y1))
            and not vertical_intersection(y1, xs)
            and not vertical_intersection(y2, xs)
            and not horizontal_intersection(x1, ys)
            and not horizontal_intersection(x2, ys)
        )

    return valid


def part2(data: list[Coord]) -> int:
    is_valid = make_valid_test(data)
    return max((area(*pair) for pair in combinations(data, 2) if is_valid(pair)))


part2(test_data)

assert part2(test_data) == 24, "Failed part 2 test"

ans2 = part2(data)
print(f"Answer 2: {ans2}")
assert ans2 == 1516172795, "Failed part 2 answer"
