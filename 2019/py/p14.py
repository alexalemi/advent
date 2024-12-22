from utils import data19

data = data19(14)

tests = [
    (
        """10 ORE => 10 A
1 ORE => 1 B
7 A, 1 B => 1 C
7 A, 1 C => 1 D
7 A, 1 D => 1 E
7 A, 1 E => 1 FUEL""",
        31,
    ),
    (
        """9 ORE => 2 A
8 ORE => 3 B
7 ORE => 5 C
3 A, 4 B => 1 AB
5 B, 7 C => 1 BC
4 C, 1 A => 1 CA
2 AB, 3 BC, 4 CA => 1 FUEL""",
        165,
    ),
    (
        """157 ORE => 5 NZVS
165 ORE => 6 DCFZ
44 XJWVT, 5 KHKGT, 1 QDVJ, 29 NZVS, 9 GPVTF, 48 HKGWZ => 1 FUEL
12 HKGWZ, 1 GPVTF, 8 PSHF => 9 QDVJ
179 ORE => 7 PSHF
177 ORE => 5 HKGWZ
7 DCFZ, 7 PSHF => 2 XJWVT
165 ORE => 2 GPVTF
3 DCFZ, 7 NZVS, 5 HKGWZ, 10 PSHF => 8 KHKGT""",
        13312,
    ),
    (
        """2 VPVL, 7 FWMGM, 2 CXFTF, 11 MNCFX => 1 STKFG
17 NVRVD, 3 JNWZP => 8 VPVL
53 STKFG, 6 MNCFX, 46 VJHF, 81 HVMC, 68 CXFTF, 25 GNMV => 1 FUEL
22 VJHF, 37 MNCFX => 5 FWMGM
139 ORE => 4 NVRVD
144 ORE => 7 JNWZP
5 MNCFX, 7 RFSQX, 2 FWMGM, 2 VPVL, 19 CXFTF => 3 HVMC
5 VJHF, 7 MNCFX, 9 VPVL, 37 CXFTF => 6 GNMV
145 ORE => 6 MNCFX
1 NVRVD => 8 CXFTF
1 VJHF, 6 MNCFX => 4 RFSQX
176 ORE => 6 VJHF""",
        180697,
    ),
    (
        """171 ORE => 8 CNZTR
7 ZLQW, 3 BMBT, 9 XCVML, 26 XMNCP, 1 WPTQ, 2 MZWV, 1 RJRHP => 4 PLWSL
114 ORE => 4 BHXH
14 VRPVC => 6 BMBT
6 BHXH, 18 KTJDG, 12 WPTQ, 7 PLWSL, 31 FHTLT, 37 ZDVW => 1 FUEL
6 WPTQ, 2 BMBT, 8 ZLQW, 18 KTJDG, 1 XMNCP, 6 MZWV, 1 RJRHP => 6 FHTLT
15 XDBXC, 2 LTCX, 1 VRPVC => 6 ZLQW
13 WPTQ, 10 LTCX, 3 RJRHP, 14 XMNCP, 2 MZWV, 1 ZLQW => 1 ZDVW
5 BMBT => 4 WPTQ
189 ORE => 9 KTJDG
1 MZWV, 17 XDBXC, 3 XCVML => 2 XMNCP
12 VRPVC, 27 CNZTR => 2 XDBXC
15 KTJDG, 12 BHXH => 5 XCVML
3 BHXH, 2 VRPVC => 7 MZWV
121 ORE => 7 VRPVC
7 XCVML => 6 RJRHP
5 BHXH, 4 VRPVC => 5 LTCX""",
        2210736,
    ),
]


def parse(inp):
    """Process the text into the universe representation.

    The keys are the result of the reactions,
    the values are (yield, requirements) where yield
    is how many of the thing is produced
    and requirements is a dictionary with keys
    set to be elements and values the required amounts.
    """
    universe = {}
    for line in inp.splitlines():
        frm, to = line.split(" => ")
        n, product = to.split(" ")
        reactants = {}
        for line in frm.split(", "):
            m, reactant = line.split(" ")
            reactants[reactant] = int(m)
        universe[product] = (int(n), reactants)
    return universe


def topo(universe):
    """Simple topological sorting."""
    universe = universe.copy()
    ordered = ["ORE"]

    def seen(reqs, prev):
        return set(reqs).issubset(set(prev))

    while universe:
        elem = next(
            key for key, values in universe.items() if seen(values[1].keys(), ordered)
        )
        universe.pop(elem)
        ordered.append(elem)
    return ordered


def answer1(inp, target=1, debug=False):
    universe = parse(inp)
    needed = {"FUEL": target}
    ordered = topo(universe)
    # in reverse topological order...
    for elem in reversed(ordered[1:]):
        if debug:
            print(f"elem={elem}, needed={needed}")
        if elem in needed:
            want = needed.pop(elem)
            n, reqs = universe[elem]
            # m * n >= want
            m = (want - 1) // n + 1
            if debug:
                print(f"want={want}, n={n}, m={m}")
            for reactant in reqs:
                needed[reactant] = needed.get(reactant, 0) + m * reqs[reactant]

    assert len(needed) == 1, "Only one thing left."
    assert "ORE" in needed, "Ore is not last element."
    return needed["ORE"]


tests2 = [
    (tests[2][0], 82892753),
    (tests[3][0], 5586022),
    (tests[4][0], 460664),
]


def answer2(inp):
    # we know we can produce at least 1 trillion / required
    req = answer1(inp, 1)
    cutoff = 1_000_000_000_000
    start = cutoff // req

    ## find an end point by doubling
    end = start
    while answer1(inp, end) <= cutoff:
        end *= 2

    # bisection to find the answer
    while (end - start) > 1:
        guess = (end + start) // 2
        needed = answer1(inp, guess)
        if needed > cutoff:
            end = guess
        else:
            start = guess

    return start


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    print("Answer1:", answer1(data))

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    print("Answer2:", answer2(data))
