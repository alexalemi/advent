"""
Simple parser combinator library.

Assume that the main thing is just an iterator
"""

from typing import Iterator, NamedTuple, Optional, Any, Callable, List, Sequence, Tuple
import itertools
import string
from functools import partial

ParserErrors = (ValueError, StopIteration, AssertionError)

curry = lambda f: partial(partial, f)

Datum = Any


class Result(NamedTuple):
  datum: Any
  rest: Iterator[Datum]


Tokens = Iterator[Datum]
Parser = Callable[[Tokens], Result]


def atom(atom: Datum, desc: str = None) -> Parser:
  """Match an atom exactly."""
  desc = desc or f"Atom match {atom} failed"

  def parser(s: Tokens) -> Result:
    if next(s) != atom:
      raise ValueError(desc)
    return Result(atom, s)

  return parser


def element(collection: Sequence[Datum], desc: str = None) -> Parser:
  """Efficient check for one of many datums as a set."""
  universe = set(collection)
  desc = desc or f"Element failed on {collection}."

  def parser(s: Tokens) -> Result:
    if not (c := next(s)) in universe:
      raise ValueError(desc)
    return Result(c, s)

  return parser


def alternatives(parsers: Sequence[Parser], desc: str = None) -> Parser:
  """Takes a sequence of parsers, matches on the first one."""
  desc = desc or f"Failed to match any!"
  parsers = list(parsers)

  def parser(s: Tokens) -> Result:
    for parser in parsers:
      a, b = itertools.tee(s)
      try:
        return parser(a)
      except ParserErrors:
        s = b
    else:
      raise ValueError(desc)
    return Result(None, s)

  return parser


def repeat(parser: Parser, n: int) -> Parser:
  """Repeat the parser n times."""

  def new_parser(s: Tokens) -> Result:
    results = []
    for _ in range(n):
      result, s = parser(s)
      results.append(result)
    return Result(results, s)

  return new_parser


def plus(parser: Parser) -> Parser:
  """Acts like a regex +, matches at least once but repeated."""

  def new_parser(s: Tokens) -> Result:
    result, s = parser(s)
    results = [result]
    while s:
      a, b = itertools.tee(s)
      try:
        datum, s = parser(a)
        results.append(datum)
      except ParserErrors:
        return Result(results, b)
    return Result(results, s)

  return new_parser


def optional(parser: Parser) -> Parser:
  """Acts like a regex ?, match zero or one times."""

  def new_parser(s: Tokens) -> Result:
    a, b = itertools.tee(s)
    try:
      return parser(a)
    except ParserErrors:
      return Result(None, b)

  return new_parser


def ignore(parser: Parser) -> Parser:
  """Wrap a parser so that it drops its data."""

  def new_parser(s: Tokens) -> Result:
    _, rest = parser(s)
    return Result(None, rest)

  return new_parser


def chain(*parsers: Parser, filter=lambda x: not x is None) -> Parser:
  """Chain many parsers together in a sequence."""

  def parse(s: Tokens) -> Result:
    data = []
    for parser in parsers:
      datum, s = parser(s)
      if filter(datum):
        data.append(datum)
    return Result(data, s)

  return parse


def map(parser: Parser, f: Callable) -> Parser:
  """Wrap a parser to handle some processing of the result."""

  def new_parser(s: Tokens) -> Result:
    result, rest = parser(s)
    return Result(f(result), rest)

  return new_parser


def dictionary(**parsers: Parser) -> Parser:
  """Require that we manage to match each of the parsers once in any order return a dictionary of results."""
  desc = f"Failed to match all of {parsers}"

  def parser(s: Tokens) -> Result:
    remaining = parsers.copy()
    results = {}
    while remaining:
      # try to match each of the remaining guys
      for name, parser in remaining.items():
        a, b = itertools.tee(s)
        try:
          result, s = parser(a)
          results[name] = result
          remaining.pop(name)
          break
        except ParserErrors:
          s = b
      else:
        raise ValueError(desc)
    return Result(results, s)

  return parser


## Helper utilities

join = lambda x: ''.join(x)


def test_assertion(cond):

  def f(x):
    assert cond(x)
    return x

  return f


assertion = lambda parser, cond: map(parser, test_assertion(cond))

whitespace = element(string.whitespace)
space = atom(" ")
newline = atom("\n")
letter = element(string.ascii_letters)
digit = element(string.digits)
printable = element(string.printable)
non_whitespace = element(list(set(string.printable) - set(string.whitespace)))
hex_digit = element(string.digits + "abcdef")
word = map(plus(letter), join)
integer = map(plus(digit), lambda x: int(''.join(x)))
hex_color = map(chain(atom("#"), repeat(hex_digit, 6)), lambda x: ''.join(x[1]))
exact = lambda word: map(chain(*[atom(c) for c in word]), join)
pad = lambda parser: map(chain(parser, whitespace), lambda x: x[0])


def parse(parser: Parser, s: str) -> Tuple[Any, str]:
  result, rest = parser(iter(s))
  return result, ''.join(rest)
