
import logging
from utils import data19
from collections import namedtuple
from typing import Callable, NamedTuple, List, IO
from enum import Enum
import sys
import io

logging.basicConfig(level=logging.DEBUG)

data = data19(5)

Code = int

def getcodes(s):
    return list(map(int, s.strip().split(",")))

def interpret(codes: List[Code], inp, out):
    loc = 0

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
            return

        # Read one parameter
        p1, mode = readparam(mode, loc+1)

        if op == 3:  # INPUT
            x = inp.get()
            #print(f"input={x}")
            codes[codes[loc+1]] = x
            loc += 2
            inp.task_done()
            continue

        elif op == 4:  # OUTPUT
            #print(f"output={p1}")
            out.put(p1)
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
            
    return 


