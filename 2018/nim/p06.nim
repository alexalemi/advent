#[
    Advent of Code Day 6
]#

import os, strutils, strformat, sequtils, strscans, math


type Coord = tuple[x, y: int]

proc readLine(line: string): Coord =
  if not scanf(line, "$i, $i",
    result.x, result.y):
    raise newException(IOError, "Cannot parse line")

proc extent(data: seq[Coord]): tuple[startx, endx, starty, endy: int] =
  result.startx = (2 ^ 32-1)
  result.starty = (2 ^ 32-1)
  result.endx = -(2 ^ 32-1)
  result.endy = -(2 ^ 32-1)
  for elem in data:
    if elem.x > result.endx:
      result.endx = elem.x
    if elem.x < result.startx:
      result.startx = elem.x
    if elem.y < result.starty:
      result.starty = elem.y
    if elem.y > result.endy:
      result.endy = elem.y

proc main =
  let dataPath = currentSourcePath().parentDir.joinPath("../input/06.txt")
  let data = toSeq(open(dataPath).lines).map(readLine)

  echo data

  echo data.extent

  # First get the boundaries for the region

when isMainModule:
  main()
