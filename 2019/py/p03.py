import math
from utils import data19

tests = [
    ("R8,U5,L5,D3\nU7,R6,D4,L4", 6),
    ("R75,D30,R83,U83,L12,D49,R71,U7,L72\nU62,R66,U55,R34,D71,R55,D58,R83", 159),
    (
        "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51\nU98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
        135,
    ),
]


def visit(path):
    loc = (0, 0)
    visited = set()
    for part in path.split(","):
        if part[0] == "R":
            for i in range(int(part[1:])):
                loc = (loc[0] + 1, loc[1])
                visited.add(loc)
        elif part[0] == "L":
            for i in range(int(part[1:])):
                loc = (loc[0] - 1, loc[1])
                visited.add(loc)
        elif part[0] == "D":
            for i in range(int(part[1:])):
                loc = (loc[0], loc[1] - 1)
                visited.add(loc)
        elif part[0] == "U":
            for i in range(int(part[1:])):
                loc = (loc[0], loc[1] + 1)
                visited.add(loc)
    return visited


def process(inp):
    visited = set()
    one, two = inp.strip().split("\n")

    firstpart = visit(one)
    secondpart = visit(two)

    distance = math.inf
    for loc in firstpart.intersection(secondpart):
        newdist = abs(loc[0]) + abs(loc[1])
        if newdist < distance:
            distance = newdist
    return distance


def visit2(path):
    loc = (0, 0)
    step = 0
    visited = {}
    for part in path.split(","):
        if part[0] == "R":
            for i in range(int(part[1:])):
                loc = (loc[0] + 1, loc[1])
                step += 1
                visited[loc] = step
        elif part[0] == "L":
            for i in range(int(part[1:])):
                loc = (loc[0] - 1, loc[1])
                step += 1
                visited[loc] = step
        elif part[0] == "D":
            for i in range(int(part[1:])):
                loc = (loc[0], loc[1] - 1)
                step += 1
                visited[loc] = step
        elif part[0] == "U":
            for i in range(int(part[1:])):
                loc = (loc[0], loc[1] + 1)
                step += 1
                visited[loc] = step
    return visited


tests2 = [
    ("R8,U5,L5,D3\nU7,R6,D4,L4", 30),
    ("R75,D30,R83,U83,L12,D49,R71,U7,L72\nU62,R66,U55,R34,D71,R55,D58,R83", 610),
    (
        "R98,U47,R26,D63,R33,U87,L62,D20,R33,U53,R51\nU98,R91,D20,R16,D67,R40,U7,R15,U6,R7",
        410,
    ),
]


def process2(inp):
    visited = set()
    one, two = inp.strip().split("\n")

    firstpart = visit2(one)
    secondpart = visit2(two)

    distance = math.inf
    for loc in firstpart:
        if loc in secondpart:
            newdist = firstpart[loc] + secondpart[loc]
            if newdist < distance:
                distance = newdist
    return distance


data = data19(3)

if __name__ == "__main__":
    for case, ans in tests:
        assert process(case) == ans

    print("Answer1: ", process(data))

    for case, ans in tests2:
        assert process2(case) == ans

    print("Answer1: ", process2(data))
