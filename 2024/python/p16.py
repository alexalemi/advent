# Advent of Code 2024 - Day 16

import itertools
import heapq
import math
from collections import defaultdict
from typing import Sequence, Callable
import dataclasses
import maps

type Coord = tuple[int, int]

with open("../input/16.txt") as f:
    raw_data = f.read()

raw_test_data = """###############
#.......#....E#
#.#.###.#.###.#
#.....#.#...#.#
#.###.#####.#.#
#.#.#.......#.#
#.#.#####.###.#
#...........#.#
###.#.#####.#.#
#...#.....#.#.#
#.#.#.###.#.#.#
#.....#...#.#.#
#.###.#.#.#.#.#
#S..#.....#...#
###############"""

raw_test_data2 = """#################
#...#...#...#..E#
#.#.#.#.#.#.#.#.#
#.#.#.#...#...#.#
#.#.#.#.###.#.#.#
#...#.#.#.....#.#
#.#.#.#.#.#####.#
#.#...#.#.#.....#
#.#.#####.#.###.#
#.#.#.......#...#
#.#.###.#####.###
#.#.#...#.....#.#
#.#.#.#####.###.#
#.#.#.........#.#
#.#.#.#########.#
#S#.............#
#################"""


@dataclasses.dataclass
class Maze:
    walls: set[Coord]
    start: Coord
    end: Coord


def process(s: str) -> Maze:
    walls = set()
    start = None
    end = None
    for y, line in enumerate(s.strip().splitlines()):
        for x, c in enumerate(line):
            match c:
                case "#":
                    walls.add((x, y))
                case "S":
                    start = (x, y)
                case "E":
                    end = (x, y)
    return Maze(walls, start, end)


data = process(raw_data)
test_data = process(raw_test_data)
test_data2 = process(raw_test_data2)


type Direction = Callable[[Coord], Coord]


def north(loc: Coord) -> Coord:
    (x, y) = loc
    return (x, y - 1)


def south(loc: Coord) -> Coord:
    (x, y) = loc
    return (x, y + 1)


def west(loc: Coord) -> Coord:
    (x, y) = loc
    return (x - 1, y)


def east(loc: Coord) -> Coord:
    (x, y) = loc
    return (x + 1, y)


def turn_right(direction: Direction) -> Direction:
    if direction == north:
        return east
    elif direction == east:
        return south
    elif direction == south:
        return west
    elif direction == west:
        return north


def turn_left(direction: Direction) -> Direction:
    if direction == north:
        return west
    elif direction == west:
        return south
    elif direction == south:
        return east
    elif direction == east:
        return north


@dataclasses.dataclass(frozen=True)
class State:
    location: Coord
    direction: Direction


def solve(maze: Maze) -> tuple[list[State], int] | None:
    start = State(maze.start, east)

    def goal(state: State) -> bool:
        return state.location == maze.end

    def cost(state1: State, state2: State) -> int:
        if state1.location != state2.location:
            return 1
        elif state1.direction != state2.direction:
            return 1000
        return math.inf

    def neighbors(state: State) -> list[State]:
        if (new := state.direction(state.location)) not in maze.walls:
            yield State(new, state.direction)
        yield State(state.location, turn_right(state.direction))
        yield State(state.location, turn_left(state.direction))

    def heuristic(state: State) -> int:
        (x, y) = state.location
        (ex, ey) = maze.end
        return abs(x - ex) + abs(y - ey)

    return maps.astar(start, goal, cost, neighbors, heuristic)


def part1(maze: Maze) -> int:
    _, best_score = solve(maze)
    return best_score


assert part1(test_data) == 7036, "Failed part 1 test 1"
assert part1(test_data2) == 11048, "Faield part 1 test 2"
ans1 = part1(data)
assert ans1 == 99448, "Failed part 1"

## Part 2


def reconstruct_paths(
    came_from: dict[State, State], end: State
) -> Sequence[Sequence[State]]:
    current = end
    frontier = [(end,)]
    paths = set()
    while frontier:
        current_path = frontier.pop()
        current = current_path[-1]
        neighs = came_from.get(current)
        if neighs is None:
            paths.add(current_path)
        else:
            for neigh in neighs:
                frontier.append(current_path + (neigh,))
    return {tuple(reversed(path)) for path in paths}


def dijkstra(
    start: State,
    goal: Callable[[State], bool],
    cost: Callable[[State, State], int],
    neighbors: Callable[[State], Sequence[State]],
) -> tuple[Sequence[State], int] | None:
    frontier = []
    counter = itertools.count()
    heapq.heappush(frontier, (0, -next(counter), start))
    came_from = defaultdict(set)
    cost_so_far = {}
    cost_so_far[start] = 0

    while frontier:
        _, _, current = heapq.heappop(frontier)
        if goal(current):
            return reconstruct_paths(came_from, current), cost_so_far[current]
        for n in neighbors(current):
            new_cost = cost_so_far[current] + cost(current, n)
            if n not in cost_so_far or new_cost < cost_so_far[n]:
                cost_so_far[n] = new_cost
                priority = new_cost
                heapq.heappush(frontier, (priority, -next(counter), n))
                came_from[n].add(current)
            elif new_cost == cost_so_far[n]:
                priority = new_cost
                heapq.heappush(frontier, (priority, -next(counter), n))
                came_from[n].add(current)
    return None


def solve_all(maze: Maze) -> tuple[list[State], int] | None:
    start = State(maze.start, east)

    def goal(state: State) -> bool:
        return state.location == maze.end

    def cost(state1: State, state2: State) -> int:
        if state1.location != state2.location:
            return 1
        elif state1.direction != state2.direction:
            return 1000
        return math.inf

    def neighbors(state: State) -> list[State]:
        if (new := state.direction(state.location)) not in maze.walls:
            yield State(new, state.direction)
        yield State(state.location, turn_right(state.direction))
        yield State(state.location, turn_left(state.direction))

    return dijkstra(start, goal, cost, neighbors)


def part2(maze: Maze) -> int:
    best_paths, _ = solve_all(maze)
    return len(set.union(*tuple({x.location for x in path} for path in best_paths)))


assert part2(test_data) == 45, "Failed part 2 test 1"
assert part2(test_data2) == 64, "Faield part 2 test 2"
ans2 = part2(data)
# assert ans2 == 99448, "Failed part 1"


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
