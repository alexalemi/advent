import os
import strUtils
import seqUtils

const data = staticRead(currentSourcePath.parentDir() / "../input/15.txt")

proc seq(start: seq[int], top: int): int =
  var j = 0
  var num = 0
  var lastSeen = newSeq[int](top)
  for i, x in start:
    j += 1
    num = x
    lastSeen[x] = j
    echo i, ":", num

  for j in j..<top:
    if lastSeen[num] == j-1:
      num = 0
    else:
      num = j - lastSeen[num]
    echo j, ":", num
    lastSeen[num] = j

  return num


proc answer1(inp: string, top: int): int =
  let start = inp.strip.split(",").map(parseInt)
  return seq(start, top)


when isMainModule:
  echo answer1("0,3,6", 10) # == 436

  # echo "Answer1: ", answer1(data, 2020)
