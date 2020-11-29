from utils import data19
from typing import NamedTuple, Any
import jax
import jax.numpy as np
from jax import jit
from parse import parse

data = data19(12)

line = "<x={:d}, y={:d}, z={:d}>"


class State(NamedTuple):
  xs: Any
  vs: Any

  @classmethod
  def init(cls, xs):
    return cls(xs, np.zeros((4, 3), dtype="int32"))

  @classmethod
  def from_string(cls, s):
    data = parse("\n".join(line for _ in range(4)), s)
    return cls.init(np.array([data[i] for i in range(3 * 4)]).reshape((4, 3)))

  def step(self):
    dvs = np.sign(self.xs[:, None, :] -
                  self.xs[None, :, :]).astype("int32").sum(0)
    vs = self.vs + dvs
    xs = self.xs + vs
    return self.__class__(xs, vs)

  @property
  def potential_energies(self):
    return np.abs(self.xs).sum(1)

  @property
  def kinetic_energies(self):
    return np.abs(self.vs).sum(1)

  @property
  def energy(self):
    return (self.potential_energies * self.kinetic_energies).sum()

  def freeze(self):
    return tuple(self.xs.ravel()) + tuple(self.vs.ravel())


xs = np.array([[-1, 0, 2], [2, -10, -7], [4, -8, 8], [3, 5, -1]])


@jit
def steps(state: State, n: int) -> State:

  def body(i, s):
    return s.step()

  return jax.lax.fori_loop(0, n, body, state)


def equal(s1, s2):
  return np.all(np.equal(s1.xs, s2.xs)) * np.all(np.equal(s1.vs, s2.vs))


@jit
def floyd(state: State):

  def cond_fun(cstate):
    tortoise, hare = cstate
    return ~equal(tortoise, hare)

  def body_fun(cstate):
    tortoise, hare = cstate
    return (tortoise.step(), hare.step().step())

  tortoise, hare = jax.lax.while_loop(cond_fun, body_fun,
                                      (state.step(), state.step().step()))

  def cond_fun(cstate):
    mu, tortoise, hare = cstate
    return ~equal(tortoise, hare)

  def body_fun(cstate):
    mu, tortoise, hare = cstate
    return (mu + 1, tortoise.step(), hare.step())

  mu, tortoise, hare = jax.lax.while_loop(cond_fun, body_fun, (0, state, hare))

  def cond_fun(cstate):
    lam, hare = cstate
    return ~equal(tortoise, hare)

  def body_fun(cstate):
    lam, hare = cstate
    return (lam + 1, hare.step())

  lam, hare = jax.lax.while_loop(cond_fun, body_fun, (0, tortoise.step()))
  return mu, lam


state = State.init(xs)

tests = []


def answer1(inp):
  s = state.from_string(inp)
  s = steps(s, 1000)
  return s.energy


s2 = """<x=-1, y=0, z=2>
<x=2, y=-10, z=-7>
<x=4, y=-8, z=8>
<x=3, y=5, z=-1>"""

s3 = """<x=-8, y=-10, z=0>
<x=5, y=5, z=10>
<x=2, y=-7, z=3>
<x=9, y=-8, z=-3>"""

tests2 = []
# (s2, 2772),]
# (s3, 4686774924)]


def answer2(inp):
  return recur(state.from_string(inp))


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  # print("Answer2:", answer2(data))
