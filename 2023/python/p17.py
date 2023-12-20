# Advent of Code - Day 17

import numpy as np
import itertools
import heapq
from collections.abc import Generator, Callable
from typing import Any
from functools import partial

test_string = """2413432311323
3215453535623
3255245654254
3446585845452
4546657867536
1438598798454
4457876987766
3637877979653
4654967986887
4564679986453
1224686865563
2546548887735
4322674655533"""

type Board = Any
type Cost = int


def process(s: str) -> Board:
    return np.array([[int(c) for c in line] for line in s.splitlines()])


with open("../input/17.txt") as f:
    data = process(f.read())

test_data = process(test_string)

type Loc = tuple[int, int]
type Dir = str
type State = tuple[Loc, Dir]


def up(loc: Loc) -> Loc:
    (y, x) = loc
    return (y - 1, x)


def down(loc: Loc) -> Loc:
    (y, x) = loc
    return (y + 1, x)


def right(loc: Loc) -> Loc:
    (y, x) = loc
    return (y, x + 1)


def left(loc: Loc) -> Loc:
    (y, x) = loc
    return (y, x - 1)


name_to_func = {"left": left, "right": right, "down": down, "up": up}

start: State = ((0, 0), "none")


def neighbors_and_cost(
    board: Board, minimum: int, maximum: int, state: State
) -> Generator[tuple[State, Cost], None, None]:
    (loc, direction) = state
    (Y, X) = board.shape

    def inside(z: Loc) -> bool:
        return (0 <= z[0] < Y) and (0 <= z[1] < X)

    match direction:
        case "left" | "right":
            dirs = ["up", "down"]
        case "up" | "down":
            dirs = ["right", "left"]
        case "none":
            dirs = ["right", "down"]
    for d in dirs:
        x = loc
        cost = 0
        f = name_to_func[d]
        for i in range(maximum):
            x = f(x)
            if inside(x):
                cost += board[x]
                if (i + 1) >= minimum:
                    yield (x, d), cost
            else:
                break


def reconstruct_path(came_from: dict[State, State], end: State) -> list[State]:
    current = end
    path = []
    while current in came_from:
        path.append(current)
        current = came_from[current]
    path.reverse()
    return path


def astar(
    start: State,
    goal: Callable[[State], bool],
    neighbors_and_cost: Callable[[State], Generator[tuple[State, Cost]]],
    heuristic: Callable[[State], Cost] = lambda x: 0.0,
) -> tuple[dict[State, State], dict[State, Cost]]:
    frontier = []
    counter = itertools.count()
    heapq.heappush(frontier, (0, -next(counter), start))
    came_from = {}
    cost_so_far = {}
    came_from[start] = None
    cost_so_far[start] = 0

    while frontier:
        _, _, current = heapq.heappop(frontier)
        if goal(current):
            return cost_so_far[current]
            # return reconstruct_path(came_from, current)
        for n, c in neighbors_and_cost(current):
            new_cost = cost_so_far[current] + c
            if n not in cost_so_far or new_cost < cost_so_far[n]:
                cost_so_far[n] = new_cost
                priority = new_cost + heuristic(n)
                heapq.heappush(frontier, (priority, -next(counter), n))
                came_from[n] = current
    return None


def manhattan(a: Loc, b: Loc) -> Cost:
    (y1, x1) = a
    (y2, x2) = b
    return abs(y1 - y2) + abs(x1 - x2)


def solver(board: Board, minimum: int = 1, maximum: int = 3) -> int:
    (Y, X) = board.shape

    def goal(x: State) -> bool:
        (loc, _) = x
        return (loc[0] == (Y - 1)) and (loc[1] == (X - 1))

    def heuristic(x: State) -> Cost:
        (loc, _) = x
        return manhattan(loc, (Y - 1, X - 1))

    my_neighbors_and_cost = partial(neighbors_and_cost, board, minimum, maximum)
    return astar(start, goal, my_neighbors_and_cost, heuristic)


part1 = partial(solver, minimum=1, maximum=3)

assert 102 == part1(test_data)
ans1 = part1(data)
assert ans1 == 1065

## Part 2

part2 = partial(solver, minimum=4, maximum=10)
assert 94 == part2(test_data), f"Instead found {part2(test_data)=}"
ans2 = part2(data)
assert ans2 == 1249


if __name__ == "__main__":
    print("Answer1: ", ans1)
    print("Answer2: ", ans2)
