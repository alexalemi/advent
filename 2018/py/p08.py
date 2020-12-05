"""AdventofCode.com Day 8"""

from typing import NamedTuple, List

DATA = list(map(int, open('../input/08.txt').read().strip().split()))

# reverse so that we can pop
DATA = DATA[::-1]

TESTDATA = list(map(int, "2 3 0 3 10 11 12 1 1 0 1 99 2 1 1 2".split()))[::-1]


class Node(NamedTuple):
  """Represents the Tree."""
  children: List['Node']
  metadata: List[int]


def create_node(data: List[int]) -> Node:
  """Given a list of data, create the tree."""
  nchildren = data.pop()
  nmetadata = data.pop()
  children = []
  for _ in range(nchildren):
    children.append(create_node(data))
  metadata = []
  for _ in range(nmetadata):
    metadata.append(data.pop())
  return Node(children, metadata)


def sum_meta(tree: Node) -> int:
  """Sum all of the metadata in the tree."""
  return sum(tree.metadata) + sum([sum_meta(child) for child in tree.children])


TEST_TREE = create_node(TESTDATA)
assert sum_meta(TEST_TREE) == 138, "Passed test"

TREE = create_node(DATA)
print('Answer1:', sum_meta(TREE))


def value(node: Node) -> int:
  """Compute the /value/ of the tree."""
  if not node.children:
    return sum(node.metadata)

  val = 0
  nchildren = len(node.children)
  for meta in node.metadata:
    if 0 < meta <= nchildren:
      val += value(node.children[meta - 1])
  return val


assert value(TEST_TREE) == 66, "Test value on test data"
print('Answer2:', value(TREE))
