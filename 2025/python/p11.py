test_string = """aaa: you hhh
you: bbb ccc
bbb: ddd eee
ccc: ddd eee fff
ddd: ggg
eee: out
fff: out
ggg: out
hhh: ccc fff iii
iii: out"""


def process(s: str) -> dict[str, set[str]]:
    graph = {}
    for line in s.splitlines():
        key, vals = line.split(":")
        graph[key] = set(vals.split())
    return graph


test_data = process(test_string)

with open("../input/11.txt") as f:
    data = process(f.read())


def invert(data: dict[str, set[set]]) -> dict[str, set[set]]:
    parents = {}
    for node, children in data.items():
        for c in children:
            cs = parents.setdefault(c, set())
            cs.add(node)
    return parents


def count_paths(
    parents: dict[str, set[str]], start: str = "you", end: str = "out"
) -> int:
    num_paths: dict[str, int] = {start: 1}

    def paths(node: str) -> int:
        if node in num_paths:
            return num_paths[node]
        elif node not in parents:
            return 0
        else:
            ans = sum(paths(n) for n in parents[node])
            num_paths[node] = ans
            return ans

    return paths(end)


def part1(data: dict[str, set[set]]) -> int:
    return count_paths(invert(data))


assert part1(test_data) == 5, "Failed part 1 test"

ans1 = part1(data)
print(f"Answer 1: {ans1}")
assert ans1 == 649, "Failed part 1 answer"


test_data2 = process("""svr: aaa bbb
aaa: fft
fft: ccc
bbb: tty
tty: ccc
ccc: ddd eee
ddd: hub
hub: fff
eee: dac
dac: fff
fff: ggg hhh
ggg: out
hhh: out""")


def part2(data: dict[str, set[set]]) -> int:
    parents = invert(data)
    svr_fft = count_paths(parents, "svr", "fft")
    srv_dac = count_paths(parents, "svr", "dac")

    fft_dac = count_paths(parents, "fft", "dac")
    dac_fft = count_paths(parents, "dac", "fft")

    dac_out = count_paths(parents, "dac", "out")
    fft_out = count_paths(parents, "fft", "out")

    assert fft_dac * dac_fft == 0, "No cycles!"

    return svr_fft * fft_dac * dac_out + srv_dac * dac_fft * fft_out


assert part2(test_data2) == 2, "Failed part 2 test"

ans2 = part2(data)
print(f"Answer 2: {ans2}")
assert ans2 == 458948453421420, "Failed part 2 answer"
