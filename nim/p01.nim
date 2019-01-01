# Advent of Code Day 1

import strutils, intsets

proc loadFile(flname: string): seq[int] =
  for line in open(flname).lines:
    result.add(line.parseInt)

func sum[T](x: seq[T]): T =
  for elem in x:
    result += elem

let data = loadFile("../input/01.txt")

echo "Answer1: ", data.sum

#[ now we need to find the first value that
  repeats 
]#

proc loopFinder(data: seq[int]): int =
  var loc = 0
  var sum = 0
  var seen = initIntSet()
  seen.incl(0)
  while true:
    sum += data[loc mod data.len]
    if seen.containsOrIncl(sum):
      return sum
    loc += 1

assert loopFinder(@[+1, -2, +3, +1]) == 2
assert loopFinder(@[+1, -1]) == 0
assert loopFinder(@[+3, +3, +4, -2, -4]) == 10
assert loopFinder(@[-6, +3, +8, +5, -6]) == 5
assert loopFinder(@[+7, +7, -2, -7, -4]) == 14

echo "Answer2: ", data.loopFinder


