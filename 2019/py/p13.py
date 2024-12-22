import intcode
from utils import data19
import numpy as np
import os
import time

data = data19(13)

tests = []

# 0 is an empty tile. No game object appears in this tile.
# 1 is a wall tile. Walls are indestructible barriers.
# 2 is a block tile. Blocks can be broken by the ball.
# 3 is a horizontal paddle tile. The paddle is indestructible.
# 4 is a ball tile. The ball moves diagonally and bounces off objects.


def update(state, new):
    newdata = np.array(new).reshape((-1, 3))
    for x, y, z in newdata:
        state[(x, y)] = z
    return state


def render(state):
    blocks = " █░▭○"
    score = state[(-1, 0)]
    for y in range(25):
        for x in range(41):
            print(blocks[state[(x, y)]], end="")
        print()
    print(f"Score: {score}", flush=True)
    return state


def answer1(inp):
    comp = intcode.Computer(intcode.getcodes(inp))
    result = comp.run()
    return sum([x == 2 for x in result[2::3]])


tests2 = []


def cmp(a, b):
    return 1 * (a > b) - 1 * (a < b)


def answer2(inp, show=False):
    prog = intcode.getcodes(inp)
    prog[0] = 2
    comp = intcode.Computer(prog)
    new = comp.run()
    state = {}

    while not comp.finished:
        state = update(state, new)
        if show:
            os.system("clear")
            render(state)
            time.sleep(1 / 60)

        ballpos = next(pos for pos, val in state.items() if val == 4)
        paddlepos = next(pos for pos, val in state.items() if val == 3)
        move = cmp(ballpos[0], paddlepos[0])
        new = comp.run(move)
    state = update(state, new)
    return state[(-1, 0)]


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    print("Answer1:", answer1(data))

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    print("Answer2:", answer2(data, True))
