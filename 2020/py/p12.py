import time
import cmath
import dataclasses
from utils import data20

data = data20(12)

tests = [
    (
        """F10
N3
F7
R90
F11""",
        25,
    )
]


@dataclasses.dataclass(frozen=True)
class Move:
    kind: str
    amount: int


@dataclasses.dataclass(frozen=True)
class State:
    direction: complex
    position: complex


def process(inp):
    return tuple(Move(s[0], int(s[1:])) for s in inp.splitlines())


def intify(c):
    return complex(int(round(c.real)), int(round(c.imag)))


def move(state, move):
    if move.kind == "N":
        return dataclasses.replace(
            state, position=intify(state.position + 1j * move.amount)
        )
    elif move.kind == "S":
        return dataclasses.replace(
            state, position=intify(state.position - 1j * move.amount)
        )
    elif move.kind == "E":
        return dataclasses.replace(state, position=intify(state.position + move.amount))
    elif move.kind == "W":
        return dataclasses.replace(state, position=intify(state.position - move.amount))
    elif move.kind == "L":
        return dataclasses.replace(
            state, direction=intify(state.direction * (1j) ** (move.amount // 90))
        )
    elif move.kind == "R":
        return dataclasses.replace(
            state, direction=intify(state.direction * (-1j) ** (move.amount // 90))
        )
    elif move.kind == "F":
        return dataclasses.replace(
            state, position=intify(state.position + move.amount * state.direction)
        )
    else:
        raise ValueError(f"Do not understand {move}!")


def answer1(inp):
    inst = process(inp)
    state = State(1, 0)
    for i in inst:
        state = move(state, i)
    return abs(int(state.position.real)) + abs(int(state.position.imag))


tests2 = [(tests[0][0], 286)]


@dataclasses.dataclass(frozen=True)
class State2:
    position: complex
    waypoint: complex


def move2(state, move):
    if move.kind == "N":
        return dataclasses.replace(
            state, waypoint=intify(state.waypoint + 1j * move.amount)
        )
    elif move.kind == "S":
        return dataclasses.replace(
            state, waypoint=intify(state.waypoint - 1j * move.amount)
        )
    elif move.kind == "E":
        return dataclasses.replace(state, waypoint=intify(state.waypoint + move.amount))
    elif move.kind == "W":
        return dataclasses.replace(state, waypoint=intify(state.waypoint - move.amount))
    elif move.kind == "L":
        return dataclasses.replace(
            state,
            waypoint=intify(
                state.waypoint * cmath.exp(1j * 2 * cmath.pi * move.amount / 360)
            ),
        )
    elif move.kind == "R":
        return dataclasses.replace(
            state,
            waypoint=intify(
                state.waypoint * cmath.exp(-1j * 2 * cmath.pi * move.amount / 360)
            ),
        )
    elif move.kind == "F":
        return dataclasses.replace(
            state, position=intify(state.position + move.amount * state.waypoint)
        )
    else:
        raise ValueError(f"Do not understand {move}!")


def answer2(inp):
    inst = process(inp)
    state = State2(position=0, waypoint=10 + 1j)
    for i in inst:
        state = move2(state, i)
    return abs(int(state.position.real)) + abs(int(state.position.imag))


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
