from typing import DefaultDict, Set, List, Tuple
import re
import collections

Graph = DefaultDict[str, Set[str]]
graph = Graph()

pattern = re.compile('Step (\w) must be finished before step (\w) can begin.')

f = """Step C must be finished before step A can begin.
Step C must be finished before step F can begin.
Step A must be finished before step B can begin.
Step A must be finished before step D can begin.
Step B must be finished before step E can begin.
Step D must be finished before step E can begin.
Step F must be finished before step E can begin.
""".splitlines()

# represent the list of dependencies

with open('../input/07.txt') as f:
  for line in f:
    groups = pattern.match(line).groups()
    graph[groups[1]].add(groups[0])

all_nodes = set(graph.keys()).union(set.union(*graph.values()))

available = all_nodes.difference(set(graph.keys()))
ordering = []

while available:
  next_node = sorted(available)[0]
  available.remove(next_node)
  ordering.append(next_node)

  available = available.union(
      {node for node, deps in graph.items() if not (deps - set(ordering))})
  available = available - set(ordering)

  print(f'current node: {next_node}, current available: {available}')

answer1 = ''.join(ordering)
print('ANSWER1: ', answer1)

# Now let's try that again with workers

import heapq

print('PART DEUX')

available = all_nodes.difference(set(graph.keys()))
ordering = []

time = 0
Time = int
Node = int
workers: List[Tuple[Time, Node]] = []
NUMWORKERS = 5
BASETIME = 60


def time_till_done(time, job):
  return time + BASETIME + ord(job) - ord('A') + 1


assert time_till_done(0, 'A') == 61

print(available)

while len(ordering) < len(all_nodes):
  while available and (len(workers) < NUMWORKERS):
    next_node = sorted(available)[0]
    available.remove(next_node)
    heapq.heappush(workers, (time_till_done(time, next_node), next_node))
    print(f'pushed {next_node}, workers: {workers}')
  # we have no workers left, we must advance time
  new_time, node = heapq.heappop(workers)
  time = new_time
  ordering.append(node)

  available = available.union(
      {node for node, deps in graph.items() if not (deps - set(ordering))})
  available = available - set(ordering) - set(x[1] for x in workers)
  print(
      f'current node: {next_node}, current available: {available}, workers: {workers}'
  )

answer2 = ''.join(ordering)

print(f'Answer2: {answer2}, total time: {time}')
