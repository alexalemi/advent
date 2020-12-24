import itertools
import math
import time
import tqdm
from typing import NamedTuple
from utils import data20

data = data20(23)

tests = [("389125467", "67384529")]


class State(NamedTuple):
  current: int
  children: list

  def child(self, loc: int) -> int:
    return self.children[loc]

  def read(self, n: int, start: int = None) -> list:
    current = start or self.current
    seen = []
    for _ in range(n):
      current = self.children[current]
      seen.append(current)
    return seen

  @property
  def n(self):
    return len(self.children) - 1

  def dec(self, x: int) -> int:
    """Decrement with wraparound."""
    return x - 1 or self.n

  def move(self):
    # get hand
    children = self.children
    hand = self.read(3)
    # get the insertion point
    new = self.dec(self.current)
    while new in hand:
      new = self.dec(new)
    # remove and insert hand
    (children[self.current], children[new],
     children[hand[-1]]) = (children[hand[-1]], hand[0], children[new])
    return self._replace(current=children[self.current], children=children)

  def __repr__(self):
    return ''.join(map(str, [self.current] + self.read(self.n - 1)))


def process(inp: str, n=None) -> list:
  """Represent the deck as a list of the child
  of each node.  I.e. if the 3rd element is 5 that 
  means that 5 follows 3 in the ordering.
  
  Starts at 0 so we don't have to deal
  with 1 based indexing.

  Pad with additional numbers up to n at the end, if given.
  """
  deck = list(map(int, inp.strip()))
  if n is not None:
    deck = list(itertools.chain(deck, range(len(deck) + 1, n + 1)))
  children = [-1] * (len(deck) + 1)
  prev = deck[-1]
  for elem in deck:
    children[prev], prev = elem, elem
  children[deck[-1]] = deck[0]
  return State(current=deck[0], children=children)


def finalize(state: State) -> str:
  return ''.join(map(str, state.read(state.n - 1, start=1)))


def answer1(inp):
  state = process(inp)
  for _ in range(100):
    state = state.move()
  return finalize(state)


tests2 = [(tests[0][0], 149245887792)]


def finalize2(state: State) -> int:
  values = state.read(2, start=1)
  print(f"Final values = {values}")
  return math.prod(values)


def answer2(inp):
  state = process(inp, 1_000_000)
  assert state.n == 1_000_000, "Wrong size!"
  assert set(state.children[1:]) == set(range(1, 1_000_000 +
                                              1)), "Not all numbers."
  for _ in tqdm.trange(10_000_000):
    state = state.move()
  return finalize2(state)


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
