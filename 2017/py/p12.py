from typing import Iterable

from utils import data17

data_string = data17(12)

type Graph = dict[int, set[int]]


def parse(inp: str) -> Graph:
    graph = {}
    for line in inp.splitlines():
        parts = line.split("<->")
        parent = int(parts[0])
        children = set(map(int, parts[1].split(",")))
        graph[parent] = children
    return graph


test_string = """0 <-> 2
1 <-> 1
2 <-> 0, 3, 4
3 <-> 2, 4
4 <-> 2, 3, 6
5 <-> 6
6 <-> 4, 5"""

test_data = parse(test_string)
data = parse(data_string)


def cluster(graph: Graph, start: int = 0) -> set[int]:
    """Find the cluster of points connected to the start."""
    frontier = {start}
    seen = {start}

    while frontier:
        current = frontier.pop()
        seen.add(current)
        candidates = graph[current]
        frontier |= candidates - seen

    return seen


def cluster_size(graph: Graph, start: int = 0) -> int:
    return len(cluster(graph, start))


assert cluster_size(test_data) == 6, "Failed the test case"

ans1 = cluster_size(data)
print(f"Answer1: {ans1}")

## Part 2


def num_clusters(graph: Graph) -> int:
    """Compute how many clusters there are."""
    graph = graph.copy()
    clusters = 0

    def first(graph: Graph) -> int:
        return next(iter(graph))

    def remove(graph: Graph, which: Iterable[int]):
        """Remove the given notes from the graph."""
        for elem in which:
            graph.pop(elem)

    while graph:
        root = first(graph)
        found = cluster(graph, root)
        clusters += 1
        remove(graph, found)

    return clusters


assert num_clusters(test_data) == 2, "Failed test for part 2"

ans2 = num_clusters(data)
print(f"Answer2: {ans2}")
