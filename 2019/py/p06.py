
from utils import data19
from collections import defaultdict
import functools
from frozendict import frozendict

data = data19(6)

tests = [("""COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L""",
42)
]


def build_tree(s):
    tree = defaultdict(set)
    for line in s.splitlines():
        left, right = line.split(')')
        tree[left].add(right)
    return frozendict({node:frozenset(children) for node, children in tree.items()})


def roots(tree):
    children = frozenset.union(*[children for node, children in tree.items()])
    return tree.keys() - children

def leaves(tree):
    children = frozenset.union(*[children for node, children in tree.items()])
    return children - tree.keys()

def allnodes(tree):
    children = frozenset.union(*[children for node, children in tree.items()])
    return children.union(frozenset(tree.keys()))

def product(it):
    ans = 1
    for x in it:
        ans *= x
    return ans

def parents(tree, cand):
    return frozenset({node for node, children in tree.items() if cand in children})


@functools.lru_cache(None)
def orbits(tree, node):
    return sum(1 + orbits(tree, parent) for parent in parents(tree, node))

def totalorbits(tree):
    return sum(orbits(tree, node) for node in allnodes(tree))

def answer1(inp):
    tree = build_tree(inp)
    return totalorbits(tree)

tests2 = [("""COM)B
B)C
C)D
D)E
E)F
B)G
G)H
D)I
E)J
J)K
K)L
K)YOU
I)SAN""", 4)]

def parent(tree, cand):
    p = list(parents(tree, cand))
    if len(p) == 1:
        return p[0]
    return None

def neighbors(tree, start):
    return parents(tree, start).union(tree.get(start, set())) - {start}

def reroot(tree, node):
    newtree = {}
    newtree[node] = neighbors(tree, node)
    queue = list(newtree[node])
    while queue:
        current = queue.pop()
        newchildren = [n for n in neighbors(tree, current) if n not in newtree]
        if newchildren:
            newtree[current] = set(newchildren)
            queue.extend(newchildren)
    return newtree

def depth(tree, node):
    if parent(tree, node) in roots(tree):
        return 1
    else:
        return 1 + depth(tree, parent(tree, node))


def answer2(inp):
    tree = build_tree(inp)
    tree = reroot(tree, 'YOU')
    return depth(tree, 'SAN') - 2 

    

if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
