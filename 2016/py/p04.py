import numpy as np
from utils import data16
import string
from collections import Counter

data = data16(4)


def check(line):
    check = line[-7:][1:-1]
    sectorid = int(line[:-7].split('-')[-1])
    c = Counter([x for x in line[:-7] if x in set(string.ascii_letters)])
    mycheck = "".join([key for key,c in sorted(c.most_common(), key=lambda x: (-x[1], x[0]))][:5])
    return mycheck == check, sectorid


tests = [("""aaaaa-bbb-z-y-x-123[abxyz]\na-b-c-d-e-f-g-h-987[abcde]\nnot-a-real-room-404[oarel]\n"totally-real-room-200[decoy]""", 1514)]


def answer1(inp):
    tot = 0
    for line in inp.splitlines():
        t, v = check(line)
        if t:
            tot += v
    return tot


def decrypt(line):
    stuff = line[:-7]
    parts = stuff.split('-')
    sectorid = int(parts[-1])
    table = str.maketrans(string.ascii_lowercase, 
            "".join(np.roll(np.array(list(string.ascii_lowercase)), -sectorid)))
    return " ".join(parts).translate(table)

tests2 = []

def answer2(inp):
    for line in data.splitlines():
        out = decrypt(line)
        if 'north' in out:
            return int(out.split()[-1])

if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  print("Answer1:", answer1(data))

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  print("Answer2:", answer2(data))
