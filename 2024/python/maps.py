import itertools
import heapq
from typing import Dict, List, Any, Callable, Tuple, Mapping, NamedTuple, Sequence

Location = Any
Score = Any
Value = Any


def distance(x, y):
    return abs(x.real - y.real) + abs(x.imag + y.imag)


def neighbors(x):
    return [x + 1, x - 1, x + 1j, x - 1j]


def wrap(x):
    return (x.real, x.imag)


def unwrap(x):
    return complex(*x)


def wrapped_neighbors(x):
    return [wrap(y) for y in neighbors(unwrap(x))]


class Bounds(NamedTuple):
    xmin: int
    xmax: int
    ymin: int
    ymax: int


def get_bounds(world: Sequence[Location]):
    return Bounds(
        xmin=int(min(z.real for z in world)),
        xmax=int(max(z.real for z in world)),
        ymin=int(min(z.imag for z in world)),
        ymax=int(max(z.imag for z in world)),
    )


def render(
    bounds: Bounds,
    world: Mapping[Location, Value],
    symbol: Callable[[Value], chr] = lambda x: x,
    make=lambda x, y: x + 1j * y,
    default=" ",
):
    for y in range(bounds.ymin, bounds.ymax + 1):
        for x in range(bounds.xmin, bounds.xmax + 1):
            print(symbol(world.get(make(x, y), default)), end="")
        print()


def reconstruct_path(
    came_from: Dict[Location, Location], end: Location
) -> List[Location]:
    current = end
    path = []
    while current in came_from:
        path.append(current)
        current = came_from[current]
    path.reverse()
    return path


def astar(
    start: Location,
    goal: Callable[[Location], bool],
    cost: Callable[[Location, Location], Score],
    neighbors: Callable[[Location], Sequence[Location]],
    heuristic: Callable[[Location], Score] = lambda x: 0.0,
) -> Tuple[Sequence[Location], Score] | None:
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
            return reconstruct_path(came_from, current), cost_so_far[current]
        for n in neighbors(current):
            new_cost = cost_so_far[current] + cost(current, n)
            if n not in cost_so_far or new_cost < cost_so_far[n]:
                cost_so_far[n] = new_cost
                priority = new_cost + heuristic(n)
                heapq.heappush(frontier, (priority, -next(counter), n))
                came_from[n] = current
    return None
