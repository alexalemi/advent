import os
import strUtils
import seqUtils
import times

const data = staticRead(currentSourcePath.parentDir() / "../input/15.txt")

type Num = uint32

func sequence(start: seq[int], top: Num): Num {.inline.} =
  var num: Num = 0
  var prev: Num = 0
  var lastSeen = newSeq[Num](top)

  num = 0
  for i, x in start:
    prev = num
    num = x.Num
    if i > 0:
      lastSeen[prev] = i.Num

  for j in start.len.Num..<top:
    prev = num
    if lastSeen[num] == 0:
      num = 0
    else:
      num = j - lastSeen[num]
    lastSeen[prev] = j

  return num


func answer(inp: string, top: Num): Num {.inline.} =
  let start = inp.strip.split(",").map(parseInt)
  return sequence(start, top)


when isMainModule:
  const n1 = 2020

  assert answer("0,3,6", n1) == 436
  assert answer("1,3,2", n1) == 1
  assert answer("2,1,3", n1) == 10
  assert answer("1,2,3", n1) == 27
  assert answer("2,3,1", n1) == 78
  assert answer("3,2,1", n1) == 438
  assert answer("3,1,2", n1) == 1836

  var time = cpuTime()
  echo "Answer1: ", answer(data, n1), " in ", cpuTime() - time, " secs"

  const n2 = 30_000_000
  assert answer("0,3,6", n2) == 175594
  assert answer("1,3,2", n2) == 2578
  assert answer("2,1,3", n2) == 3544142
  assert answer("1,2,3", n2) == 261214
  assert answer("2,3,1", n2) == 6895259
  assert answer("3,2,1", n2) == 18
  assert answer("3,1,2", n2) == 362

  time = cpuTime()
  echo "Answer2: ", answer(data, n2), " in ", cpuTime() - time, " secs"
