# Advent of Code 2024 - Day 24

import pprint
import copy
import graphlib
import operator
import dataclasses
import enum

with open("../input/24.txt") as f:
    raw_data = f.read()


raw_test_data1 = """x00: 1
x01: 1
x02: 1
y00: 0
y01: 1
y02: 0

x00 AND y00 -> z00
x01 XOR y01 -> z01
x02 OR y02 -> z02"""

raw_test_data2 = """x00: 1
x01: 0
x02: 1
x03: 1
x04: 0
y00: 1
y01: 1
y02: 1
y03: 1
y04: 1

ntg XOR fgs -> mjb
y02 OR x01 -> tnw
kwq OR kpj -> z05
x00 OR x03 -> fst
tgd XOR rvg -> z01
vdt OR tnw -> bfw
bfw AND frj -> z10
ffh OR nrd -> bqk
y00 AND y03 -> djm
y03 OR y00 -> psh
bqk OR frj -> z08
tnw OR fst -> frj
gnj AND tgd -> z11
bfw XOR mjb -> z00
x03 OR x00 -> vdt
gnj AND wpb -> z02
x04 AND y00 -> kjc
djm OR pbm -> qhw
nrd AND vdt -> hwm
kjc AND fst -> rvg
y04 OR y02 -> fgs
y01 AND x02 -> pbm
ntg OR kjc -> kwq
psh XOR fgs -> tgd
qhw XOR tgd -> z09
pbm OR djm -> kpj
x03 XOR y03 -> ffh
x00 XOR y04 -> ntg
bfw OR bqk -> z06
nrd XOR fgs -> wpb
frj XOR qhw -> z04
bqk OR frj -> z07
y03 OR x01 -> nrd
hwm AND bqk -> z03
tgd XOR rvg -> z12
tnw OR pbm -> gnj"""


type Node = str


class Op(enum.Enum):
    AND = 0
    OR = 1
    XOR = 2


@dataclasses.dataclass
class Rule:
    inputs: tuple[Node, Node]
    op: Op

    def __call__(self, val1, val2):
        match self.op:
            case Op.AND:
                return operator.and_(val1, val2)
            case Op.OR:
                return operator.or_(val1, val2)
            case Op.XOR:
                return operator.xor(val1, val2)


@dataclasses.dataclass
class Problem:
    known: dict[Node, bool]
    rules: dict[Node, Rule]

    def ready(self):
        for node, rule in self.rules.items():
            a, b = rule.inputs
            if a in self.known and b in self.known:
                aval = self.known[a]
                bval = self.known[b]
                yield node, rule(aval, bval), rule


def process(s: str):
    inputs, wires = s.split("\n\n")
    known = {}
    for line in inputs.splitlines():
        node, val = line.split(": ")
        known[node] = bool(int(val))

    rules = {}
    for wire in wires.splitlines():
        a, op, b, _, out = wire.split()
        rules[out] = Rule(inputs=(a, b), op=Op[op])

    return Problem(known=known, rules=rules)


test_data1 = process(raw_test_data1)
test_data2 = process(raw_test_data2)
data = process(raw_data)


def resolve(data: Problem) -> dict[Node, int]:
    output = data.known.copy()

    for node in graphlib.TopologicalSorter(
        {output: rule.inputs for output, rule in data.rules.items()}
    ).static_order():
        if node in data.rules:
            rule = data.rules[node]
            a, b = rule.inputs
            aval = output[a]
            bval = output[b]
            output[node] = rule(aval, bval)

    return output


def toint(bools: list[bool]) -> int:
    out = 0
    for x in bools:
        out <<= 1
        out |= x
    return out


def part1(data: Problem) -> int:
    outputs = resolve(data)
    z = []
    i = 0
    while (name := f"z{i:02d}") in outputs:
        z.append(outputs[name])
        i += 1

    return toint(reversed(z))


assert part1(test_data1) == 4, "Failed part 1 test 1"
assert part1(test_data2) == 2024, "Failed part 1 test 2"
ans1 = part1(data)
assert ans1 == 49430469426918, "Failed part 1"

