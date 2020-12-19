import time
from utils import data20
import itertools
import functools
from typing import Optional, List, Tuple, Dict, Sequence, Union

data = data20(19)

tests = [("""0: 4 1 5
1: 2 3 | 3 2
2: 4 4 | 5 5
3: 4 5 | 5 4
4: "a"
5: "b"

ababbb
bababa
abbbab
aaabbb
aaaabbb""", 2)]


def compile(inp):
  """Process the input into a dictionary of rules.
  
  Lists encode alternatives, tuples are sequences."""
  rules = {}
  for line in inp.splitlines():
    pk, rule = line.split(": ")
    pk = int(pk)
    if rule.startswith('"'):
      rules[pk] = rule.strip('"')
      continue
    rule = [tuple(map(int, branch.split(" "))) for branch in rule.split(" | ")]
    rule = [x[0] if len(x) == 1 else x for x in rule]
    if len(rule) == 1:
      rules[pk] = rule[0]
    else:
      rules[pk] = rule
  return rules


Rule = Union[int, str, List, Tuple]


@functools.singledispatch
def match(rule: Rule, rules: Dict[int, Rule], s: Sequence[str]):
  """Match the rule from the set of rules to the sequence of string s.
  
  The type of the rule determines its behavior.  Integers refer to another
  rule in the rules dictionary.  Strings are exact matches.
  Tuples are sequential matches and lists are for optional branching.

  The input 's' is itself a generator of possible things to continue to match.
  """
  yield None


@match.register
def _(rule: str, rules, ses):
  """Exact match."""
  for s in ses:
    if s.startswith(rule):
      yield s[len(rule):]


@match.register
def _(rule: tuple, rules, ses):
  """Tuples match sequentially."""
  stream = ses
  for cand in ses:
    stream = [cand]
    for part in rule:
      stream = match(part, rules, stream)
    yield from stream


@match.register
def _(rule: list, rules, ses):
  """Lists create alternatives."""
  tees = itertools.tee(ses, len(rule))
  for part, sub in zip(rule, tees):
    yield from match(part, rules, sub)


@match.register
def _(rule: int, rules, ses):
  """Match a rule number."""
  yield from match(rules[rule], rules, ses)


def valid(rules: Dict[int, Rule], query: str) -> bool:
  """Determines the rules match a given query."""
  return any(x == '' for x in match(0, rules, [query]))


def answer1(inp):
  rules, queries = inp.split("\n\n")
  rules = compile(rules)
  queries = queries.splitlines()
  return sum([valid(rules, q) for q in queries])


tests2 = [("""42: 9 14 | 10 1
9: 14 27 | 1 26
10: 23 14 | 28 1
1: "a"
11: 42 31
5: 1 14 | 15 1
19: 14 1 | 14 14
12: 24 14 | 19 1
16: 15 1 | 14 14
31: 14 17 | 1 13
6: 14 14 | 1 14
2: 1 24 | 14 4
0: 8 11
13: 14 3 | 1 12
15: 1 | 14
17: 14 2 | 1 7
23: 25 1 | 22 14
28: 16 1
4: 1 1
20: 14 14 | 1 15
3: 5 14 | 16 1
27: 1 6 | 14 18
14: "b"
21: 14 1 | 1 14
25: 1 1 | 1 14
22: 14 14
8: 42
26: 14 22 | 1 20
18: 15 15
7: 14 5 | 1 21
24: 14 1

abbbbbabbbaaaababbaabbbbabababbbabbbbbbabaaaa
bbabbbbaabaabba
babbbbaabbbbbabbbbbbaabaaabaaa
aaabbbbbbaaaabaababaabababbabaaabbababababaaa
bbbbbbbaaaabbbbaaabbabaaa
bbbababbbbaaaaaaaabbababaaababaabab
ababaaaaaabaaab
ababaaaaabbbaba
baabbaaaabbaaaababbaababb
abbbbabbbbaaaababbbbbbaaaababb
aaaaabbaabaaaaababaa
aaaabbaaaabbaaa
aaaabbaabbaaaaaaabbbabbbaaabbaabaaa
babaaabbbaaabaababbaabababaaab
aabbbbbaabbbaaaaaabbbbbababaaaaabbaaabba""", 12)]


def answer2(inp):
  rules, queries = inp.split("\n\n")
  rules = compile(rules)
  rules[8] = [42, (42, 8)]
  rules[11] = [(42, 31), (42, 11, 31)]
  queries = queries.splitlines()
  return sum([valid(rules, q) for q in queries])


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
