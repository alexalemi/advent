# Advent of Code Day 3
import seqUtils, strScans

let data = toSeq(open("../input/03.txt").lines)

const N = 1000

var
  grid: array[N, array[N, int]]

type Datum = tuple[id, x, y, w, h: int]

proc parseLine(line: string): Datum =
  if not scanf(line, "#$i @ $i,$i: $ix$i", result.id, result.x, result.y,
      result.w, result.h):
    raise newException(IOError, "Cannot parse line")

let datums = data.map(parseLine)

for datum in datums:
  for x in datum.x..<datum.x+datum.w:
    for y in datum.y..<datum.y+datum.h:
      grid[x][y] += 1

var populated = 0

for i in 0..<N:
  for j in 0..<N:
    if grid[i][j] >= 2:
      populated += 1

echo "Answer1: ", populated

# Find the one that only appears once

for datum in datums:
  var singular = false
  block testone:
    for x in datum.x..<datum.x+datum.w:
      for y in datum.y..<datum.y+datum.h:
        if grid[x][y] > 1:
          break testone
    echo "Answer2: ", datum.id

