import time
import itertools
from utils import data20
import string

data = data20(4)

# byr (Birth Year)
# iyr (Issue Year)
# eyr (Expiration Year)
# hgt (Height)
# hcl (Hair Color)
# ecl (Eye Color)
# pid (Passport ID)
# cid (Country ID)

tests = [("""ecl:gry pid:860033327 eyr:2020 hcl:#fffffd
byr:1937 iyr:2017 cid:147 hgt:183cm

iyr:2013 ecl:amb cid:350 eyr:2023 pid:028048884
hcl:#cfa07d byr:1929

hcl:#ae17e1 iyr:2013
eyr:2024
ecl:brn pid:760753108 byr:1931
hgt:179cm

hcl:#cfa07d eyr:2025 pid:166559648
iyr:2011 ecl:brn hgt:59in""", 2)]

required_fields = ('byr', 'iyr', 'eyr', 'hgt', 'hcl', 'ecl', 'pid')


def process(inp):
  return list(map(make, inp.split("\n\n")))


def make(line):
  return dict(part.split(":") for part in line.strip().split())


def consume(str, chrs):
  return ''.join(itertools.dropwhile(lambda x: x in set(chrs), str))


def valid(pw):
  if not all(f in pw for f in required_fields):
    return False
  return True


def valid2(pw, debug=False):
  if debug:
    debug = print
  else:
    debug = lambda x: None
  debug(f"Checking {pw}")
  if not valid(pw):
    debug(f"Bad fields.")
    return False

  if not (1920 <= int(pw['byr']) <= 2002):
    debug(f"Bad birth year: {pw['byr']}")
    return False

  if not (2010 <= int(pw['iyr']) <= 2020):
    debug(f"Bad issue year: {pw['iyr']}")
    return False

  if not (2020 <= int(pw['eyr']) <= 2030):
    debug(f"Bad expire year: {pw['eyr']}")
    return False

  if (height := pw['hgt']).endswith('cm'):
    if not (150 <= int(height[:-2]) <= 193):
      debug(f"Bad cm height: {pw['hgt']}")
      return False
  elif (height := pw['hgt']).endswith('in'):
    if not (59 <= int(height[:-2]) <= 76):
      debug(f"Bad in height: {pw['hgt']}")
      return False
  else:
    debug(f"Bad height: {pw['hgt']}")
    return False

  if not pw['hcl'].startswith('#'):
    debug(f"Bad hcl, no#: {pw['hcl']}")
    return False
  if len(pw['hcl']) != 7:
    debug(f"Bad hcl, not 6: {pw['hcl']}")
    return False
  if consume(pw['hcl'][1:], '0123456789abcdef'):
    debug(f"Bad hcl, not hex: {pw['hcl']}")
    return False

  if not (pw['ecl'] in set(('amb', 'blu', 'brn', 'gry', 'grn', 'hzl', 'oth'))):
    debug(f"Bad ecl: {pw['ecl']}")
    return False

  if len(pw['pid']) != 9:
    debug(f"Bad pid not 9: {pw['pid']}")
    return False
  if consume(pw['pid'], string.digits):
    debug(f"Bad pid not digits: {pw['pid']}")
    return False

  return True


def answer1(inp):
  return sum(map(valid, process(inp)))


tests2 = [("""eyr:1972 cid:100
hcl:#18171d ecl:amb hgt:170 pid:186cm iyr:2018 byr:1926

iyr:2019
hcl:#602927 eyr:1967 hgt:170cm
ecl:grn pid:012533040 byr:1946

hcl:dab227 iyr:2012
ecl:brn hgt:182cm pid:021572410 eyr:2020 byr:1992 cid:277

hgt:59cm ecl:zzz
eyr:2038 hcl:74454a iyr:2023
pid:3556412378 byr:2007""", 0),
          ("""pid:087499704 hgt:74in ecl:grn iyr:2012 eyr:2030 byr:1980
hcl:#623a2f

eyr:2029 ecl:blu cid:129 byr:1989
iyr:2014 pid:896056539 hcl:#a97842 hgt:165cm

hcl:#888785
hgt:164cm byr:2001 iyr:2015 cid:88
pid:545766238 ecl:hzl
eyr:2022

iyr:2010 hgt:158cm hcl:#b6652a ecl:blu byr:1944 eyr:2021 pid:093154719""", 4)]


def answer2(inp):
  return sum(map(valid2, process(inp)))


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
