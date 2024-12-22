from typing import Tuple, NamedTuple, List
import re

test1 = """position=< 9,  1> velocity=< 0,  2>
position=< 7,  0> velocity=<-1,  0>
position=< 3, -2> velocity=<-1,  1>
position=< 6, 10> velocity=<-2, -1>
position=< 2, -4> velocity=< 2,  2>
position=<-6, 10> velocity=< 2, -2>
position=< 1,  8> velocity=< 1, -1>
position=< 1,  7> velocity=< 1,  0>
position=<-3, 11> velocity=< 1, -2>
position=< 7,  6> velocity=<-1, -1>
position=<-2,  3> velocity=< 1,  0>
position=<-4,  3> velocity=< 2,  0>
position=<10, -3> velocity=<-1,  1>
position=< 5, 11> velocity=< 1, -2>
position=< 4,  7> velocity=< 0, -1>
position=< 8, -2> velocity=< 0,  1>
position=<15,  0> velocity=<-2,  0>
position=< 1,  6> velocity=< 1,  0>
position=< 8,  9> velocity=< 0, -1>
position=< 3,  3> velocity=<-1,  1>
position=< 0,  5> velocity=< 0, -1>
position=<-2,  2> velocity=< 2,  0>
position=< 5, -2> velocity=< 1,  2>
position=< 1,  4> velocity=< 2,  1>
position=<-2,  7> velocity=< 2, -2>
position=< 3,  6> velocity=<-1, -1>
position=< 5,  0> velocity=< 1,  0>
position=<-6,  0> velocity=< 2,  0>
position=< 5,  9> velocity=< 1, -2>
position=<14,  7> velocity=<-2,  0>
position=<-3,  6> velocity=< 2, -1>"""

with open("../input/10.txt") as f:
    data = f.readlines()

regex = re.compile(r"position=<\s*(-?\d+),\s*(-?\d+)> velocity=<\s*(-?\d+),\s*(-?\d+)>")


class Reading(object):
    def __init__(self, position: Tuple[int, int], velocity: Tuple[int, int]):
        self.position = position
        self.velocity = velocity

    @classmethod
    def from_groups(cls, x, y, vx, vy):
        return cls(position=(x, y), velocity=(vx, vy))

    @classmethod
    def from_line(cls, line):
        return cls.from_groups(*map(int, regex.match(line).groups()))

    def step(self):
        self.position = (
            self.position[0] + self.velocity[0],
            self.position[1] + self.velocity[1],
        )

    def __repr__(self):
        return f"<{self.position}, {self.velocity}>"


# all_readings = list(map(Reading.from_line, test1.splitlines()))
all_readings = list(map(Reading.from_line, data))

counter = 0


def step_all(all_readings: List[Reading]):
    global counter
    counter = counter + 1
    for reading in all_readings:
        reading.step()


def getedges(all_readings: List[Reading]) -> Tuple[int, int, int, int]:
    right = max([reading.position[0] for reading in all_readings])
    left = min([reading.position[0] for reading in all_readings])
    top = min([reading.position[1] for reading in all_readings])
    bottom = max([reading.position[1] for reading in all_readings])
    return left, right, top, bottom


def plot_readings(all_readings: List[Reading]) -> str:
    occupied = set(reading.position for reading in all_readings)

    left, right, top, bottom = getedges(all_readings)
    width = right - left
    height = bottom - top

    output = ""
    for line in range(height + 1):
        for row in range(width + 1):
            if (row + left, line + top) in occupied:
                output += "█"
            else:
                output += " "
        output += "\n"
    return output
