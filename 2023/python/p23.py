# Advent of Code 2023 - Day 23 - 

import itertools
import heapq
from typing import NamedTuple
from collections.abc import Callable, Generator

with open("../input/23.txt") as f:
    data_string = f.read()

test_string = """#.#####################
#.......#########...###
#######.#########.#.###
###.....#.>.>.###.#.###
###v#####.#v#.###.#.###
###.>...#.#.#.....#...#
###v###.#.#.#########.#
###...#.#.#.......#...#
#####.#.#.#######.#.###
#.....#.#.#.......#...#
#.#####.#.#.#########v#
#.#...#...#...###...>.#
#.#.#v#######v###.###v#
#...#.>.#...>.>.#.###.#
#####v#.#.###v#.#.###.#
#.....#...#...#.#.#...#
#.#########.###.#.#.###
#...###...#...#...#.###
###.###.#.###v#####v###
#...#...#.#.>.>.#.>.###
#.###.###.#.###.#.#v###
#.....###...###...#...#
#####################.#"""

type Coord = tuple[int, int]

class Data(NamedTuple):
    forests: set[Coord]
    paths: set[Coord]
    slopes: dict[Coord, chr]
    start: Coord
    end: Coord

def process(s: str) -> Data:
    forests = set()
    paths = set()
    slopes = {}
    start = None
    end = None
    for row, line in enumerate(s.splitlines()):
        for col, c in enumerate(line):
            if c == "#":
                forests.add((row, col))
            elif c == ".":
                paths.add((row, col))
            elif c in {">","v","<","^"}:
                slopes[(row, col)] = c
    end = max(paths, key=lambda x: x[0])
    start = min(paths, key=lambda x: x[0])
    return Data(forests, paths, slopes, start, end)

test_data = process(test_string)
data = process(data_string)

def reconstruct_path(came_from: dict[Coord, Coord],
                     end: Coord) -> list[Coord]:
  current = end
  path = []
  while current in came_from:
    path.append(current)
    current = came_from[current]
  path.reverse()
  return path

type Score = int

def astar(
    start: Coord,
    goal: Callable[[Coord], bool],
    cost: Callable[[Coord, Coord], Score],
    neighbors: Callable[[Coord], list[Coord]],
    heuristic: Callable[[Coord], Score] = lambda x: 0.0,
) -> tuple[dict[Coord, Coord], dict[Coord, Score]]:
  frontier = []
  counter = itertools.count()
  heapq.heappush(frontier, (0, -next(counter), start))
  came_from = {}
  cost_so_far = {}
  came_from[start] = None
  cost_so_far[start] = 0

  while frontier:
    print(f"{frontier=}")
    _, _, current = heapq.heappop(frontier)
    if goal(current):
      return reconstruct_path(came_from, current)
    for n in neighbors(current):
      new_cost = cost_so_far[current] + cost(current, n)
      if n not in cost_so_far or new_cost < cost_so_far[n]:
        cost_so_far[n] = new_cost
        priority = new_cost + heuristic(n)
        heapq.heappush(frontier, (priority, -next(counter), n))
        came_from[n] = current
  return None

class State(NamedTuple):
    loc: Coord
    direction: chr

def raw_neighbors(z: State) -> list[State]:
    y, x = z.loc
    if z.direction == "v":
        return [State((y+1, x), "v"),
                State((y, x+1), ">"), 
                State((y, x-1), "<")]
    elif z.direction == "^":
        return [State((y-1, x), "^"), 
                State((y, x+1), ">"), 
                State((y, x-1), "<")]
    elif z.direction == "<":
        return [State((y-1, x), "^"), 
                State((y+1, x), "v"),
                State((y, x-1), "<")]
    elif z.direction == ">":
        return [State((y-1, x), "^"), 
                State((y+1, x), "v"),
                State((y, x+1), ">")]

def raw_slide(loc: Coord, slide: chr) -> Coord:
    y, x = loc
    if slide == "v":
        return (y+1, x)
    elif slide == "^":
        return (y-1, x)
    elif slide == ">":
        return (y, x+1)
    elif slide == "<":
        return (y, x-1)

def manhattan(a: Coord, b: Coord) -> int:
    (x1, y1) = a
    (x2, y2) = b
    return abs(x1 - x2) + abs(y1 - y2)

