import math
from collections import defaultdict
from typing import NamedTuple, Iterable
from itertools import combinations, islice


class Coord(NamedTuple):
    x: int
    y: int
    z: int


def metric(a: Coord, b: Coord) -> int:
    return (a.x - b.x) ** 2 + (a.y - b.y) ** 2 + (a.z - b.z) ** 2


def process(inp: str) -> Iterable[Coord]:
    for line in inp.splitlines():
        yield Coord(*map(int, line.split(",")))


with open("../input/08.txt") as f:
    data = list(process(f.read()))

test_string = """162,817,812
57,618,57
906,360,560
592,479,940
352,342,300
466,668,158
542,29,236
431,825,988
739,650,466
52,470,668
216,146,977
819,987,18
117,168,530
805,96,715
346,949,466
970,615,88
941,993,340
862,61,35
984,92,344
425,690,689"""


def connections(data: list[Coord]) -> Iterable[tuple[Coord, Coord]]:
    yield from sorted(combinations(data, 2), key=lambda x: metric(*x))


test_data = list(process(test_string))


def combine(data: list[Coord], cutoff: int | None = None) -> Iterable[set[Coord]]:
    junction_to_cluster = {junction: id for id, junction in enumerate(data)}
    cluster_to_junction = {id: {junction} for id, junction in enumerate(data)}

    for a, b in islice(connections(data), cutoff):
        if (cluster_a := junction_to_cluster[a]) != (
            cluster_b := junction_to_cluster[b]
        ):
            cluster_a, cluster_b = sorted((cluster_a, cluster_b))

            for junction in cluster_to_junction[cluster_b]:
                junction_to_cluster[junction] = cluster_a

            cluster_to_junction[cluster_a] |= cluster_to_junction[cluster_b]
            del cluster_to_junction[cluster_b]

        if len(cluster_to_junction) == 1:
            return (cluster_to_junction.values(), ((a, b)))

    return (cluster_to_junction.values(), ((a, b)))


def part1(data: list[Coord], cutoff: int | None = None) -> int:
    clusters, _ = combine(data, cutoff)
    return math.prod(islice(sorted((len(s) for s in clusters), reverse=True), 3))


assert part1(test_data, 10) == 5 * 4 * 2, "Failed part1 test"


def part2(data: list[Coord]) -> int:
    _, (a, b) = combine(data)
    return a.x * b.x


assert part2(test_data) == 216 * 117, "Failed part2 test"


if __name__ == "__main__":
    ans1 = part1(data, 1000)
    print(f"Answer 1: {ans1}")
    assert ans1 == 75582, "Failed part 1"

    ans2 = part2(data)
    print(f"Answer 2: {ans2}")
    assert ans2 == 59039696, "Failed part 2!"
