"""Advent of Code helper utilities, inspired by Norvig's:
https://github.com/norvig/pytudes/blob/main/ipynb/AdventUtils.ipynb
"""

import re
from typing import Callable

lines = str.splitlines


def paragraphs(text):
    "Split text into paragraphs."
    return text.split("\n\n")


def mapt(function: Callable, *sequences) -> tuple:
    """`map` with the result as a tuple."""
    return tuple(map(function, *sequences))


def parse(text, parser=str, sections=lines) -> tuple:
    """Split the input text into `sections`, and apply `parser` to each."""
    return mapt(parser, sections(text.rstrip()))


type Char = str
type Atom = str | float | int


def ints(text: str) -> tuple[int]:
    """A tuple of all the integers in the text, ignoring non-number characters."""
    return mapt(int, re.findall(r"-?[0-9]+", text))


def positive_ints(text: str) -> tuple[int]:
    return mapt(int, re.findall(r"[0-9]+", text))


def digits(text: str) -> tuple[int]:
    """A tuple of all the digits in text (as ints 0-9), ignoring non-digit characters."""
    return mapt(int, re.findall(r"[0-9]+", text))


def words(text: str) -> tuple[str]:
    """A tuple of all the alphabetic words in text, ignoring non-letters."""
    return tuple(re.findall(r"[a-zA-Z]+", text))


def atoms(text: str) -> tuple[Atom]:
    """A tuple of all the atoms (numbers or identifiers) in text. Skip punctuation."""
    return mapt(atom, re.findall(r"[+-]?\d+\?\d*|w+", text))


def atom(text: str) -> Atom:
    try:
        x = float(text)
        return round(x) if x.is_integer() else x
    except ValueError:
        return text.strip()
