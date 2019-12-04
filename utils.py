#! /usr/bin/env python3
"""Utility functions for Advent of Code."""

import datetime
import functools
import logging
import os
from pathlib import Path
import pytz
import sys

logging.basicConfig(level=logging.INFO)

EAST_COAST = pytz.timezone("America/New_York")
TODAY = datetime.datetime.today().astimezone(EAST_COAST)

with open(Path(__file__).resolve().parent / "token.txt") as f:
  TOKEN = f.read().strip()


def get_data(day=None, year=None):
  p = Path(__file__).resolve().parent
  year = year or TODAY.year
  if year is not None:
    p = p / f"{year}"
  p = p / "input" / f"{day:02d}.txt"
  try:
    logging.info(f"Attempting to load {p}...")
    with open(p) as f:
      return f.read()
  except FileNotFoundError:
    pass

  # See if the requested day is available
  wanted = datetime.datetime(
    year=year, month=12, day=day, hour=0, minute=0, second=0, tzinfo=EAST_COAST)
  if wanted > TODAY:
    logging.error(f"Requesting a file in the future: {wanted - TODAY} from now!")
    sys.exit(1)
  elif day > 25:
    logging.error("Day not in Advent!")
    sys.exit(1)

  logging.info("File not found, attempting to download.")
  import urllib.request
  import urllib.error
  import shutil

  url = f"https://adventofcode.com/{year}/day/{day}/input"
  req = urllib.request.Request(url)
  req.add_header("Cookie", f"session={TOKEN}")
  try:
    logging.info(f"Requesting data from {url}...")
    with urllib.request.urlopen(req) as r:
      if not p.parent.exists():
        logging.info(f"Creating directory {p.parent}")
        os.makedirs(p.parent)
      with open(p, "wb") as f:
        shutil.copyfileobj(r, f)
      logging.info(f"Input saved at {p}")
      return open(p).read()
  except urllib.error.HTTPError as e:
    status_code = e.getcode()
    if status_code == 400:
      logging.error("Auth failed!")
      sys.exit(1)
    elif status_code == 404:
      logging.error("Day is not out yet???")
      sys.exit(1)
    else:
      logging.error(f"Request failed with code: {code}??")
      sys.exit(1)


data19 = functools.partial(get_data, year=2019)
data18 = functools.partial(get_data, year=2018)
data17 = functools.partial(get_data, year=2017)
data16 = functools.partial(get_data, year=2016)
data15 = functools.partial(get_data, year=2015)

if __name__ == "__main__":
  logging.basicConfig(level=logging.INFO)

  if len(sys.argv) > 2:
    year = int(sys.argv[2])
  else:
    year = TODAY.year

  if len(sys.argv) > 1:
    day = int(sys.argv[1])
  else:
    if TODAY.month != 12 and TODAY.day > 25:
      logging.error("It isn't Advent!")
      sys.exit(1)
    day = TODAY.day

  print(get_data(day, year))

