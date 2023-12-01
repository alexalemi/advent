import time
import itertools
import numpy as np
from utils import data23
import string
import regex

data = data23(1)

tests = [("""1abc2
pqr3stu8vwx
a1b2c3d4e5f
treb7uchet""", 142),]

def filter_digits(s):
    return list(x for x in s if x in string.digits)

def answer1(inp):
    lines = inp.splitlines()
    digits = ( filter_digits(line) for line in lines )
    first_and_last = ( "".join([line[0], line[-1]]) for line in digits )
    numbers = map(int, first_and_last)
    return sum(numbers)


tests2 = [("""two1nine
eightwothree
abcone2threexyz
xtwone3four
4nineeightseven2
zoneight234
7pqrstsixteen""",281)]

def filter_digits_and_words(s):
    return regex.findall("[1234567890]|one|two|three|four|five|six|seven|eight|nine", s, overlapped=True)

convert = {"one": 1, "two": 2, "three": 3, "four": 4, "five": 5, "six": 6, "seven": 7, "eight": 8, "nine": 9, 
           "1": 1, "2": 2, "3": 3, "4": 4, "5": 5, "6": 6, "7": 7, "8": 8, "9": 9, "0": 0}


def answer2(inp):
    lines = inp.splitlines()
    digits = ( filter_digits_and_words(line) for line in lines )
    numbers = ( 10 * convert[line[0]] + convert[line[-1]] for line in digits )
    return sum(numbers)


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
