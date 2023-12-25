# Advent of Code 2023 - Day 25 - Snowverload

import networkx as nx
import math

with open("../input/25.txt") as f:
    data_string = f.read()

test_string = """jqt: rhn xhk nvd
rsh: frs pzl lsr
xhk: hfx
cmg: qnr nvd lhk bvb
rhn: xhk bvb hfx
bvb: xhk hfx
pzl: lsr hfx nvd
qnr: nvd
ntq: jqt hfx bvb xhk
nvd: lhk
lsr: lhk
rzs: qnr cmg lsr rsh
frs: qnr lhk lsr"""

def process(s: str) -> dict[str, set[str]]:
    graph = {}
    for line in s.splitlines():
        node, children = line.split(": ")
        for child in children.split():
            graph.setdefault(node, set()).add(child)
            graph.setdefault(child, set()).add(node)
    return graph

test_data = process(test_string)
data = process(data_string)

graph = nx.Graph(data)
# I used networkx to visualie the graph with
# nx.draw(graph, with_labels=True)
# and then found the three edges I had to cut.

new_data = data.copy()
to_remove = [('dgc', 'fqn'), ('vps', 'htp'), ('rpd', 'ttj')]

for (fm, to) in to_remove:
   new_data[fm].remove(to)
   new_data[to].remove(fm)

def find_clusters(graph):
    g = graph.copy()
    sizes = []
    while g:
        cluster = set()
        frontier = [next(iter(g))]
        while frontier:
            new = frontier.pop()
            cluster.add(new)
            for neigh in g[new]:
                if neigh in g and neigh not in cluster:
                    frontier.append(neigh)
        sizes.append(len(cluster))
        for x in cluster:
            del g[x]
    return sizes

ans1 = math.prod(find_clusters(new_data))
assert ans1 == 600225

# Part 2 was the same as always, you just push the button if you 
# have completed all of the other parts for the year.

if __name__ == "__main__":
    print("Answer1:", ans1)

