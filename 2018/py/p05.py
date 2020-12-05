import collections
import string

with open("../input/05.txt") as f:
  text = f.read().strip()


def match(a, b):
  return abs(ord(a) - ord(b)) == 32


def react(st):
  if not st:
    return st
  new = []
  old = collections.deque(st)
  new.append(old.popleft())
  while old:
    if new and match(new[-1], old[0]):
      new.pop()
      old.popleft()
    else:
      new.append(old.popleft())
  return ''.join(new)


tests = [
    ("dabAcCaCBAcCcaDA", 10),
    ("aA", 0),
    ("abBA", 0),
    ("abAB", 4),
    ("aabAAB", 6),
]


def answer1(inp):
  return len(react(inp))


for inp, ans in tests:
  assert (out := answer1(inp)) == ans, f"Failed example {inp} -> {out} != {ans}"

print("Answer: ", answer1(text), flush=True)


def answer2(inp):
  best = float('inf')
  for letter in string.ascii_lowercase:
    test = inp.replace(letter, '').replace(letter.upper(), '')
    result = answer1(test)
    best = min(best, result)
  return best


print('Answer2: ', answer2(text))
