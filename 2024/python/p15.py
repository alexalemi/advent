# Advent of Code 2024 - Day 15

import copy
import dataclasses

type Coord = tuple[int, int]


with open("../input/15.txt") as f:
    raw_data = f.read()

raw_test_data = """##########
#..O..O.O#
#......O.#
#.OO..O.O#
#..O@..O.#
#O#..O...#
#O..O..O.#
#.OO.O.OO#
#....O...#
##########

<vv>^<v^>v>^vv^v>v<>v^v<v<^vv<<<^><<><>>v<vvv<>^v^>^<<<><<v<<<v^vv^v>^
vvv<<^>^v^^><<>>><>^<<><^vv^^<>vvv<>><^^v>^>vv<>v<<<<v<^v>^<^^>>>^<v<v
><>vv>v^v^<>><>>>><^^>vv>v<^^^>>v^v^<^^>v^^>v^<^v>v<>>v^v^<v>v^^<^^vv<
<<v<^>>^^^^>>>v^<>vvv^><v<<<>^^^vv^<vvv>^>v<^^^^v<>^>vvvv><>>v^<<^^^^^
^><^><>>><>^^<<^^v>>><^<v>^<vv>>v>>>^v><>^v><<<<v>>v<v<v>vvv>^<><<>^><
^>><>^v<><^vvv<^^<><v<<<<<><^v<<<><<<^^<v<^^^><^>>^<v^><<<^>>^v<v^v<v^
>^>>^v>vv>^<<^v<>><<><<v<<v><>v<^vv<<<>^^v^>^^>>><<^v>>v^v><^^>>^<>vv^
<><^^>^^^<><vvvvv^v<v<<>^v<v>v<<^><<><<><<<^^<<<^<<>><<><^^^>^^<>^>v<>
^^>vv<^v^v<vv>^<><v<^v>^^^>>>^^vvv^>vvv<>>>^<^>>>>>^<<^v>^vvv<>^<><<v>
v^^>>><<^^<>>^v^<v^vv<>v^<<>^<^v^v><^<<<><<^<v><v<>vv>>v><v^<vv<>v^<<^
"""

raw_test_data2 = """########
#..O.O.#
##@.O..#
#...O..#
#.#.O..#
#...O..#
#......#
########

<^^>>>vv<v>>v<<"""


@dataclasses.dataclass
class Data:
    walls: set[Coord]
    boxes: set[Coord]
    robot: Coord
    instructions: str
    t: int

    def score(self):
        return sum(100 * y + x for (x, y) in self.boxes)

    def __repr__(self):
        out = ""
        MAXY = max(x[1] for x in self.walls)
        MAXX = max(x[0] for x in self.walls)
        for y in range(MAXY + 1):
            for x in range(MAXX + 1):
                loc = (x, y)
                if loc in self.walls:
                    out += "#"
                elif loc in self.boxes:
                    out += "O"
                elif loc == self.robot:
                    out += "@"
                else:
                    out += "."
            out += "\n"
        out += "\n" + str(self.t) + "\n"
        out += self.instructions
        return out


def process(s: str):
    board_part, inst_part = s.split("\n\n")
    walls = set()
    boxes = set()
    robot = None
    for y, line in enumerate(board_part.splitlines()):
        for x, c in enumerate(line):
            if c == "#":
                walls.add((x, y))
            elif c == "O":
                boxes.add((x, y))
            elif c == "[":
                boxes.add((x, y))
            elif c == "@":
                robot = (x, y)

    inst = inst_part.replace("\n", "").strip()

    return Data(walls=walls, boxes=boxes, robot=robot, instructions=inst, t=0)


data = process(raw_data)
test_data = process(raw_test_data)
test_data2 = process(raw_test_data2)


def north(pos):
    x, y = pos
    return x, y - 1


def south(pos):
    x, y = pos
    return x, y + 1


def east(pos):
    x, y = pos
    return x + 1, y


def west(pos):
    x, y = pos
    return x - 1, y


instructions = {"^": north, "<": west, ">": east, "v": south}


