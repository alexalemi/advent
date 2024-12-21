# Advent of Code - Day 21

from collections import defaultdict
from functools import partial, cache
from typing import Iterator
import math

type Code = str
type Instructions = str
type Coord = tuple[int, int]
type Key = str
type KeyMap = dict[Key, Coord]

with open("../input/21.txt") as f:
    data = f.read().splitlines()

test_data = """029A
980A
179A
456A
379A""".splitlines()

## Logic

door_keypad = """789
456
123
 0A"""

robot_keypad = """ ^A
<v>"""


def pad_to_map(keypad: str) -> KeyMap:
    """Converts a pad string to a KeyMap."""
    keys = {}
    for y, line in enumerate(keypad.splitlines()):
        for x, c in enumerate(line):
            if c != " ":
                keys[c] = (x, y)
    return keys


door_keymap = pad_to_map(door_keypad)
robot_keymap = pad_to_map(robot_keypad)


def north(loc: Coord) -> Coord:
    (x, y) = loc
    return (x, y - 1)


def south(loc: Coord) -> Coord:
    (x, y) = loc
    return (x, y + 1)


def east(loc: Coord) -> Coord:
    (x, y) = loc
    return (x + 1, y)


def west(loc: Coord) -> Coord:
    (x, y) = loc
    return (x - 1, y)


# Make sure to optimize the order
directions = {"^": north, ">": east, "v": south, "<": west}


def neighbors(loc: Coord) -> Iterator[tuple[Key, Coord]]:
    for name, fn in directions.items():
        yield (name, fn(loc))


def invert(x: dict) -> dict:
    return {val: key for key, val in x.items()}


COSTS = {"<": 4, "v": 3, "^": 2, ">": 1}


def is_monotonic(s: str) -> bool:
    return (s == "".join(sorted(s, key=COSTS.get))) or (
        s == "".join(sorted(s, key=COSTS.get, reverse=True))
    )


def all_shortest_paths(keymap: KeyMap, start_key: Key) -> dict[Key, set[Instructions]]:
    """Given a KeyMap and starting key, return the shortest paths to all other keys. Dijkstras."""
    frontier = [(start_key, "")]
    ikeymap = invert(keymap)
    seen = set()
    shortest_paths = defaultdict(set)
    while frontier:
        key, prev = frontier.pop(0)  # use breadth-first search
        loc = keymap[key]
        for move, new_loc in neighbors(loc):
            new_key = ikeymap.get(new_loc)
            if new_key and (new_key not in seen):
                frontier.append((new_key, prev + move))
        seen.add(key)
        # early optimization, only consider monotonic paths.
        if is_monotonic(prev):
            shortest_paths[key].add(prev)

    return shortest_paths


def best_paths(keymap: KeyMap) -> dict[tuple[Key, Key], set[Instructions]]:
    """Given a keymap, returns the shortest paths between all pairs of keys."""
    paths = defaultdict(set)
    for start_key, start_loc in keymap.items():
        for end_key, instructions in all_shortest_paths(keymap, start_key).items():
            paths[(start_key, end_key)] |= instructions
    return paths


all_door_paths = best_paths(door_keymap)
all_robot_paths = best_paths(robot_keymap)

## Part 1


def expand_instructions(
    paths: dict[tuple[Key, Key], set[Instructions]], inp: Code | Instructions
) -> set[Instructions]:
    loc = "A"
    outputs = {""}
    for c in inp:
        outputs = {prev + new + "A" for prev in outputs for new in paths[(loc, c)]}
        loc = c
    return outputs


def prune(instructions: set[Instructions]) -> set[Instructions]:
    """Prune instruction sets longer than the minimum one."""
    shortest_length = min(map(len, instructions))
    return {x for x in instructions if len(x) == shortest_length}


def door_path(code: Code) -> Instructions:
    return prune(expand_instructions(all_door_paths, code))


def robot_path(code: Code) -> Instructions:
    return prune(expand_instructions(all_robot_paths, code))


def first(instructions: set[Instructions]) -> Instructions:
    return next(iter(instructions))


assert len(first(door_path("029A"))) == len(
    "<A^A^^>AvvvA"
), "Failed to find shortest path on door code 029A!"
assert door_path("029A") < {"<A^A>^^AvvvA", "<A^A^>^AvvvA", "<A^A^^>AvvvA"}


def comp(*fns):
    def comp_fn(x):
        for fn in reversed(fns):
            x = fn(x)
        return x

    return comp_fn


def shortest_sequence(code: Code) -> int:
    door_robot = door_path(code)
    second_robot_path = prune(set.union(*map(robot_path, door_robot)))
    return min(map(comp(len, first, robot_path), second_robot_path))


sequence_tests = """029A: <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
980A: <v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
179A: <v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
456A: <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
379A: <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A"""

for line in sequence_tests.splitlines():
    code, found = line.split(": ")
    best = shortest_sequence(code)
    exp = len(found)
    assert (
        best == exp
    ), f"Failed to find a short sequence on code {code}, wanted {exp}, got {best}!"


def numeric_part(code: Code) -> int:
    return int(code[:-1])


def complexity(code: Code) -> int:
    return numeric_part(code) * shortest_sequence(code)


def part1(codes: list[Code]) -> int:
    return sum(complexity(code) for code in codes)


assert (
    part1(test_data) == 126384
), f"Failed part 1 test! Wanted 126384, got {part1(test_data)}"
ans1 = part1(data)
print(f"Answer 1: {ans1}")

## Part 2 Again

## Let's start at a high level, what sort of thing are we going to be able to do.

### Part 2
print("ENTERING PART 2")

# We aren't going to be able to track much, so let's focus on just the button presses.


@cache
def optimal_moves(start: Key, end: Key, level: int) -> int:
    """Gives the optimal number of button presses to get the robot to move from start to end."""
    if level == 0:
        return 1
    else:
        return min(
            score_robot_path(path + "A", level=level - 1)
            for path in all_robot_paths[(start, end)]
        )


def score_robot_path(instructions: Instructions, level: int) -> int:
    loc = "A"
    total = 0
    for button in instructions:
        total += optimal_moves(loc, button, level)
        loc = button
    return total


def shortest_sequence(code: Code, levels: int = 2) -> int:
    return min(
        score_robot_path(instructions, level=levels) for instructions in door_path(code)
    )


assert shortest_sequence("029A", 0) == 12, "Failed 029A 0"
assert shortest_sequence("029A", 1) == 28, "Failed 029A 1"
assert shortest_sequence("029A", 2) == 68, "Failed 029A 2"

# Test cases
for line in sequence_tests.splitlines():
    code, found = line.split(": ")
    best = shortest_sequence(code)
    exp = len(found)
    assert (
        best == exp
    ), f"Failed to find a short sequence on new code {code}, wanted {exp}, got {best}!"


def numeric_part(code: Code) -> int:
    return int(code[:-1])


def complexity(code: Code, levels: int = 2) -> int:
    return numeric_part(code) * shortest_sequence(code, levels=levels)


def part2(codes: list[Code], levels=25) -> int:
    return sum(complexity(code, levels=levels) for code in codes)


assert (
    part2(test_data, 2) == 126384
), f"Failed part 1 test! Wanted 126384, got {part1(test_data)}"
assert part2(data, 2) == 177814, f"Failed part 1, redo!"

ans2 = part2(data)


## Main

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
