test_string = """3-5
10-14
16-20
12-18

1
5
8
11
17
32"""

type Range = tuple[int, int]
type Data = tuple[list[Range], list[int]]


def process(txt: str) -> Data:
    range_txt, ingredients_txt = txt.split("\n\n")

    ranges = []
    ingredients = []

    for line in range_txt.splitlines():
        left, right = line.split("-")
        ranges.append((int(left), int(right)))

    ingredients = list(map(int, ingredients_txt.splitlines()))
    return ranges, ingredients


with open("../input/05.txt") as f:
    data = process(f.read())

test_data = process(test_string)


def part1(data: Data) -> int:
    ranges, ingredients = data
    fresh = 0
    for ingredient in ingredients:
        for left, right in ranges:
            if left <= ingredient <= right:
                fresh += 1
                break
    return fresh


assert part1(test_data) == 3, "Failed part1 test"

ans1 = part1(data)
print(f"Answer 1: {ans1}")


def simple_merge(ranges: list[Range]) -> Range:
    """Get the smallest min and largest max of a list of ranges."""
    lefts, rights = zip(*ranges)
    return (min(lefts), max(rights))


def midpoint(rng: Range) -> int:
    (l, r) = rng
    return (r + l) // 2


def split_ranges(
    ranges: list[Range], pivot: int
) -> tuple[list[Range], list[Range], list[Range]]:
    """Separate into ranges purely to the left, overlapping and purely to the right of a pivot."""
    lefts = []
    overlaps = []
    rights = []

    for l, r in ranges:
        if r < pivot:
            lefts.append((l, r))
        elif l > pivot:
            rights.append((l, r))
        else:
            overlaps.append((l, r))
    return lefts, overlaps, rights


def left_clip(ranges: list[Range], cutoff: int) -> list[Range]:
    """Truncate ranges so that they are all > cutoff."""
    out = []
    for l, r in ranges:
        if r > cutoff:
            if l > cutoff:
                out.append((l, r))
            else:
                out.append((cutoff + 1, r))
    return out


def right_clip(ranges: list[Range], cutoff: int) -> list[Range]:
    """Truncate ranges so they are all < cutoff."""
    out = []
    for l, r in ranges:
        if l < cutoff:
            if r < cutoff:
                out.append((l, r))
            else:
                out.append((l, cutoff - 1))
    return out


def total_fresh(ranges: list[Range]) -> int:
    """Find the total number of elements covered by a list of ranges."""

    # recursive base case
    if not ranges:
        return 0

    # we want an element in at least one range, just grab the middle of the first one.
    pivot = midpoint(ranges[0])
    # split ranges into ones on the left, overlapping ones, and ones on the right
    lefts, overlaps, rights = split_ranges(ranges, pivot)

    # the overlapping ones are easy to merge
    l, r = simple_merge(overlaps)
    middle_count = r - l + 1

    # the left and right ones are easy to clip
    lefts = right_clip(lefts, l)
    rights = left_clip(rights, r)

    # recurse
    return middle_count + total_fresh(lefts) + total_fresh(rights)


def part2(data: Data) -> int:
    """Given a set of ranges, figure out how many ingredients in total are fresh."""
    ranges, _ = data
    return total_fresh(ranges)


assert part2(test_data) == 14, "Failed part 2 test"


ans2 = part2(data)
print(f"Answer 2: {ans2}")
