import re
import time
import string
from utils import data20

data = data20(18)

tests = [("1 + 2 * 3 + 4 * 5 + 6", 71), ("1 + (2 * 3) + (4 * (5 + 6))", 51),
         ("2 * 3 + (4 * 5)", 26), ("5 + (8 * 3 + 9 + 3 * 4 * 3)", 437),
         ("5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))", 12240),
         ("((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2", 13632)]


def operator(inp):
  if inp.startswith('+'):
    return '+', inp[1:]
  elif inp.startswith('*'):
    return '*', inp[1:]
  return False, inp


def eat_space(inp):
  while inp and inp[0] in set(string.whitespace):
    inp = inp[1:]
  return inp


def evaluate(op, a, b):
  if op == '*':
    return a * b
  elif op == '+':
    return a + b
  raise ValueError(f"Do not understand {op}!")


def expression(inp):
  # must be a number
  a, rest = number(inp.strip())
  answer = a
  while eat_space(rest) and not rest.startswith(')'):
    op, rest = operator(eat_space(rest))
    b, rest = number(eat_space(rest))
    answer = evaluate(op, answer, b)
  return answer, rest


def number(inp, inner=expression):
  if inp.startswith('('):
    # Inside a parenthesis
    answer, rest = inner(inp[1:])
    assert rest.startswith(')')
    return answer, rest[1:]

  match = re.match("(\d+)", inp)
  if match:
    return int(match.group()), inp[match.end():]
  return False, inp


def answer1(inp):
  return sum([expression(line)[0] for line in inp.strip().splitlines()])


tests2 = [("1 + 2 * 3 + 4 * 5 + 6", 231), ("1 + (2 * 3) + (4 * (5 + 6))", 51),
          ("2 * 3 + (4 * 5)", 46), ("5 + (8 * 3 + 9 + 3 * 4 * 3)", 1445),
          ("5 * 9 * (7 * 3 * 3 + 9 * 3 + (8 + 6 * 4))", 669060),
          ("((2 + 4 * 9) * (6 + 9 * 8 + 6) + 6) + 2 + 4 * 2", 23340)]


def product(inp):
  """Find a product expression."""
  a, rest = summand(inp.strip())
  answer = a
  while eat_space(rest).startswith('*'):
    op, rest = operator(eat_space(rest))
    b, rest = summand(eat_space(rest))
    answer = answer * b
  return answer, rest


def summand(inp):
  """Find a summand."""
  a, rest = number(inp.strip(), product)
  answer = a
  while eat_space(rest).startswith('+'):
    op, rest = operator(eat_space(rest))
    b, rest = number(eat_space(rest), product)
    answer = answer + b
  return answer, rest


def answer2(inp):
  return sum([product(line)[0] for line in inp.strip().splitlines()])


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
