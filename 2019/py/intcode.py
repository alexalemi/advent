import logging
from utils import data19
from collections import namedtuple
from collections import defaultdict
from typing import Callable, NamedTuple, List, IO
import sys
import io

logging.basicConfig(level=logging.DEBUG)

Code = int


def getcodes(s):
    return list(map(int, s.strip().split(",")))


class Computer:
    """Intcode Computer.
    
    The main method is `run` which will run the computer until
    the next stopping condition, either a halt (code 99) or 
    and input command if the computer is currently out of inputs.
    This will additionally return all of the newly generated outputs.

    Two status conditions can be checked, `finished` is True
    only after the Computer has terminated.  `waiting` is True
    if we are stuck at an input.  To restart the Computer, call `run`
    with some additional inputs.

    Other helpful properties include `processed_inputs` which will
    contain a list of all inputs processed, `inputs` which shows the yet to
    be processed inputs.  `outputs` contains all the outputs ever generated.
    """

    def __init__(self, codes, inps=None):
        if isinstance(codes, str):
            # Try to be friendly and convert a string to a list of codes
            codes = getcodes(codes)
        self.codes = defaultdict(int)
        for i, v in enumerate(codes[:]):
            self.codes[i] = v
        self.initial_codes = codes[:]
        self.finished = False
        self.waiting = False
        self.outputs = []
        if isinstance(inps, str):
            # Convert a string of inputs to a list of codes
            inps = getcodes(inps)
        self.inputs = inps or []
        self.processed_inputs = []
        self.loc = 0
        self.relative_base = 0

    def run(self, *inps):
        """Run the computer until it either halts or requests a missing input.

        Arguments:
            *inps: Optional inputs to add to the list of current inputs.

        Returns:
            new_outputs: A list of the outputs generated until a stopping condition.
        """
        newouts = []
        for i in inps:
            self.inputs.append(i)
        if len(self.inputs) > 0:
            self.waiting = False

        def readparam(mode, loc):
            """Helper function to read parameters according to mode."""
            pmode = mode % 10
            mode = mode // 10
            loc = self.codes[loc]
            if pmode == 0:  # position mode
                value = self.codes[loc]
            elif pmode == 1:  # immediate mode
                value = loc
            elif pmode == 2:  # relative mode
                loc = self.relative_base + loc
                value = self.codes[loc]
            # otherwise, we are in direct mode
            return value, loc, mode

        while True:
            current_code = self.codes[self.loc]
            op = current_code % 100
            mode = current_code // 100

            if op == 99:  # Halt
                self.finished = True
                return newouts

            # Read one parameter
            p1, pos1, mode = readparam(mode, self.loc + 1)

            if op == 3:  # INPUT
                if not self.inputs:
                    self.waiting = True
                    return newouts
                nextinp = self.inputs[0]
                self.processed_inputs.append(nextinp)
                self.inputs = self.inputs[1:]
                self.codes[pos1] = nextinp
                self.loc += 2
                continue

            elif op == 9:  # Relative
                self.relative_base += p1
                self.loc += 2
                continue

            elif op == 4:  # OUTPUT
                newouts.append(p1)
                self.outputs.append(p1)
                self.loc += 2
                continue

            p2, pos2, mode = readparam(mode, self.loc + 2)

            if op == 5:  # jump-if-true
                if p1 != 0:
                    self.loc = p2
                else:
                    self.loc += 3
                continue

            elif op == 6:  # jump-if-false
                if p1 == 0:
                    self.loc = p2
                else:
                    self.loc += 3
                continue

            p3, pos3, mode = readparam(mode, self.loc + 3)

            if op == 7:  # less than
                self.codes[pos3] = 1 * (p1 < p2)
                self.loc += 4
                continue

            elif op == 8:  # Equals
                self.codes[pos3] = 1 * (p1 == p2)
                self.loc += 4
                continue

            elif op == 1:  # ADD
                self.codes[pos3] = p1 + p2
                self.loc += 4
                continue

            elif op == 2:  # MUL
                self.codes[pos3] = p1 * p2
                self.loc += 4
                continue

            raise NotImplementedError(f"Don't understand code {op}!")


def run(codes: List[Code], inp: List[Code]):
    return Computer(codes).run(*inp)
