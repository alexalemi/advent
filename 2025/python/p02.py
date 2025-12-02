with open("../input/02.txt") as f:
    data_string = f.read()

test_string = """11-22,95-115,998-1012,1188511880-1188511890,222220-222224,1698522-1698528,446443-446449,38593856-38593862,565653-565659,824824821-824824827,2121212118-2121212124"""


def process(inp: str):
    ranges = []
    sections = inp.split(",")
    for section in sections:
        a, b = section.split("-")
        ranges.append((int(a), int(b)))
    return ranges


data = process(data_string)
test_data = process(test_string)


def valid(num: int) -> bool:
    x = str(num)
    l = len(x)
    if (l % 2 == 0) and x[: l // 2] == x[l // 2 :]:
        return True
    return False


def part1(data) -> int:
    total = 0
    for lo, hi in data:
        for x in range(lo, hi + 1):
            if valid(x):
                total += x
    return total


assert part1(test_data) == 1227775554, f"Failed part1 test!"


def valid2(num: int) -> bool:
    x = str(num)
    l = len(x)
    for i in range(1, l // 2 + 1):
        prefix = x[:i]
        if x == prefix * (l // i):
            return True
    else:
        return False


def part2(data) -> int:
    total = 0
    for lo, hi in data:
        for x in range(lo, hi + 1):
            if valid2(x):
                total += x
    return total


assert part2(test_data) == 4174379265, f"Failed part2 test!"


if __name__ == "__main__":
    ans1 = part1(data)
    print(f"Answer 1: {ans1}")

    ans2 = part2(data)
    print(f"Answer 2: {ans2}")
