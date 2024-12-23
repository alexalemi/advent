# Advent of Code 2024 - Day 23

from functools import reduce
from typing import Iterator
from collections import defaultdict

type Node = str
type Links = set[Node]
type Graph = dict[Node, Links]

with open("../input/23.txt") as f:
    raw_data = f.read()

raw_test_data = """kh-tc
qp-kh
de-cg
ka-co
yn-aq
qp-ub
cg-tb
vc-aq
tb-ka
wh-tc
yn-cg
kh-ub
ta-co
de-co
tc-td
tb-wq
wh-td
ta-ka
td-qp
aq-cg
wq-ub
ub-vc
de-ta
wq-aq
wq-vc
wh-yn
ka-de
kh-ta
co-tc
wh-qp
tb-vc
td-yn"""


def parse(s: str) -> list[Links]:
    return [set(line.split("-")) for line in s.splitlines()]


data = parse(raw_data)
test_data = parse(raw_test_data)

## Logic


def build_graph(links: list[Links]) -> Graph:
    graph = defaultdict(set)
    for frm, to in links:
        graph[frm].add(to)
        graph[to].add(frm)
    return graph


def triplets(links: Iterator[Links]) -> Iterator[frozenset[Links]]:
    graph = build_graph(links)
    for a, b in links:
        others = graph[a] & graph[b]
        for c in others:
            yield frozenset({a, b, c})


def deduplicate(iterable) -> Iterator:
    seen = set()
    for x in iter(iterable):
        if x not in seen:
            yield x
        seen.add(x)


## Part 1


def part1(data: list[Links]) -> int:
    answer = 0
    trips = deduplicate(triplets(data))
    nodes = set.union(*data)
    nodes_with_ta = set(x for x in nodes if x.startswith("t"))

    for trip in trips:
        if trip & nodes_with_ta:
            answer += 1
    return answer


assert part1(test_data) == 7, "Failed part 1 test."
ans1 = part1(data)
assert ans1 == 1215, "Failed part 1"

## Part 2


def expand_component(
    graph: Graph, component: frozenset[Node]
) -> Iterator[frozenset[Node]]:
    possibilities = reduce(set.intersection, (graph[node] for node in component))
    for possible in possibilities:
        yield (component | {possible})


def naive_largest_connected_component(data: list[Links]) -> frozenset[Node]:
    graph = build_graph(data)
    components = set(frozenset(link) for link in data)
    while len(components) > 1:
        print(f"Did round, have {len(components)}", flush=True)
        components = reduce(
            set.union, (set(expand_component(graph, x)) for x in components)
        )
    return next(iter(components))


def largest_connected_component(data: list[Links]) -> frozenset[Node]:
    graph = build_graph(data)

    def bron_kerbosh(r, p, x):
        """From https://en.wikipedia.org/wiki/Bron%E2%80%93Kerbosch_algorithm"""
        if not p and not x:
            yield r
            return
        for v in p - graph[next(iter(p | x))]:
            yield from bron_kerbosh(r | {v}, p & graph[v], x & graph[v])
            p -= {v}
            x |= {v}

    return max(bron_kerbosh(set(), set(graph), set()), key=len)


def part2(data: list[Links]) -> str:
    component = largest_connected_component(data)
    return ",".join(sorted(component))


assert part2(test_data) == "co,de,ka,ta", "Failed part 2 test."
ans2 = part2(data)
assert ans2 == "bm,by,dv,ep,ia,ja,jb,ks,lv,ol,oy,uz,yt", "Failed part 2"

## Main

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
