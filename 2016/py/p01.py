from utils import data16

data = data16(1)


def magnitude(x):
    return int(abs(x.real) + abs(x.imag))


def read_direction(s):
    s = s.strip()
    return (s[0], int(s[1:].strip()))


def process(line):
    heading = 0 + 1j
    position = 0 + 0j
    for thing in line.split(","):
        direction, num = read_direction(thing)

        if direction == "L":
            heading *= 1j
        else:
            heading *= -1j

        for x in range(num):
            position += heading
    return magnitude(position)


def process2(line):
    heading = 0 + 1j
    position = 0 + 0j
    visited = set()
    for thing in line.split(","):
        direction, num = read_direction(thing)

        if direction == "L":
            heading *= 1j
        else:
            heading *= -1j

        for x in range(num):
            position += heading
            if position in visited:
                return magnitude(position)
            else:
                visited.add(position)
    return None


if __name__ == "__main__":
    ans = process(data)
    print(f"Answer1: {ans}")

    ans2 = process2(data)
    print(f"Answer2: {ans2}")
