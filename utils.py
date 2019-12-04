#! /usr/bin/env python3
"""Utility functions for Advent of Code."""

import datetime
import functools
import logging
import os
import json
from pathlib import Path
import pytz
import sys
from itertools import tee
import itertools
from collections import namedtuple, defaultdict

logging.basicConfig(level=logging.INFO)

EAST_COAST = pytz.timezone("America/New_York")
TODAY = datetime.datetime.now().astimezone(EAST_COAST)
YEARS = [2015, 2016, 2017, 2018, 2019]

REPLACED_NAMES = {'pleonasticperson': 'Colin Clement'}
IGNORED_NAMES = {'pleonasticperson'}

def pairwise(iterable):
    "s -> (s0,s1), (s1,s2), (s2, s3), ..."
    a, b = tee(iterable)
    next(b, None)
    return zip(a, b)

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

LEADERBOARD = Path(__file__).resolve().parent / "leaderboard.txt"

def get_leaderboard(force=False):
  modtime = datetime.datetime.fromtimestamp(LEADERBOARD.stat().st_mtime).astimezone(EAST_COAST)
  if force or TODAY - modtime > datetime.timedelta(seconds=900):
      import urllib.request
      import urllib.error
      with open(LEADERBOARD, 'wb') as f:
          for year in YEARS:
              url = f"https://adventofcode.com/{year}/leaderboard/private/view/173774.json"
              req = urllib.request.Request(url)
              req.add_header("Cookie", f"session={TOKEN}")
              try:
                  logging.info(f"Requesting leaderboard from {url}...")
                  with urllib.request.urlopen(req) as r:
                      f.write(r.read())
                      f.write(b'\n')
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
  else:
      logging.info("Not enough time as elapsed to justify a new call.")
  with open(LEADERBOARD) as f:
      data = [json.loads(line) for line in f]
      return data

Event = namedtuple('Event', 'name time year day star')


def canonicalize_name(name):
    return REPLACED_NAMES.get(name, name)

def recent_events(data):
    """Print a summary of recent events."""
    events = []
    for yeardata in data:
        year = yeardata['event']
        for member in yeardata['members'].values():
            name = member['name']
            for day, level in member['completion_day_level'].items():
                for star, data in level.items():
                    if name not in IGNORED_NAMES:
                        events.append(Event(
                            name=canonicalize_name(name),
                            time=datetime.datetime.fromtimestamp(float(data['get_star_ts'])).astimezone(EAST_COAST),
                            year=int(year),
                            day=int(day),
                            star=int(star)))
    return sorted(events, key=lambda x: x.time, reverse=True)


def total_leaderboard(events):
    events = sorted(events, key=lambda x: x.name)
    byname = itertools.groupby(events, key=lambda x: x.name)
    return {name: sum(1 for x in events) for name, events in byname}

def global_score(events):
    starid = lambda x: (x.year, x.day, x.star)
    N = len({x.name for x in events})
    stargroups = itertools.groupby(sorted(events, key=starid), key=starid)
    points = ((event.name, N-i) for star, events in stargroups for (i, event) in enumerate(events))
    total_score = defaultdict(int)
    for name, pts in points:
        total_score[name] += pts
    return total_score


if __name__ == "__main__":
  logging.basicConfig(level=logging.INFO)

  data = get_leaderboard()
  events = recent_events(data)

  print("\n\nRECENT EVENTS\n===========================")
  for x in events[:15]:
      print(f"{x.name} solved {x.year}-{x.day}-{x.star} {(TODAY-x.time).total_seconds()/(60*60):.2f} hours ago at {x.time}")


  score = global_score(events)
  leaderboard = total_leaderboard(events)

  print("\n\nLEADERBOARD\n==============================")
  for (name, pts) in sorted(score.items(), key=lambda x: x[1], reverse=True):
      print(f"{pts:4d} pts - {leaderboard[name]:3d} stars - {name}")


