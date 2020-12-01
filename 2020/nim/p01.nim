import seqUtils
import strUtils
import os

const data = staticRead(currentSourcePath.parentDir() / "../input/01.txt")

proc answer1(inp: string): int =
  let data = inp.strip.split.map(parseInt)
  var s: set[uint16]
  for num in data:
    incl(s, num.uint16)

  for num in data:
    if (2020 - num).uint16 in s:
      return num * (2020 - num)

proc answer2(inp: string): int =
  let data = inp.strip.split.map(parseInt)
  var s: set[uint16]
  for num in data:
    incl(s, num.uint16)

  for i, num in data:
    for j, num2 in data[i .. ^1]:
      let wanted = (2020 - num - num2)
      if wanted.uint16 in s:
        return num * (2020 - num - num2) * num2



when isMainModule:
  echo "Answer1:", answer1(data)
  echo "Answer2:", answer2(data)
