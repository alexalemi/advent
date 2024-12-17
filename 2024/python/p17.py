# Advent of Code 2024 - Day 17

import dataclasses
import parser
import enum
import copy

with open("../input/17.txt") as f:
    raw_data = f.read()

raw_test_data = """Register A: 729
Register B: 0
Register C: 0

Program: 0,1,5,4,3,0"""


class OpCode(enum.Enum):
    adv = 0
    bxl = 1
    bst = 2
    jnz = 3
    bxc = 4
    out = 5
    bdv = 6
    cdv = 7


@dataclasses.dataclass
class Machine:
    instructions: list[int]
    registers: list[int]
    instruction_pointer: int = 0
    outputs: list[int] = dataclasses.field(default_factory=list)
    clock: int = 0
    halted: bool = False

    def copy(self):
        return copy.deepcopy(self)

    @property
    def A(self):
        return self.registers[0]

    @A.setter
    def A(self, val):
        self.registers[0] = val

    @property
    def B(self):
        return self.registers[1]

    @B.setter
    def B(self, val):
        self.registers[1] = val

    @property
    def C(self):
        return self.registers[2]

    @C.setter
    def C(self, val):
        self.registers[2] = val

    def combo(self, val):
        match val:
            case 0:
                return 0
            case 1:
                return 1
            case 2:
                return 2
            case 3:
                return 3
            case 4:
                return self.A
            case 5:
                return self.B
            case 6:
                return self.C
            case 7:
                return None

    def run(self) -> "Machine":
        while not self.halted:
            self.tic()
        return ",".join(map(str, self.outputs))

    def tic(self) -> "Machine":
        if self.halted:
            return self

        try:
            inst = OpCode(self.instructions[self.instruction_pointer])
            raw_op = self.instructions[self.instruction_pointer + 1]
        except IndexError:
            self.halted = True
            return self

        match inst:
            case OpCode.adv:
                self.A = self.A // (1 << self.combo(raw_op))
            case OpCode.bxl:
                self.B = self.B ^ raw_op
            case OpCode.bst:
                self.B = self.combo(raw_op) % 8
            case OpCode.jnz:
                if self.A != 0:
                    self.clock += 1
                    self.instruction_pointer = raw_op
                    return self
            case OpCode.bxc:
                self.B = self.B ^ self.C
            case OpCode.out:
                self.outputs.append(self.combo(raw_op) % 8)
            case OpCode.bdv:
                self.B = self.A // (1 << self.combo(raw_op))
            case OpCode.cdv:
                self.C = self.A // (1 << self.combo(raw_op))

        self.clock += 1
        self.instruction_pointer += 2


def process(s: str) -> Machine:
    registers, program = s.split("\n\n")
    registers = parser.ints(registers)
    program = parser.ints(program)
    return Machine(instructions=list(program), registers=list(registers))


data = process(raw_data)
test_data = process(raw_test_data)


def part1(data: Machine) -> str:
    return data.copy().run()


assert part1(test_data) == "4,6,3,5,6,3,5,2,1,0", "Failed part 1 test"
ans1 = part1(data)
exp_out = [1, 7, 6, 5, 1, 0, 5, 0, 7]
assert ans1 == ",".join(map(str, exp_out))

## Part 2

raw_test_data2 = """Register A: 2024
Register B: 0
Register C: 0

Program: 0,3,5,4,3,0"""

test_data2 = process(raw_test_data2)


def exhaustive_part2(data: Machine) -> int:
    t = 0
    while True:
        local = data.copy()
        local.A = t
        local.run()
        if local.outputs == local.instructions:
            return t
        t += 1


# assert exhaustive_part2(test_data2) == 117440, "Failed exhaustive reversal"

def inner_loop(a: int) -> int:
    b = a % 8
    b = b ^ 3
    c = a >> b
    b = b ^ c
    a = a >> 3
    b = b ^ 5
    return a, b % 8


def program(a: int) -> str:
    out = []
    while a != 0:
        # print(f"a={bin(a)}: {a} -> {inner_loop(a)[1]}")
        a, o = inner_loop(a)
        out.append(o)
    return out


assert program(data.A) == exp_out

LOOKUP = {6: 3, 7: 1, 5: 2, 2: 4, 3: 5, 0: 6, 1: 7, 4: 0}

# targ  bin    ^5
#  0    000   101
#  1    001   100
#  2    010   111
#  3    011   110
#  4    100   001
#  5    101   000
#  6    110   011
#  7    111   010


def reconstructions(target: list[int]) -> int:
    # try to work backwards through program.
    sols = {0}
    for val in reversed(target):
        new = set()
        for sol in sols:
            for tail in range(8):
                cand = (sol << 3) | tail
                newa, out = inner_loop(cand)
                if out == val:
                    new.add(cand)
        sols = new
    return sols


def part2() -> int:
    sols = reconstructions(data.instructions)
    ans = min(sols)
    return ans


ans2 = part2()
assert program(ans2) == data.instructions

## Main

if __name__ == "__main__":
    print(f"Answer 1: {ans1}")
    print(f"Answer 2: {ans2}")
