import time
from utils import data20
import dataclasses
from typing import List, Tuple

data = data20(8)

Inst = str
Instruction = Tuple[Inst, int]


@dataclasses.dataclass(frozen=True)
class Computer:
    code: List[Instruction]
    pk: int = 0
    accumulator: int = 0

    def run(self):
        inst, n = self.code[self.pk]
        if inst == "acc":
            acc = self.accumulator + n
            pk = self.pk + 1
        elif inst == "jmp":
            acc = self.accumulator
            pk = self.pk + n
        elif inst == "nop":
            acc = self.accumulator
            pk = self.pk + 1
        else:
            raise ValueError(f"Don't know the inst: {inst}, {n}!")
        return dataclasses.replace(self, pk=pk, accumulator=acc)


def parse(inp: str) -> Computer:
    out = []
    for line in inp.splitlines():
        inst, n = line.split()
        out.append((inst, int(n)))
    return Computer(out)


tests = [
    (
        """nop +0
acc +1
jmp +4
acc +3
jmp -3
acc -99
acc +1
jmp -4
acc +6""",
        5,
    )
]


def answer1(inp):
    comp = parse(inp)
    seen = set()

    while not (pk := comp.pk) in seen:
        seen.add(pk)
        comp = comp.run()

    return comp.accumulator


tests2 = [(tests[0][0], 8)]


def finish(comp):
    seen = set()
    while (not (pk := comp.pk) in seen) and (pk != len(comp.code)):
        seen.add(pk)
        comp = comp.run()
    return comp


def answer2(inp):
    comp = parse(inp)

    for i, (inst, n) in enumerate(comp.code):
        if inst in ("jmp", "nop"):
            newcode = comp.code[:]
            if inst == "jmp":
                newcode[i] = ("nop", n)
            elif inst == "nop":
                newcode[i] = ("jmp", n)
            else:
                raise IndexError("mistake!")
            localcomp = dataclasses.replace(comp, code=newcode)
            localcomp = finish(localcomp)
            if localcomp.pk == len(localcomp.code):
                return localcomp.accumulator


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    start = time.time()
    ans1 = answer1(data)
    end = time.time()
    print("Answer1:", ans1, f" in {end - start:0.3e} secs")

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    start = time.time()
    ans2 = answer2(data)
    end = time.time()
    print("Answer2:", ans2, f" in {end - start:0.3e} secs")
