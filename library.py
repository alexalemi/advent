from typing import Dict, List, Any, Callable, Tuple, Mapping, NamedTuple, Sequence
import heapq

Location = Any
Score = Any
Value = Any


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
    symbol: Callable[[Value], chr],
    make=lambda x, y: x + 1j * y,
):
    for y in range(bounds.ymin, bounds.ymax + 1):
        for x in range(bounds.xmin, bounds.xmax + 1):
            print(symbol(world.get(make(x, y))), end="")
        print()


def reconstruct_path(came_from: Dict[Location, Location], start: Location,
                     goal: Location) -> List[Location]:
    current = goal
    path = []
    while current != start:
        path.append(current)
        current = came_from[current]
    path.append(start)
    path.reverse()
    return path


def astar(
    start: Location,
    goal: Callable[[Location], bool],
    cost: Callable[[Location, Location], Score],
    neighbors: Callable[[Location], List[Location]],
    heuristic: Callable[[Location], Score] = lambda x: 0.0,
) -> Tuple[Dict[Location, Location], Dict[Location, Score]]:
    frontier = []
    heapq.heappush(frontier, (0.0, start))
    came_from = {}
    cost_so_far = {}
    came_from[start] = None
    cost_so_far[start] = 0

    while frontier:
        _, current = heapq.heappop(frontier)
        if goal(current):
            break
        for next in neighbors(current):
            new_cost = cost_so_far[current] + cost(current, next)
            if next not in cost_so_far or new_cost < cost_so_far[next]:
                cost_so_far[next] = new_cost
                priority = new_cost + heuristic(next)
                heapq.heappush(frontier, (priority, next))
                came_from[next] = current

    return came_from, cost_so_far
