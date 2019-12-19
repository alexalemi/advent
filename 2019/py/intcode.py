
import logging
from utils import data19
from collections import namedtuple
from typing import Callable, NamedTuple, List, IO
from enum import Enum
import sys

logging.basicConfig(level=logging.DEBUG)

data = data19(5)

Code = int

def getcodes(s):
    return list(map(int, s.split(",")))


def run(codes: List[Code], inp: List[Code], out: List[Code] = None):
    loc = 0
    out = out or []

    def readparam(mode, loc):
        pmode = mode % 10
        mode = mode // 10
        p = codes[loc]
        if pmode == 0:  # position mode
            p = codes[p]
        return p, mode

    while True:
        current_code = codes[loc]
        op = current_code % 100
        mode = current_code // 100

        if op == 99:
            return out

        # Read one parameter
        p1, mode = readparam(mode, loc+1)

        if op == 3:  # INPUT
            codes[codes[loc+1]] = inp.pop()
            loc += 2
            continue

        elif op == 4:  # OUTPUT
            out.append(p1)
            loc += 2
            continue

        p2, mode = readparam(mode, loc+2)
        if op == 5:  # jump-if-true
            if p1 != 0:
                loc = p2
            else:
                loc += 3
            continue

        if op == 6:  # jump-if-false
            if p1 == 0:
                loc = p2
            else:
                loc += 3
            continue

        if op == 7:  # less than
            codes[codes[loc+3]] = 1 * (p1 < p2)
            loc += 4
            continue

        if op == 8:
            codes[codes[loc+3]] = 1 * (p1 == p2)
            loc += 4
            continue

        if op == 1:  # ADD
            codes[codes[loc+3]] = p1 + p2
            loc += 4
            continue

        if op == 2:  # MUL
            codes[codes[loc+3]] = p1 * p2
            loc += 4
            continue

        raise NotImplementedError(f"Don't understand code {op}!")
            
    return out


if __name__ == "__main__":

    codes = getcodes(data)
    outs = run(codes[:], [1])
    assert all([x==0 for x in outs[:-1]])
    print("Answer1:", outs[-1])

    inps = [1, 2342, 7, 8, 9, -10, 0, -1, 500]
    def bigex(x):
        if x < 8:
            return 999
        elif x == 8:
            return 1000
        else:
            return 1001

    testfuncs = {
        (lambda x: 1*(x == 8)): [
            "3,9,8,9,10,9,4,9,99,-1,8", 
            "3,3,1108,-1,8,3,4,3,99",
            ],
        (lambda x: 1*(x < 8)): [
            "3,9,7,9,10,9,4,9,99,-1,8",
            "3,3,1107,-1,8,3,4,3,99",
            ],
        (lambda x: 1*(x != 0)): [
            "3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9",
            "3,3,1105,-1,9,1101,0,0,12,4,12,99,1",
            ],
        bigex: [
            "3,21,1008,21,8,20,1005,20,22,107,8,21,20,1006,20,31,1106,0,36,98,0,0,1002,21,125,20,4,20,1105,1,46,104,999,1105,1,46,1101,1000,1,20,4,20,1105,1,46,98,99",
            ],
        }

    for i, (func, progs) in enumerate(testfuncs.items()):
        for j, prog in enumerate(progs):
            codes = getcodes(prog)
            for inp in inps:
                assert run(codes[:], [inp]) == [func(inp)], f"Error on element {i}:{j} with input {inp}, got {run(codes[:], [inp])}, expected {func(inp)}"


    codes = getcodes(data)
    outs2 = run(codes[:], [5])
    assert len(outs2) == 1, "Wrong length output"
    print("Answer2:", outs2[-1])

