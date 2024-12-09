# Advent of Code 2024 - Day 9

from collections import deque

with open("../input/09.txt") as f:
    raw_data = f.read()

raw_test_data = """2333133121414131402"""


def process(s: str) -> deque:
    return deque(
        (int(n), i // 2 if i % 2 == 0 else None) for i, n in enumerate(s.strip())
    )


data = process(raw_data)
test_data = process(raw_test_data)


def compact(data: deque) -> list[tuple[int, int | None]]:
    """Attempts to move file segments from the right to the free space on the left."""
    output = []
    data = data.copy()
    (chunks, pk) = (0, None)
    (tmp_chunks, tmp_pk) = (0, None)
    while data:
        (chunks, pk) = data.popleft()
        if pk is not None:
            # This is a normal file
            output.append((chunks, pk))
        else:
            while chunks:
                if not data:
                    break
                while (tmp_pk is None) or (tmp_chunks == 0):
                    (tmp_chunks, tmp_pk) = data.pop()
                to_take = min(chunks, tmp_chunks)
                output.append((to_take, tmp_pk))
                chunks -= to_take
                tmp_chunks -= to_take
    if (tmp_chunks > 0) and (tmp_pk is not None):
        output.append((tmp_chunks, tmp_pk))

    return output


def checksum(data: list[tuple[int, int | None]]) -> int:
    total = 0
    loc = 0
    for count, pk in data:
        pk = pk or 0
        total += (count * pk * (count + 2 * loc - 1)) // 2
        loc += count

    return total


def part1(data: deque) -> int:
    return checksum(compact(data))


assert (test1 := part1(test_data)) == 1928, f"Failed Part 1 test. {test1} != 1928"
ans1 = part1(data)
assert ans1 == 6349606724455, "Failed part 1"

## Part 2


def seek(data: deque, size: int) -> tuple[int, int | None]:
    """Find the first file that fits. It none, return (size, None)"""
    n = len(data)
    for i, (chunks, pk) in enumerate(reversed(data)):
        if (pk is not None) and (chunks <= size):
            data[n - i - 1] = (chunks, None)
            return (chunks, pk)
    else:
        return (size, None)


def compact2(data: deque) -> list[tuple[int, int | None]]:
    """Now only moves whole files to the right."""
    output = []
    data = data.copy()

    while data:
        (chunks, pk) = data.popleft()
        if pk is not None:
            # This is a normal file
            output.append((chunks, pk))
        else:
            while chunks:
                # There is some empty space
                (found_chunks, found_pk) = seek(data, chunks)
                output.append((found_chunks, found_pk))
                chunks -= found_chunks

    return output


def part2(data: deque) -> int:
    return checksum(compact2(data))


assert (test2 := part2(test_data)) == 2858, f"Failed part 2 test {test2=} != 2858"
ans2 = part2(data)

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
