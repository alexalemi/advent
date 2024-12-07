#! /usr/bin/env python3
"""Utility functions for Advent of Code."""

import urllib.request
import urllib.error
import shutil
import tqdm
import argparse
import datetime
import functools
import logging
import os
import json
from pathlib import Path
import pytz
import sys
import time
from itertools import tee
import itertools
import operator
from collections import namedtuple, defaultdict

logging.basicConfig(level=logging.INFO)


USER_AGENT = "github.com/alexalemi/advent by alexalemi@gmail.com v2"
EAST_COAST = pytz.timezone("America/New_York")
TODAY = datetime.datetime.now().astimezone(EAST_COAST)
# YEARS = [2015, 2016, 2017, 2018, 2019, 2020, 2021, 2022, 2023, 2024]
YEARS = [2024]
DEFAULT_TOKEN = open(Path(__file__).resolve().parent / "../.token", "r")
# B_TOKEN = open(Path(__file__).resolve().parent / ".tokenb", 'r')

REPLACED_NAMES = {"pleonasticperson": "Colin Clement"}
IGNORED_NAMES = {"pleonasticperson"}


def pairwise(iterable):
    "s -> (s0,s1), (s1,s2), (s2, s3), ..."
    a, b = tee(iterable)
    next(b, None)
    return zip(a, b)


def threewise(iterable):
    "s -> (s0,s1), (s1,s2), (s2, s3), ..."
    a, b, c = tee(iterable, 3)
    next(b, None)
    next(c, None)
    next(c, None)
    return zip(a, b, c)


def unique_justseen(iterable, key=None):
    "List unique elements, preserving order. Remember only the element just seen."
    # unique_justseen('AAAABBBCCDAABBB') --> A B C D A B
    # unique_justseen('ABBCcAD', str.lower) --> A B C A D
    return map(next, map(operator.itemgetter(1), itertools.groupby(iterable, key)))


def get_data(day=None, year=None, token=DEFAULT_TOKEN, suffix=""):
    p = Path(__file__).resolve().parent
    year = year or TODAY.year
    if year is not None:
        p = p / f".." / f"{year}"
    p = p / "input" / f"{day:02d}{suffix}.txt"
    try:
        logging.info(f"Attempting to load {p}...")
        with open(p) as f:
            return f.read()
    except FileNotFoundError:
        pass

    # See if the requested day is available
    wanted = datetime.datetime(
        year=year, month=12, day=day, hour=0, minute=0, second=0, tzinfo=EAST_COAST
    )
    if wanted > TODAY:
        logging.error(f"Requesting a file in the future: {wanted - TODAY} from now!")
        sys.exit(1)
    elif day > 25:
        logging.error("Day not in Advent!")
        sys.exit(1)

    logging.info("File not found, attempting to download.")

    url = f"https://adventofcode.com/{year}/day/{day}/input"
    req = urllib.request.Request(url)
    TOKEN = token.read().strip()
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


data23 = functools.partial(get_data, year=2023)
data22 = functools.partial(get_data, year=2022)
data21 = functools.partial(get_data, year=2021)
data20 = functools.partial(get_data, year=2020)
data19 = functools.partial(get_data, year=2019)
data18 = functools.partial(get_data, year=2018)
data17 = functools.partial(get_data, year=2017)
data16 = functools.partial(get_data, year=2016)
data15 = functools.partial(get_data, year=2015)

LEADERBOARD = Path(__file__).resolve().parent / ".leaderboard.txt"

# original token
FRIENDS = "173774"
SAL = "851286"
GOOGLE = "275172"
# B token
DISCORD = "1575826"

BOARDS = [(FRIENDS, DEFAULT_TOKEN), 
          (SAL, DEFAULT_TOKEN),]

# BOARDS = [(SAL, DEFAULT_TOKEN)]


def request_leaderboard(year, num, token=DEFAULT_TOKEN):
    url = f"https://adventofcode.com/{year}/leaderboard/private/view/{num}.json"
    req = urllib.request.Request(url)
    req.add_header("Cookie", f"session={token}")
    req.add_header("User-Agent", USER_AGENT)
    try:
        logging.info(f"Requesting leaderboard from {url}...")
        with urllib.request.urlopen(req) as r:
            data = r.read()
            print(f"Recieved {data[:30]}...")
            try:
                json.loads(data)
                return data
            except:
                logging.error(f"Failure on {url}...")
    except urllib.error.HTTPError as e:
        status_code = e.getcode()
        if status_code == 400:
            logging.error("Auth failed!")
            sys.exit(1)
        elif status_code == 404:
            logging.error("Day is not out yet???")
            sys.exit(1)
        else:
            logging.error(f"Request failed with code: {status_code}??")
            sys.exit(1)