def part1(data: Data) -> int:
    def goal(state: State) -> bool:
        return state.loc == data.end
    start = State(data.start, "v")
    def slide(x: State, prev: State) -> Coord:
        if x.loc in data.slopes:
            return State(raw_slide(x.loc, prev.loc, data.slopes[loc]), data.slopes[loc])
        return x
    def inside(z: State) -> bool:
        y, x = z.loc
        y0, x0 = data.start
        yf, xf = data.end
        return (y0 <= y <= yf) and  (x0 <= x <= xf)
    def incompatible(state: State) -> bool:
        if state.loc in data.slopes:
            slopedir = data.slopes[state.loc]
            if state.direction == ">":
                return slopedir == "<"
            elif state.direction == "<":
                return slopedir == ">"
            elif state.direction == "^":
                return slopedir == "v"
            elif state.direction == "v":
                return slopedir == "^"
            return state.direction
        return False
    def neighbors(state: State) -> list[State]:
        if state.loc in data.slopes:
            return [State(raw_slide(state.loc, data.slopes[state.loc]), data.slopes[state.loc])]
        return [neigh for neigh in raw_neighbors(state) if inside(neigh) and neigh and (neigh.loc not in data.forests) and not incompatible(neigh)]

    paths = [(start, 0)]
    lengths = []
    while paths:
        (state, steps) = paths.pop()
        for neigh in neighbors(state):
            if goal(neigh):
                lengths.append(steps+1)
            else:
                paths.append((neigh, steps+1))


    return max(lengths)

assert part1(test_data) == 94

ans1 = part1(data)
assert ans1 == 2114


## Part 2


def part2(data: Data) -> int:
    def goal(loc: Coord) -> bool:
        return loc == data.end
    def neighbors(loc: Coord) -> list[Coord]:
        y, x = loc
        return [(y-1, x), (y+1, x), (y, x+1), (y, x-1)]

    y0, x0 = data.start
    paths = [((y0+1, x0), 1, {data.start})]
    lengths = []
    while paths:
        (loc, steps, seen) = paths.pop()
        for neigh in neighbors(loc):
            if goal(neigh):
                lengths.append(steps+1)
            elif neigh not in data.forests and neigh not in seen:
                seen = seen.copy()
                seen.add(neigh)
                paths.append((neigh, steps+1, seen))

    return lengths

def part2(data: Data) -> int:
    def goal(loc: Coord) -> bool:
        return loc == data.end
    start = data.start
    def cost(a: Coord, b: Coord) -> Score:
        141 * 141 - 1
    def inside(loc: Coord) -> bool:
        y, x = loc
        y0, x0 = data.start
        yf, xf = data.end
        return (y0 <= y <= yf) and  (x0 <= x <= xf)
    def raw_neighbors(loc: Coord) -> list[Coord]:
        y, x = loc
        return [(y-1, x), (y+1, x), (y, x+1), (y, x-1)]
    def neighbors(state: State) -> list[State]:
        return [neigh for neigh in raw_neighbors(state) if inside(neigh) and (neigh not in data.forests)]
    def heuristic(x: Coord) -> Score:
        return 141 * 141 - manhattan(x, data.end)

    return astar(start, goal, cost, neighbors, heuristic)

def condense(data: Data) -> int:
    def goal(loc: Coord) -> bool:
        return loc == data.end
    def neighbors(loc: Coord) -> list[Coord]:
        y, x = loc
        return [(y-1, x), (y+1, x), (y, x+1), (y, x-1)]

    y0, x0 = data.start
              # loc,  length,  prev,    origin
    paths = [((y0+1, x0), 1, data.start, data.start)]
    graph = {} # dictionary from node, to sets of elements (node, cost)
    while paths:
        (loc, steps, prev, origin) = paths.pop()
        # neighbors who are not prev and not forests
        neighs = [neigh for neigh in neighbors(loc) if (neigh != prev) and (neigh not in data.forests)]
        if len(neighs) > 1:
            # at a branch point
            if (origin, steps) not in graph.get(loc, set()):
                graph.setdefault(loc, set()).add((origin, steps))
                graph.setdefault(origin, set()).add((loc, steps))
                for neigh in neighs:
                    paths.append((neigh, 1, loc, loc))
        elif len(neighs) == 1:
            # only one neighbor, just keep going
            if goal(neighs[0]):
                # unless it the goal
                graph.setdefault(neighs[0], set()).add((origin, steps+1))
                graph.setdefault(origin, set()).add((neighs[0], steps+1))
            elif neighs[0] == data.start:
                pass
            else:
                # normal walk
                paths.append((neighs[0], steps+1, loc, origin))

    return graph


def part2(data, condensed_data):
    def goal(loc):
        return loc == data.end
    def neighbors(loc):
        return condensed_data.get(loc, [])

    paths = [(data.start, 0, {data.start})]
    lengths = []
    while paths:
        (loc, steps, seen) = paths.pop()
        for neigh, cost in neighbors(loc):
            if goal(neigh):
                lengths.append(steps+cost)
            elif neigh not in seen:
                seen = seen.copy()
                seen.add(loc)
                paths.append((neigh, steps+cost, seen))

    return max(lengths)

print("Condensing test example...")
condensed_test_data = condense(test_data)
print("Checking test example...")
assert  part2(test_data, condensed_test_data) == 154

print("Condensing...")
condensed_data = condense(data)
print("Running...")
ans2 = part2(data, condensed_data)



if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
