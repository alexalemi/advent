from tqdm import tqdm
from typing import NamedTuple, Generator
from collections import deque, defaultdict
from itertools import combinations_with_replacement
from functools import reduce
from heapq import heappop, heappush, heapify
from concurrent.futures import ProcessPoolExecutor

with open("../input/10.txt") as f:
    data_string = f.read()

test_string = """[.##.] (3) (1,3) (2) (2,3) (0,2) (0,1) {3,5,4,7}
[...#.] (0,2,3,4) (2,3) (0,4) (0,1,2) (1,2,3,4) {7,5,12,7,2}
[.###.#] (0,1,2,3,4) (0,3,4) (0,1,2,4,5) (1,2) {10,11,11,5,10,5}"""


Joltage = tuple[int]
Button = frozenset[int]


class Machine(NamedTuple):
    target: tuple[bool]
    buttons: tuple[Button]
    joltages: Joltage


def process(s: str) -> list[Machine]:
    machines = []
    for line in s.splitlines():
        parts = line.split()
        lights, *buttons, joltages = parts

        machines.append(
            Machine(
                target=tuple(x == "#" for x in lights[1:-1]),
                buttons=tuple(
                    frozenset(int(x) for x in button[1:-1].split(","))
                    for button in buttons
                ),
                joltages=tuple(map(int, joltages[1:-1].split(","))),
            )
        )
    return machines


data = process(data_string)
test_data = process(test_string)


def toggle(lights: list[bool], button: Button) -> list[bool]:
    lights = list(lights)
    for pk in button:
        lights[pk] ^= True
    return tuple(lights)


def fewest_presses(machine: Machine) -> int:
    """Determine the minimum number of presses needed to reach target."""
    target = machine.target
    lights = tuple(False for _ in target)
    depth = 0
    frontier = deque()
    seen = set(lights)

    while lights != target:
        seen.add(lights)
        for button in machine.buttons:
            new_lights = toggle(lights, button)
            if new_lights not in seen:
                frontier.append((new_lights, depth + 1))
        lights, depth = frontier.popleft()
    return depth


def part1(data: list[Machine]) -> int:
    with ProcessPoolExecutor() as executor:
        return sum(tqdm(executor.map(fewest_presses, data), total=len(data)))


assert part1(test_data) == 7, "Failed part 1 test"
ans1 = part1(data)
print(f"Answer 1: {ans1}")


def press(joltage: Joltage, button: Button, n: int = 1) -> Joltage:
    joltage = list(joltage)
    for pk in button:
        joltage[pk] -= n
    return tuple(joltage)


def neighbors(
    joltage: Joltage, buttons: set[Button]
) -> Generator[tuple[int, Joltage], None, None]:
    """Try to find the light that has the fewest buttons interacting with it."""

    def ok(button: Button) -> bool:
        return all(x >= 0 for x in press(joltage, button))

    touched = defaultdict(set)
    for button in buttons:
        if ok(button):
            for light in button:
                touched[light].add(button)

    def key(item: tuple[int, set[Button]]):
        slot, buttons = item
        return (len(buttons), -joltage[slot])

    if not touched:
        return
    slot, buttons = min(touched.items(), key=key)
    # print(f"{joltage=}, {slot=}, {buttons=}")
    presses = joltage[slot]

    for pushes in combinations_with_replacement(buttons, r=presses):
        final = reduce(press, pushes, joltage)
        if all(x >= 0 for x in final):
            yield presses, final


def heuristic(joltage: Joltage) -> int:
    return max(joltage)


TOO_LARGE = 100_000_000
SMALLER = 1_000_000


def joltage_presses(machine: Machine) -> int:
    """Determine the minimum number of presses needed to reach target."""

    joltage = machine.joltages
    buttons = machine.buttons

    frontier = []
    heappush(frontier, (0, joltage))
    presses = {joltage: 0}

    while frontier:
        if len(frontier) > TOO_LARGE:
            print("compressing...")
            frontier = heapify(sorted(frontier)[:SMALLER])
        _, joltage = heappop(frontier)
        if not any(joltage):
            return presses[joltage]

        for n, neigh in neighbors(joltage, buttons):
            new_cost = presses[joltage] + n
            if neigh not in presses or new_cost < presses[neigh]:
                presses[neigh] = new_cost
                priority = new_cost + heuristic(neigh)
                heappush(frontier, (priority, neigh))


def solve_linprob(machine: Machine) -> int:
    import numpy as np
    import scipy.optimize as spopt

    n = len(machine.joltages)
    m = len(machine.buttons)

    mat = np.zeros((n, m), dtype="int")
    for i, button in enumerate(machine.buttons):
        for light in button:
            mat[light, i] = 1

    c = np.ones(m)
    b = np.array(machine.joltages)
    res = spopt.linprog(c, A_eq=mat, b_eq=b, integrality=1)
    assert res.success
    return int(round(res.fun))


def part2(data: list[Machine]) -> int:
    with ProcessPoolExecutor() as executor:
        return sum(tqdm(executor.map(joltage_presses, data), total=len(data)))


assert part2(test_data) == 33, "Failed part 2 test"
ans2 = part2(data)
print(f"Answer 2: {ans2}")
assert ans2 == 18559