def get_leaderboards(boards=BOARDS, force=False):
    modtime = datetime.datetime.fromtimestamp(LEADERBOARD.stat().st_mtime).astimezone(
        EAST_COAST
    )
    if force or TODAY - modtime > datetime.timedelta(seconds=900):
        import urllib.request
        import urllib.error

        with open(LEADERBOARD, "wb") as f:
            count = 0
            for num, token in boards:
                TOKEN = token.read().strip()
                for year in YEARS:
                    if count > 0:
                        for _ in tqdm.trange(100):
                            time.sleep(0.1337)
                    count += 1
                    data = request_leaderboard(year, num)
    else:
        logging.info(
            f"Not enough time as elapsed to justify a new call. {(900 - (TODAY - modtime).total_seconds())/60:0.1f} minutes left."
        )
    with open(LEADERBOARD) as f:
        data = [json.loads(line) for line in f if line]
        return data


Event = namedtuple("Event", "name time year day star")


def canonicalize_name(name):
    return REPLACED_NAMES.get(name, name)


def recent_events(data):
    """Print a summary of recent events."""
    events = []
    for yeardata in data:
        year = yeardata["event"]
        for member in yeardata["members"].values():
            name = member.get("name") or member["id"]
            for day, level in member["completion_day_level"].items():
                for star, data in level.items():
                    if name not in IGNORED_NAMES:
                        events.append(
                            Event(
                                name=canonicalize_name(name),
                                time=datetime.datetime.fromtimestamp(
                                    float(data["get_star_ts"])
                                ).astimezone(EAST_COAST),
                                year=int(year),
                                day=int(day),
                                star=int(star),
                            )
                        )
    return list(unique_justseen(sorted(events, key=lambda x: x.time, reverse=True)))


def total_leaderboard(events):
    events = sorted(events, key=lambda x: x.name)
    byname = itertools.groupby(events, key=lambda x: x.name)
    return {name: sum(1 for x in events) for name, events in byname}


def global_score(events):
    starid = lambda x: (x.year, x.day, x.star)
    N = len({x.name for x in events})
    stargroups = itertools.groupby(sorted(events, key=starid), key=starid)
    points = (
        (event.name, N - i)
        for star, events in stargroups
        for (i, event) in enumerate(events)
    )
    total_score = defaultdict(int)
    for name, pts in points:
        total_score[name] += pts
    return total_score


if __name__ == "__main__":
    logging.basicConfig(level=logging.INFO)

    parser = argparse.ArgumentParser(description="advent of code utilites.")
    parser.add_argument(
        "--force",
        dest="force",
        action="store_true",
        default=False,
        help="force a refresh (default: False)",
    )
    subparsers = parser.add_subparsers(dest="command")

    # create the parser for the "fetch" command
    parser_fetch = subparsers.add_parser("fetch", help="Fetches data files.")
    parser_fetch.add_argument(
        "day", nargs="?", type=int, default=TODAY.day, help="Day to download"
    )
    parser_fetch.add_argument("--year", type=int, default=TODAY.year, help="year")
    parser_fetch.add_argument(
        "--token", type=argparse.FileType("r"), default=open(".token", "r")
    )
    parser_fetch.add_argument(
        "--suffix", type=str, default="", help="input file suffix (default: none)"
    )

    args = parser.parse_args()

    if args.command == "fetch":
        # We are trying to download data.
        get_data(args.day, args.year, args.token, args.suffix)

    else:
        data = get_leaderboards(boards=BOARDS, force=args.force)

        events = recent_events(data)

        score = global_score(events)
        leaderboard = total_leaderboard(events)

        print("\nLEADERBOARD\n==============================")
        for name, pts in sorted(score.items(), key=lambda x: x[1], reverse=True):
            print(f"{pts:4d} pts - {leaderboard[name]:3d} stars - {name}")

        print("\n\nRECENT EVENTS\n===========================")
        for x in events[:15]:
            print(
                    f"{x.name:10} solved {x.year}-{x.day:02d}-{x.star} {(TODAY-x.time).total_seconds()/(60*60):05.2f} hours ago at {x.time}"
            )
