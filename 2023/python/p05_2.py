# Gonna work on part 2 again.

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

def process_map(s):
    lines = s.splitlines()[1:]
    return [list(map(int, line.split())) for line in lines]


def lookup(m, x):
    for (dest, start, width) in m:
        if start <= x <= start+width:
            return dest + (x - start)
    return x

s = test_data
[seeds, *maps] = s.split("\n\n")
seeds = list(map(int, seeds.split(": ")[1].split()))
seeds = [(seeds[i], seeds[i+1]) for i in range(0, len(seeds), 2)]
maps = list(map(process_map, maps))

def transform1(entry, r):
    x, w = r
    lo = x
    hi = x + w
    (dest, start, width) = entry
    if start+width <= lo:
        # below
        return [r]
    elif start >= hi:
        # above
        return [r]
    elif start <= lo and (start+width) >= hi:
        # whole
        return [[dest + (x - start), w]]
    elif start <= lo and start+width < hi:
        # left half
        return [[dest+ (x-start), (start+width)-x], [start+width, w-(start+width-x)]]
    elif start > lo and (start+width) >= hi:
        # right half
        return [[x, start-x], [dest, x+w-start]]
    else:
        # inside
        assert (start > lo) and ((start + width) < hi)
        return [[x, start-x], [dest, width], [start+width, (x+w)-(start+width)]]

def transform(m, r):
    rs = [r]
    for entry in m:
        rs = [piece for part in [transform1(entry, r) for r in rs] for piece in part]
    return rs

def transform_all(m, rs):
    for r in rs:
        yield from transform(m, r)

def transform_deep(ms, start):
    rs = start
    for m in ms:
        rs = list(transform_all(m, rs))
    return rs

# seeds[0]
# maps[0][1]

# transform1(maps[0][1], seeds[0])

# transform(maps[0], seeds[0])

# list(transform_all(maps[0], seeds))

# seeds2 = list(transform_all(maps[0], seeds))
# seeds3 = list(transform_all(maps[1], seeds2))
# seeds4 = list(transform_all(maps[2], seeds3))
# seeds5 = list(transform_all(maps[3], seeds4))
# seeds6 = list(transform_all(maps[4], seeds5))
# seeds7 = list(transform_all(maps[5], seeds6))
# seeds8 = list(transform_all(maps[6], seeds7))
# seeds8

# transform_deep(maps, seeds)

print(min(transform_deep(maps, seeds)))


