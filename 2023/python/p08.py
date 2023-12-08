# Advent of Code 2023 - Day 8

import parse
import math
import itertools
from functools import partial
from typing import NamedTuple

with open("../input/08.txt") as f:
    data_string = f.read()

test_string = """RL

AAA = (BBB, CCC)
BBB = (DDD, EEE)
CCC = (ZZZ, GGG)
DDD = (DDD, DDD)
EEE = (EEE, EEE)
GGG = (GGG, GGG)
ZZZ = (ZZZ, ZZZ)"""

type Instructions = list[str]
type Loc = str


class Node(NamedTuple):
    left: Loc
    right: Loc


type Network = dict[Loc, Node]
type Data = tuple[Instructions, Network]


def process_node(s: str) -> tuple[Loc, Node]:
    result = parse.parse("{loc} = ({left}, {right})", s)
    return result["loc"], Node(result["left"], result["right"])


def process(s: str) -> Data:
    inst_str, network_str = s.split("\n\n")
    instructions = list(inst_str.strip())
    network = dict(map(process_node, network_str.splitlines()))
    return (instructions, network)


test_data = process(test_string)
data = process(data_string)


def step(network: Network, loc: Loc, direction: str) -> Loc:
    node = network[loc]
    return node.left if direction == "L" else node.right


def transit_time(data: Data, start: Loc) -> int:
    instructions_list, network = data
    instructions = itertools.cycle(instructions_list)
    loc = start
    t = 0
    while not loc[-1] == "Z":
        loc = step(network, loc, next(instructions))
        t += 1
    return t


def part1(data: Data) -> int:
    return transit_time(data, "AAA")


assert part1(test_data) == 2, "Failed part1 test!"
ans1 = part1(data)
assert ans1 == 20659, "Wrong answer for part 1!"

## Part 2

test_string_2 = """LR

11A = (11B, XXX)
11B = (XXX, 11Z)
11Z = (11B, XXX)
22A = (22B, XXX)
22B = (22C, 22C)
22C = (22Z, 22Z)
22Z = (22B, 22B)
XXX = (XXX, XXX)"""

test_data_2 = process(test_string_2)


def part2(data: Data) -> int:
    instructions_list, network = data
    a_keys = [k for k in network if k[-1] == "A"]
    transits = map(partial(transit_time, data), a_keys)
    return math.lcm(*transits)


assert part2(test_data_2) == 6
ans2 = part2(data)
assert ans2 == 15690466351717

if __name__ == "__main__":
    print("Answer1:", ans1)
    print("Answer2:", ans2)
