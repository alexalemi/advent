with open("../input/03.txt") as f:
    data_string = f.read()

test_string = """987654321111111
811111111111119
234234234234278
818181911112111"""


def process(inp: str):
    out = []
    for line in inp.splitlines():
        out.append(list(map(int, line.strip())))
    return out


data = process(data_string)
test_data = process(test_string)


def maxi(seq):
    loc = 0
    val = None
    for i, x in enumerate(seq):
        if (val is None) or (x > val):
            val = x
            loc = i
    return loc, val


def maxnum(line, n=2):
    """Get the maxinum number we can form from a line of a given size."""
    loc = 0
    ans = 0
    for j in range(n):
        ans *= 10
        loc, val = maxi(line[: (-(n - 1 - j) or None)])
        line = line[loc + 1 :]
        ans += val
    return ans


def part1(data) -> int:
    total = 0
    for line in data:
        total += maxnum(line)
    return total


assert part1(test_data) == 98 + 89 + 78 + 92, f"Failed part1 test!"


def part2(data) -> int:
    total = 0
    for line in data:
        total += maxnum(line, 12)
    return total


assert part2(test_data) == 987654321111 + 811111111119 + 434234234278 + 888911112111, (
    f"Failed part2 test!"
)


if __name__ == "__main__":
    ans1 = part1(data)
    print(f"Answer 1: {ans1}")

    ans2 = part2(data)
    print(f"Answer 2: {ans2}")