## Part 2

## Now we are told that our circuit is meant to implement binary addition of two numbers
## the x and y, spread across many gates.
## But apparently, four of our gates are swapped.

## What do we expect we expect a XOR, AND and OR gate for every digit,
## The XOR computes the result, the AND the carry and an OR let's us combine the
## carry.

## xNN XOR yNN -> addNN
## addNN XOR carryNN -> zNN
## xN-1 AND yN-1 -> carryNN

z_rules = [data.rules[f"z{i:02d}"] for i in range(46)]

[rule.op for rule in z_rules]

topo_rules = [
    x
    for x in graphlib.TopologicalSorter(
        {output: rule.inputs for output, rule in data.rules.items()}
    ).static_order()
    if x in data.rules
]

# ['dcm', 'mgj', 'drs', 'fkd', 'jkt', 'vvr', 'qsh', 'pbk', 'qjf', 'fsb', 'phw', 'brk', 'nhs', 'wbf', 'ttv', 'qdb', 'gcf', 'scg', 'ptf', 'ftd', 'msq', 'hnt', 'spp', 'bmb', 'nrk', 'btn', 'bbh', 'rdf', 'tvh', 'fcv', 'mpm', 'vtf', 'bcd', 'wdr', 'pgm', 'rrb', 'vmc', 'whj', 'tfq', 'bgt', 'dtc', 'qwb', 'ggh', 'pwt', 'njs', 'thp', 'pdq', 'cbr', 'frj', 'wsq', 'wns', 'vpm', 'wqt', 'tcd', 'jgm', 'cjc', 'kcs', 'hdp', 'dmw', 'njt', 'mkv', 'wpk', 'z16', 'dfn', 'pqh', 'sdn', 'ggs', 'vnr', 'qff', 'qnw', 'z00', 'bwv', 'cjn', 'drt', 'bmr', 'gpb', 'cqk', 'mbp', 'nhk', 'jkm', 'tqj', 'dfc', 'hjw', 'smv', 'nts', 'bfd', 'rpw', 'rbm', 'knh', 'qnn', 'sgv', 'z01', 'hqq', 'z02', 'bkc', 'ckv', 'z03', 'tbq', 'frv', 'z04', 'vjj', 'mmh', 'z05', 'rkt', 'kqk', 'z06', 'pqp', 'hjd', 'z07', 'dvs', 'ksd', 'vwf', 'z08', 'gdm', 'z09', 'ftf', 'pvb', 'fjh', 'z10', 'ncw', 'z11', 'stw', 'wsv', 'z12', 'hbq', 'fbm', 'z13', 'pjj', 'vmp', 'z14', 'ggg', 'kqc', 'z15', 'wfw', 'qcr', 'pbv', 'mvp', 'dbj', 'z17', 'svg', 'rsq', 'brq', 'z18', 'vrh', 'qpj', 'z19', 'bkv', 'gkj', 'z20', 'grv', 'z21', 'tbb', 'srv', 'z22', 'rng', 'cts', 'qqp', 'jcd', 'kqp', 'z24', 'z23', 'frw', 'hgq', 'z25', 'dbp', 'z26', 'dbt', 'wcw', 'hjm', 'z27', 'wvv', 'qdh', 'z28', 'jmh', 'gfp', 'z29', 'qpq', 'z30', 'qmw', 'tpt', 'z31', 'dfj', 'fgc', 'z32', 'shp', 'pnw', 'z33', 'wcs', 'kpp', 'z34', 'wbk', 'fgq', 'drp', 'z35', 'jdd', 'z36', 'fbq', 'fhv', 'wwg', 'z37', 'bhh', 'hsh', 'z38', 'rms', 'z39', 'fhc', 'fwt', 'z40', 'ghn', 'hvw', 'vgk', 'z41', 'rps', 'z42', 'drv', 'gdw', 'kpc', 'z43', 'frk', 'hgp', 'z44', 'z45']
# the beginning of the sort are just all of the read ins.


