
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


def part1(s):
    [seeds, *maps] = s.split("\n\n")
    seeds = list(map(int, seeds.split(": ")[1].split()))
    maps = list(map(process_map, maps))

    def transform(start):
        vals = start
        for m in maps:
            vals = [lookup(m, x) for x in vals]
        return min(vals)

    return transform(seeds)


assert part1(test_data) == 35

ans1 = part1(data)

assert ans1 == 165788812

# Part 2
# Now we need to treat the cards differently, each card
# tells us how many copies of the subsequent cards we win.


def part2(s):
    [seeds, *maps] = s.split("\n\n")
    seeds = list(map(int, seeds.split(": ")[1].split()))
    seeds = [(seeds[i], seeds[i+1]) for i in range(0, len(seeds), 2)]

    maps = list(map(process_map, maps))

    def transform(start):
        x = start
        for m in maps:
            x = lookup(m, x) 
        return x

    def gen():
        for (start, width) in seeds:
            for i in range(width):
                yield transform(start + i)

    return min(gen())

assert part2(test_data) == 46

print("Part2")
ans2 = part2(data)

# assert ans2 == 5625994


if __name__ == "__main__":
    print("Answer1:", ans1)
    print("Answer2:", ans2)
