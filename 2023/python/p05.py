from functools import cached_property, partial
from dataclasses import dataclass

with open("../input/05.txt") as f:
    data = f.read()

test_data = """seeds: 79 14 55 13

seed-to-soil map:
50 98 2
52 50 48

soil-to-fertilizer map:
0 15 37
37 52 2
39 0 15

fertilizer-to-water map:
49 53 8
0 11 42
42 0 7
57 7 4

water-to-light map:
88 18 7
18 25 70

light-to-temperature map:
45 77 23
81 45 19
68 64 13

temperature-to-humidity map:
0 69 1
1 0 69

humidity-to-location map:
60 56 37
56 93 4
"""


@dataclass
class Entry:
    dest: int
    init: int
    size: int

    @cached_property
    def tail(self):
        return self.init + self.size

    def contains(self, x):
        return self.init <= x < self.tail

    def map(self, x):
        if self.contains(x):
            return self.dest + (x - self.init)
        return x


assert Entry(50, 98, 2).map(15) == 15
assert Entry(50, 98, 2).map(97) == 97
assert Entry(50, 98, 2).map(98) == 50
assert Entry(50, 98, 2).map(99) == 51
assert Entry(50, 98, 2).map(100) == 100
assert Entry(52, 50, 48).map(49) == 49
assert Entry(52, 50, 48).map(50) == 52
assert Entry(52, 50, 48).map(50 + 48) == 50 + 48
assert Entry(52, 50, 48).map(50 + 47) == 52 + 47


def process_map(s: str) -> list[Entry]:
    lines = s.splitlines()[1:]
    return [Entry(*list(map(int, line.split()))) for line in lines]


type Map = list[Entry]


def lookup(m: Map, x: int) -> int:
    for entry in m:
        if entry.contains(x):
            return entry.map(x)
    return x


def transform(maps: list[Map], x0: int) -> int:
    x = x0
    for m in maps:
        x = lookup(m, x)
    return x


def part1(s: str) -> int:
    # split file in to parts
    [seed_str, *maps_strs] = s.split("\n\n")
    # the seeds are a list of ints
    seeds: list[int] = list(map(int, seed_str.split(": ")[1].split()))
    # the maps are a list of list of entries
    maps: list[Map] = list(map(process_map, maps_strs))

    trans = partial(transform, maps)
    return min(map(trans, seeds))


assert part1(test_data) == 35

ans1 = part1(data)

assert ans1 == 165788812

# Part 2
# Now we need to treat the cards differently, each card
# tells us how many copies of the subsequent cards we win.


@dataclass
class Range:
    init: int
    tail: int

    @classmethod
    def from_size(cls, init: int, size: int):
        return cls(init, init + size)

    @cached_property
    def size(self):
        return self.tail - self.init


def modify(entry: Entry, x: Range) -> tuple[list[Range], list[Range]]:
    """Given a map entry and a range, return the untransformed and transformed Range"""
    if entry.tail <= x.init:
        # its all to the left
        return ([x], [])
    elif entry.init > x.tail:
        # all to right
        return ([x], [])
    elif entry.init <= x.init and entry.tail >= x.tail:
        # whole inside
        return ([], [Range(entry.map(x.init), 1 + entry.map(x.tail - 1))])
    elif entry.init <= x.init and entry.tail < x.tail:
        # left side inside
        return (
            [Range(entry.tail, x.tail)],
            [Range(entry.map(x.init), 1 + entry.map(entry.tail - 1))],
        )
    elif entry.init > x.init and entry.tail >= x.tail:
        # right side inside
        return (
            [Range(x.init, entry.init)],
            [Range(entry.map(entry.init), 1 + entry.map(x.tail - 1))],
        )
    elif entry.init > x.init and entry.tail < x.tail:
        # in middle
        return (
            [Range(x.init, entry.init), Range(entry.tail, x.tail)],
            [Range(entry.map(entry.init), 1 + entry.map(entry.tail - 1))],
        )
    else:
        raise NotImplementedError(f"Should never reach here! {entry} {range}")


def transform_range(m: Map, range0: Range) -> list[Range]:
    """Transform a list of ranges through a Map."""
    old = [range0]
    new = []
    for entry in m:
        unprocessed = []
        for r in old:
            (unused, active) = modify(entry, r)
            new.extend(active)
            unprocessed.extend(unused)
        old = unprocessed
    return old + new


assert transform_range(
    [Entry(0, 15, 37), Entry(37, 52, 2), Entry(39, 0, 15)], Range(57, 70)
) == [Range(57, 70)]


def step_ranges(m: Map, ranges: list[Range]):
    """Transform a list of ranges all through a single map."""
    return [x for parts in [transform_range(m, r) for r in ranges] for x in parts]


def part2(s: str) -> int:
    (seed_str, *maps_strs) = s.split("\n\n")
    seeds_pairs: list[int] = list(map(int, seed_str.split(": ")[1].split()))
    # convert the seeds to intervals [lo, hi)
    seeds: list[Range] = [
        Range.from_size(seeds_pairs[i], seeds_pairs[i + 1])
        for i in range(0, len(seeds_pairs), 2)
    ]
    maps: list[Map] = list(map(process_map, maps_strs))

    for i, m in enumerate(maps):
        seeds = step_ranges(m, seeds)

    return min(r.init for r in seeds)


assert part2(test_data) == 46

ans2 = part2(data)

assert ans2 == 1928058


if __name__ == "__main__":
    print("Answer1:", ans1)
    print("Answer2:", ans2)
