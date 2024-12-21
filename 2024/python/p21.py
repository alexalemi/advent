# Advent of Code - Day 21

from functools import partial
from typing import Iterator

type Code = str
type Instructions = str
type Coord = tuple[int, int]
type Key = str
type KeyMap = dict[Key, Coord]

with open('../input/21.txt') as f:
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
            if c != ' ':
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
    return {val:key for key, val in x.items()}

def shortest_paths(keymap: KeyMap, start_key: Key) -> dict[Key, Instructions]:
    """Given a KeyMap and starting key, return the shortest paths to all other keys. Dijkstras."""
    frontier = [(start_key, "")]
    ikeymap = invert(keymap)
    seen = set()
    shortest_paths = {}
    while frontier:
        key, prev = frontier.pop(0) # use breadth-first search
        loc = keymap[key]
        for move, new_loc in neighbors(loc):
            new_key = ikeymap.get(new_loc)
            if new_key and (new_key not in seen):
                frontier.append((new_key, prev + move))
        seen.add(key)
        if key not in shortest_paths:
            shortest_paths[key] = prev

    return shortest_paths


def best_paths(keymap: KeyMap) -> dict[tuple[Key, Key], Instructions]:
    """Given a keymap, returns the shortest paths between all pairs of keys."""
    paths = {}
    for start_key, start_loc in keymap.items():
        for end_key, instructions in shortest_paths(keymap, start_key).items():
            paths[(start_key, end_key)] = instructions
    return paths

door_paths = best_paths(door_keymap)
robot_paths = best_paths(robot_keymap) 
#!! Fix the issue with double presses

## Part 1

COSTS = {"<": 4, "v": 3, "^": 2, ">": 1}

def expand_instructions(paths: dict[tuple[Key, Key], Instructions], inp: Code | Instructions) -> Instructions:
    output = ""
    loc = 'A'
    for c in inp:
        output += paths[(loc, c)]
        output += 'A'
        loc = c
    return output

def door_path(code: Code) -> Instructions:
    output = ""
    loc = 'A'
    for c in code:
        new_instructions = door_paths[(loc, c)]
        keyfunc = lambda x: COSTS[x]
        output += ''.join(sorted(new_instructions, key=keyfunc, reverse=True))
        output += 'A'
        loc = c
    return output

assert len(door_path('029A')) == len("<A^A^^>AvvvA"), "Failed to find shortest path on door code 029A!"
assert door_path('029A') in {'<A^A>^^AvvvA', '<A^A^>^AvvvA', '<A^A^^>AvvvA'}

def robot_path(instructions: Instructions) -> Instructions:
    output = ""
    loc = 'A'
    for c in instructions:
        new_instructions = robot_paths[(loc, c)]
        # Given wherever we are currently, try to optimize the new segment of the path to require few movements.
        keyfunc = lambda x: (len(robot_paths[(loc, x)]), -COSTS[x])
        if loc != '<':
            output += ''.join(sorted(new_instructions, key=keyfunc))
        else:
            output += new_instructions
        output += 'A'
        loc = c
    return output

assert len(robot_path(door_path('029A'))) == len('v<<A>>^A<A>AvA<^AA>A<vAAA>^A'), "Failed to find a short sequence for telling the door robot!"

assert len(robot_path(robot_path(door_path('029A')))) == len('<vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A'), "Failed to find a short sequence for the second order robot!"

def shortest_sequence(code: Code) -> int:
    door_robot = door_path(code)
    second_robot_path = robot_path(door_robot)
    your_code = robot_path(second_robot_path)
    return len(your_code)

sequence_tests = """029A: <vA<AA>>^AvAA<^A>A<v<A>>^AvA^A<vA>^A<v<A>^A>AAvA^A<v<A>A>^AAAvA<^A>A
980A: <v<A>>^AAAvA^A<vA<AA>>^AvAA<^A>A<v<A>A>^AAAvA<^A>A<vA>^A<A>A
179A: <v<A>>^A<vA<A>>^AAvAA<^A>A<v<A>>^AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
456A: <v<A>>^AA<vA<A>>^AAvAA<^A>A<vA>^A<A>A<vA>^A<A>A<v<A>A>^AAvA<^A>A
379A: <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A"""

def execute_code(keymap: KeyMap, instructions: Instructions) -> Instructions:
    ikeymap = invert(keymap)
    key = 'A'
    loc = keymap[key]
    output = ""
    for i in instructions:
        if i == 'A':
            output += key
        else:
            loc = directions[i](loc)
            key = ikeymap[loc]
    return output

execute_doorcode = partial(execute_code, door_keymap)
execute_robotcode = partial(execute_code, robot_keymap)
            

assert execute_doorcode(door_path('379A')) == '379A', "Execute doorcode failed to roundtrip."
assert execute_robotcode(robot_path(door_path('029A'))) == door_path('029A'), "Execute robotcode failed to roundtrip."


# # Mine, BAD
# robot_path(robot_path(door_path('379A')))    
# # 'v<<A>>^AvA^Av<<A>>^AAv<A<A>>^AAvAA^<A>Av<A>^AA<A>Av<A<A>>^AAAvA^<A>A'
# # 'v<<A>>^AvA^Av<<A>>^AAv<A<A>>^AAvAA^<A>Av<A^>AA<A>Av<A<A>>^AAA<A>vA^A'
# execute_code(robot_keymap, 'v<<A>>^AvA^Av<<A>>^AAv<A<A>>^AAvAA^<A>Av<A^>AA<A>Av<A<A>>^AAA<A>vA^A') 
# # '<A>A<AAv<AA>>^AvAA^Av<AAA^>A'
# execute_code(robot_keymap, '<A>A<AAv<AA>>^AvAA^Av<AAA^>A') 
# # '^A^^<<A>>AvvvA'
# execute_code(door_keymap, '^A^^<<A>>AvvvA') 
# # '379A'
# 
# # THEIRS, GOOD
# # <v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A
# execute_robotcode('<v<A>>^AvA^A<vA<AA>>^AAvA<^A>AAvA^A<vA>^AA<A>A<v<A>A>^AAAvA<^A>A') 
# # '<A>Av<<AA>^AA>AvAA^A<vAAA>^A'
# execute_robotcode('<A>Av<<AA>^AA>AvAA^A<vAAA>^A') 
# # '^A<<^^A>>AvvvA'
# execute_doorcode('^A<<^^A>>AvvvA') 
# # '379A'
# 
# 
# # '379A'
# door_path('379A') 
# # '^A^^<<A>>AvvvA'


for line in sequence_tests.splitlines():
    code, found = line.split(': ')
    best = shortest_sequence(code)
    exp = len(found)
    assert best == exp, f"Failed to find a short sequence on code {code}, wanted {exp}, got {best}!"

def numeric_part(code: Code) -> int:
    return int(code[:-1])

def complexity(code: Code) -> int:
    return numeric_part(code) * shortest_sequence(code)

def part1(codes: list[Code]) -> int:
    return sum(complexity(code) for code in codes)


assert part1(test_data) == 126384, f"Failed part 1 test! Wanted 126384, got {part1(test_data)}"
ans1 = part1(data)

## Part 2

ans2 = None

## Main

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
