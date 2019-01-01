import strutils, sequtils
import iterutils

# echo readFile("../input/01.txt").splitLines.map(parseInt)
echo open("../input/01.txt").lines.map(parseInt)

func sum(inp: seq[int]): int =
  for x in inp:
    result += x

for line in open("../input/01.txt").lines:
  echo line, '\t', line.parseInt

echo readFile("../input/01.txt").splitLines

