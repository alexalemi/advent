import logging
from utils import data19
from collections import namedtuple
from typing import Callable, NamedTuple, List
from enum import Enum
import sys

logging.basicConfig(level=logging.DEBUG)

data = data19(5)


def getcodes(s):
    return list(map(int, s.split(",")))


class State(NamedTuple):
    tape: List[int]
    loc: int = 0
    done: bool = False
    inputs: List[int] = []
    outputs: List[int] = []


Op = Callable[[State], State]


class ParameterMode(Enum):
    Position = 0
    Immediate = 1


def mode(modes: int, pk: int) -> ParameterMode:
    return ParameterMode(modes // 10**pk % 10)


def value(state: State, mode: ParameterMode, param: int) -> int:
    if mode == ParameterMode.Position:
        return state.tape[param]
    elif mode == ParameterMode.Immediate:
        return param
    else:
        raise ValueError("Don't understand the ParameterMode!")


def get_value(argnum: int):
    def _(state: State, modes: int) -> int:
        return value(state, mode(modes, argnum), state.tape[state.loc + argnum + 1])

    return _


def add(modes):
    def _(state):
        x = get_value(0)(state, modes)
        y = get_value(1)(state, modes)
        to = state.tape[state.loc + 3]

        tape = state.tape[:]
        tape[to] = x + y
        return state._replace(tape=tape, loc=state.loc + 4)

    return _


def mul(modes):
    def _(state):
        x = get_value(0)(state, modes)
        y = get_value(1)(state, modes)
        to = state.tape[state.loc + 3]

        tape = state.tape[:]
        tape[to] = x * y
        return state._replace(tape=tape, loc=state.loc + 4)

    return _


def inp(modes):
    def _(state):
        to = state.tape[state.loc + 1]
        tape = state.tape[:]
        inps = state.inputs[:]
        tape[to] = inps.pop()
        return state._replace(
            tape=tape,
            loc=state.loc + 2,
            inputs=inps,
        )

    return _


def out(modes):
    def _(state):
        x = get_value(0)(state, modes)
        outs = state.outputs[:]
        outs.append(x)
        return state._replace(
            loc=state.loc + 2,
            outputs=outs,
        )

    return _


def end(modes):
    def _(state):
        return state._replace(done=True)

    return _


def jit(modes):
    """Jump-if-True: if the first parameter is non-zero,
    it sets the pointer to the value from the second."""

    def _(state):
        cond = get_value(0)(state, modes)
        to = get_value(1)(state, modes)

        if cond != 0:
            return state._replace(loc=to)
        else:
            return state._replace(loc=state.loc + 3)
        return state

    return _


def jif(modes):
    """Jump-if-False: if the first parameter is zero,
    it sets the pointer to the value from the second."""

    def _(state):
        cond = get_value(0)(state, modes)
        to = get_value(1)(state, modes)

        if cond == 0:
            return state._replace(loc=to)
        else:
            return state._replace(loc=state.loc + 3)
        return state

    return _


def less(modes):
    """Less: if the first parameter is less than the
    second, if yes, stores 1 in the final loc,
    otherwise stores 0."""

    def _(state):
        x = get_value(0)(state, modes)
        y = get_value(1)(state, modes)
        to = state.tape[state.loc + 3]
        tape = state.tape[:]
        tape[to] = 1 * (x < y)
        return state._replace(tape=tape, loc=state.loc + 4)

    return _


def equals(modes):
    """Equals: if the first parameter is equal to the
    second, it stores 1 in the final loc, otherwise
    stores 0."""

    def _(state):
        x = get_value(0)(state, modes)
        y = get_value(1)(state, modes)
        to = state.tape[state.loc + 3]
        tape = state.tape[:]
        tape[to] = 1 * (x == y)
        return state._replace(tape=tape, loc=state.loc + 4)

    return _


class OpType(Enum):
    Add = 1
    Mul = 2
    Inp = 3
    Out = 4
    Jit = 5
    Jif = 6
    Less = 7
    Equals = 8
    End = 99


Ops = {
    OpType.Add: add,
    OpType.Mul: mul,
    OpType.Inp: inp,
    OpType.Out: out,
    OpType.End: end,
    OpType.Jit: jit,
    OpType.Jif: jif,
    OpType.Less: less,
    OpType.Equals: equals,
}


def op(state: State) -> Op:
    opcode = state.tape[state.loc]
    optype = OpType(opcode % 100)
    modes = opcode // 100
    logging.debug(f"{optype} {modes}")
    return Ops[optype](modes)


def run(codes, inputs=[1]):
    state = State(codes, 0, False, inputs, [])

    while not state.done:
        state = op(state)(state)

    return state.outputs


if __name__ == "__main__":
    assert run([3, 0, 4, 0, 99]) == [1]

    codes = getcodes(data)
    outs = run(codes[:])
    assert all([x == 0 for x in outs[:-1]])
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
        (lambda x: 1 * (x == 8)): [
            "3,9,8,9,10,9,4,9,99,-1,8",
            "3,3,1108,-1,8,3,4,3,99",
        ],
        (lambda x: 1 * (x < 8)): [
            "3,9,7,9,10,9,4,9,99,-1,8",
            "3,3,1107,-1,8,3,4,3,99",
        ],
        (lambda x: 1 * (x != 0)): [
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
                assert (
                    run(codes[:], [inp]) == [func(inp)]
                ), f"Error on element {i}:{j} with input {inp}, got {run(codes[:], [inp])}, expected {func(inp)}"

    codes = getcodes(data)
    outs2 = run(codes[:], [5])
    assert len(outs2) == 1, "Wrong length output"
    print("Answer2:", outs2[-1])
