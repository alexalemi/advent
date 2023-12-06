import math

with open("../input/06.txt") as f:
    data_string = f.read()

test_data_string = """Time:      7  15   30
Distance:  9  40  200"""


def process(s: str) -> list[tuple[int, int]]:
    time, distance = s.splitlines()
    time = time[5:]
    distance = distance[9:]
    return list(zip(map(int, time.split()), map(int, distance.split())))


data = process(data_string)
test_data = process(test_data_string)


def roots(t, d):
    x = math.sqrt(t**2 - 4 * d)
    return [
        math.ceil(math.nextafter((t - x) / 2, math.inf)),
        math.floor(math.nextafter((t + x) / 2, 0)),
    ]


def ways_to_beat(race: tuple[int, int]) -> int:
    (time, distance) = race
    (lo, hi) = roots(time, distance)
    return hi - lo + 1


def part1(data: list[tuple[int, int]]) -> int:
    return math.prod(map(ways_to_beat, data))


assert part1(test_data) == 288

ans1 = part1(data)

assert ans1 == 74698

# Part 2
# Now we have to combine the numbers into one long number.


def combine_nums(nums: list[int]) -> int:
    return int("".join(map(str, nums)))

tuple(map(int, ["1", "2"]))

def part2(data: list[tuple[int, int]]) -> int:
    new_data = [tuple(map(combine_nums, list(zip(*data))))]
    return part1(new_data)


assert part2(test_data) == 71503

ans2 = part2(data)

assert ans2 == 27563421


if __name__ == "__main__":
    print("Answer1:", ans1)
    print("Answer2:", ans2)
