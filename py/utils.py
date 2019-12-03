# Lint as: python3
"""Utility functions for Advent of Code."""

import logging
import sys
import functools

logging.basicConfig(level=logging.INFO)

from pathlib import Path

def get_data(day=None, year=None):
  p = Path('..')
  if year is not None:
    p = p / f"{year}"
  p = p / "input" / f"{day:02d}.txt"
  try:
    logging.info(f"Attempting to load {p}...")
    with open(p) as f:
      return f.read()
  except FileNotFoundError:
    logging.info("File not found, attempting to download.")


data19 = functools.partial(get_data, year=2019)

if __name__ == "__main__":
  logging.basicConfig(level=logging.INFO)

  print(get_data(2))   