def step(data: Data):
    inst = data.instructions[data.t]
    data.t += 1
    move = instructions[inst]

    new = move(data.robot)
    if new in data.walls:
        # we can't move there is a wall
        return
    if new in data.boxes:
        # There is a box in front of us, try to move it
        to_move = {new}
        while (new := move(new)) in data.boxes:
            to_move.add(new)
        if new in data.walls:
            # can't move anyone
            return
        new_boxes = {move(x) for x in to_move}
        data.boxes -= to_move
        data.boxes |= new_boxes
        data.robot = move(data.robot)
    else:
        # otherwise move
        data.robot = new


def run(data):
    while data.t < len(data.instructions):
        step(data)


def part1(data) -> int:
    data = copy.deepcopy(data)
    run(data)
    return data.score()


assert part1(test_data2) == 2028, "Failed part 1 small test"
assert part1(test_data) == 10092, "Failed part 1 test"
ans1 = part1(data)


## Part 2


def widen(s: str):
    return s.replace("#", "##").replace("O", "[]").replace(".", "..").replace("@", "@.")


@dataclasses.dataclass
class WideData:
    walls: set[Coord]
    boxes: set[Coord]
    left_boxes: set[Coord]
    robot: Coord
    instructions: str
    t: int

    def score(self):
        return sum(100 * y + x for (x, y) in self.left_boxes)

    def __repr__(self):
        out = ""
        MAXY = max(x[1] for x in self.walls)
        MAXX = max(x[0] for x in self.walls)
        for y in range(MAXY + 1):
            for x in range(MAXX + 1):
                loc = (x, y)
                if loc in self.walls:
                    out += "#"
                elif loc in self.left_boxes:
                    out += "["
                elif loc in self.boxes:
                    out += "]"
                elif loc == self.robot:
                    out += "@"
                else:
                    out += "."
            out += "\n"
        out += "\n" + str(self.t) + "\n"
        out += self.instructions
        return out


def wide_process(s: str):
    board_part, inst_part = s.split("\n\n")
    walls = set()
    boxes = set()
    left_boxes = set()
    robot = None
    for y, line in enumerate(board_part.splitlines()):
        for x, c in enumerate(line):
            if c == "#":
                walls.add((x, y))
            elif c == "O":
                boxes.add((x, y))
            elif c == "[":
                boxes.add((x, y))
                left_boxes.add((x, y))
            elif c == "]":
                boxes.add((x, y))
            elif c == "@":
                robot = (x, y)

    inst = inst_part.replace("\n", "").strip()

    return WideData(
        walls=walls,
        boxes=boxes,
        left_boxes=left_boxes,
        robot=robot,
        instructions=inst,
        t=0,
    )


raw_test_data3 = """#######
#...#.#
#.....#
#..OO@#
#..O..#
#.....#
#######

<vv<<^^<<^^"""

new_test_data = wide_process(widen(raw_test_data))
new_test_data3 = wide_process(widen(raw_test_data3))
new_data = wide_process(widen(raw_data))


def step2(data: Data):
    inst = data.instructions[data.t]
    data.t += 1
    move = instructions[inst]

    def pair(loc):
        assert loc in data.boxes
        if loc in data.left_boxes:
            return east(loc)
        else:
            return west(loc)

    new = move(data.robot)
    if new in data.walls:
        # we can't move there is a wall
        return
    if new in data.boxes:
        # There is a box in front of us, try to move it
        to_move = {new, pair(new)}
        while touched := (({move(x) for x in to_move} & data.boxes) - to_move):
            to_move |= touched
            to_move |= {pair(x) for x in touched}

        moves = {move(x) for x in to_move}
        left_to_move = to_move & data.left_boxes
        left_moves = {move(x) for x in left_to_move}
        if moves & data.walls:
            return

        data.boxes -= to_move
        data.boxes |= moves
        data.left_boxes -= left_to_move
        data.left_boxes |= left_moves
        data.robot = move(data.robot)
    else:
        # otherwise move
        data.robot = new


def run2(data):
    while data.t < len(data.instructions):
        step2(data)


def part2(data) -> int:
    data = copy.deepcopy(data)
    run2(data)
    return data.score()


assert part2(new_test_data) == 9021, "Failed part 2 test"
ans2 = part2(new_data)


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
