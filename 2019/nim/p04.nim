# Advent of Code Day 4

import strUtils

func sixDigit(x: string): bool = len($x) == 6

const bottom = 178416
const top = 676461

func inRange(x: string): bool =
  let y = parseInt(x)
  (bottom <= y) and (y <= top)

func twoSame(x: string): bool =
  for i, c in x[1..x.high].pairs:
    if x[i] == c: return true
  return false

func increasing(x: string): bool =
  for i, c in x[1..x.high].pairs:
    if c < x[i]: return false
  return true

func valid(x: string): bool =
  x.inRange and x.twoSame and x.increasing

func answer1(): int =
  for i in bottom..top:
    if valid($i):
      result += 1

func onlyTwoSame(x: string): bool =
  for i, c in x[1..x.high].pairs:
    if c == x[i]:
      let prevprev = if i > 0: x[i-1] else: '~'
      let next = if i < 4: x[i+2] else: '~'
      if (c != prevprev) and (c != next):
        return true
  return false

func valid2(x: string): bool =
  x.inRange and x.onlyTwoSame and x.increasing

func answer2(): int =
  for i in bottom..top:
    if valid2($i):
      result += 1


when isMainModule:
  echo "Answer1: ", answer1()
  echo "Answer2: ", answer2()