def rename_gate(data: Problem, old: str, new: str) -> Problem:
    data = copy.deepcopy(data)
    morph = lambda x: new if x == old else x

    data.known = {morph(node): val for node, val in data.known.items()}
    data.rules = {
        morph(node): Rule(
            inputs=(morph(rule.inputs[0]), morph(rule.inputs[1])), op=rule.op
        )
        for node, rule in data.rules.items()
    }
    return data


def rename(data: Problem) -> Problem:
    """Try to rename the gates to more sane names."""
    data = copy.deepcopy(data)
    name_changes = {}

    # find all of the input combination gates
    for i in range(45):
        xgate = f"x{i:02d}"
        ygate = f"y{i:02d}"

        # DIGIT ADDS
        add_rules = [
            out
            for out, rule in data.rules.items()
            if (set(rule.inputs) == {xgate, ygate}) and (rule.op == Op.XOR)
        ]
        assert len(add_rules) == 1, f"@{i} {add_rules=}"
        old_name = add_rules[0]
        new_name = f"ADD{i:02d}"
        print(f"Renaming {old_name} to {new_name}")
        if old_name != "z00":
            if old_name.startswith("z"):
                print(f"FOUND A BAD ONE: {old_name}")
            else:
                data = rename_gate(data, old_name, new_name)
                name_changes[old_name] = new_name

        # DIGIT CARRYS
        carry_rules = [
            out
            for out, rule in data.rules.items()
            if (set(rule.inputs) == {xgate, ygate}) and (rule.op == Op.AND)
        ]
        assert len(carry_rules) == 1, f"@{i} {carry_rules=}"
        old_name = carry_rules[0]
        new_name = f"CARRY{i:02d}"
        print(f"Renaming {old_name} to {new_name}")
        if old_name.startswith("z"):
            print(f"FOUND A BAD ONE: {old_name}")
        else:
            data = rename_gate(data, old_name, new_name)
            name_changes[old_name] = new_name

    for i in range(1, 46):
        carry_gate = f"CARRY{i:02d}"
        cands = [
            (out, rule)
            for out, rule in data.rules.items()
            if carry_gate in set(rule.inputs) and (rule.op == Op.OR)
        ]
        try:
            assert len(cands) == 1, f"carry @{i} {cands=}"
        except:
            continue
        old_name, rule = cands[0]
        new_name = f"FCARRY{i:02d}"
        print(f"Renaming {old_name} to {new_name}")
        if old_name.startswith("z"):
            print(f"FOUND A BAD ONE: {old_name}")
        else:
            data = rename_gate(data, old_name, new_name)
            name_changes[old_name] = new_name

        old_name = next(iter(set(rule.inputs) - {carry_gate}))
        new_name = f"TMP{i:02d}"
        print(f"Renaming {old_name} to {new_name}")
        if old_name.startswith("z"):
            print(f"FOUND A BAD ONE: {old_name}")
        else:
            data = rename_gate(data, old_name, new_name)
            name_changes[old_name] = new_name

    return data, name_changes


def swap(data: Problem, old_name, new_name) -> Problem:
    data = copy.deepcopy(data)
    (data.rules[old_name], data.rules[new_name]) = (
        data.rules[new_name],
        data.rules[old_name],
    )
    return data


new_data, name_changes = rename(data)
assert part1(new_data) == ans1, "Failed to preserve answer."

# pprint.pprint(new_data.rules)

# z16 is one of the ones that needs to be swapped!
# z23
# z36
# z45

swapped_data = copy.deepcopy(data)

changes = [("z16", "pbv"), ("z23", "qqp"), ("z36", "fbq"), ("qnw", "qff")]

for change in changes:
    swapped_data = swap(swapped_data, *change)

new_data, name_changes = rename(swapped_data)


ans2 = ",".join(sorted(y for x in changes for y in x))
assert ans2 == "fbq,pbv,qff,qnw,qqp,z16,z23,z36", "Failed part 2"

print()


if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
