from utils import data16

numbers = {
    0 + 0j: "5",
    0 + 1j: "2",
    -1 + 1j: "1",
    1 + 1j: "3",
    -1 + 0j: "4",
    1 + 0j: "6",
    -1 - 1j: "7",
    0 - 1j: "8",
    1 - 1j: "9",
}

moves = {
    "U": 0 + 1j,
    "D": 0 - 1j,
    "L": -1 + 0j,
    "R": 1 + 0j,
}


def process_line(start, line, pad=numbers):
    pos = start
    for char in line.strip():
        if (new := pos + moves[char]) in pad:
            pos = new
    return pos


def process(lines, pad=numbers, start=0 + 0j):
    pos = start
    poses = []
    for line in lines:
        pos = process_line(pos, line, pad)
        poses.append(pos)
    return "".join([pad[x] for x in poses])


#     1
#   2 3 4
# 5 6 7 8 9
#   A B C
#     D

numbers2 = {
    0 + 0j: "7",
    -1 + 0j: "6",
    -2 + 0j: "5",
    1 + 0j: "8",
    2 + 0j: "9",
    -1 - 1j: "A",
    0 - 1j: "B",
    1 - 1j: "C",
    0 - 2j: "D",
    -1 + 1j: "2",
    0 + 1j: "3",
    1 + 1j: "4",
    0 + 2j: "1",
}

data = data16(2).splitlines()

if __name__ == "__main__":
    test = """ULL
    RRDDD
    LURDL
    UUUUD"""
    test1 = process(test.splitlines())
    assert test1 == "1985"

    ans = process(data)
    print(f"Answer1: {ans}")

    ans2 = process(data, pad=numbers2, start=-2 + 0j)
    print(f"Answer2: {ans2}")
