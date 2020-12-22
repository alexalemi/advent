import time
import itertools
from utils import data20
from typing import Tuple, List
from collections import deque

data = data20(22)

tests = [("""Player 1:
9
2
6
3
1

Player 2:
5
8
4
7
10""", 306)]


def process(inp: str) -> Tuple[List[int], List[int]]:
  players = inp.split('\n\n')
  decks = []
  for player in players:
    deck = []
    for line in player.splitlines()[1:]:
      deck.append(int(line))
    decks.append(deck[::-1])
  return tuple(decks)


def play_round(state):
  adeck, bdeck = state
  a = adeck.pop()
  b = bdeck.pop()
  if a > b:
    adeck[:0] = [b, a]
  elif b > a:
    bdeck[:0] = [a, b]
  else:
    raise ValueError("Don't know how to handle ties.")
  return (adeck, bdeck)


def finished(state):
  if len(state[0]) == 0:
    return 1
  elif len(state[1]) == 0:
    return 0


def score(state):
  p1_score = sum([x * y for x, y in zip(state[0], itertools.count(1))])
  p2_score = sum([x * y for x, y in zip(state[1], itertools.count(1))])
  return (p1_score, p2_score)


def winner(state):
  if len(state[0]) == 0:
    return 1
  elif len(state[1]) == 0:
    return 0


def game(state):
  while (winner := finished(state)) is None:
    state = play_round(state)
  return winner, state


def answer1(inp):
  state = process(inp)
  winner, state = game(state)
  return score(state)[winner]


tests2 = [(tests[0][0], 291)]


def freeze(state):
  return tuple(map(tuple, state))


def finisher():
  seen = set()
  def _(state):
    frozen = freeze(state)
    if frozen in seen:
      return 0
    seen.add(frozen)
    return finished(state)

  return _


def play_round2(state):
  adeck, bdeck = state
  a = adeck.pop()
  b = bdeck.pop()
  if a <= len(adeck) and b <= len(bdeck):
    winner, _ = game2((adeck[-a:], bdeck[-b:]))
  elif a > b:
    winner = 0
  elif b > a:
    winner = 1
  else:
    raise ValueError("Don't know how to handle ties.")

  if winner == 0:
    adeck[:0] = [b, a]
  else:
    bdeck[:0] = [a, b]
  return (adeck, bdeck)


def game2(state):
  finished = finisher()
  while (winner := finished(state)) is None:
    state = play_round2(state)
  return winner, state


def answer2(inp):
  state = process(inp)
  winner, state = game2(state)
  return score(state)[winner]


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
