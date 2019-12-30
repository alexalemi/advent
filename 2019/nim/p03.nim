import os, strUtils, tables, hashes, seqUtils, sets

const data = staticRead(currentSourcePath.parentDir() / "../input/03.txt")

type
  Location = tuple
    x: int
    y: int
  Step = tuple
    direction: Location
    steps: int

# func hash(loc: Location): Hash =
#     hash(loc.x) xor hash(loc.y)

proc `+=`(a: var Location, b: Location) =
  a.x += b.x
  a.y += b.y

func parseStep(s: string): Step =
  case s[0]:
    of 'U':
      result.direction = (x: 0, y: 1)
    of 'D':
      result.direction = (x: 0, y: -1)
    of 'R':
      result.direction = (x: 1, y: 0)
    of 'L':
      result.direction = (x: -1, y: 0)
    else:
      raise newException(ValueError, "Didn't understand " & s[0] & "!")
  result.steps = parseInt(s[1..s.high])

func Locations(steps: seq[Step]): HashSet[Location] =
  result = initHashSet[Location](4096)
  var loc = (x: 0, y: 0)
  for step in steps:
    for i in 0..<step.steps:
      loc += step.direction
      result.incl loc

func Dists(steps: seq[Step]): Table[Location, int] =
  result = initTable[Location, int](4096)
  var loc = (x: 0, y: 0)
  var dist = 0
  for step in steps:
    for i in 0..<step.steps:
      loc += step.direction
      dist += 1
      result[loc] = dist

func manhattan(loc: Location): int = abs(loc.x) + abs(loc.y)

func `<`(a: Location, b: Location): bool =
  a.manhattan < b.manhattan

proc answer1(): int =
  let wires = data.splitLines()
  let locs1 = wires[0].strip().split(",").map(parseStep).Locations
  let locs2 = wires[1].strip().split(",").map(parseStep).Locations
  let both = intersection(locs1, locs2)
  result = min(toSeq(both.items)).manhattan

proc answer2(): int =
  let wires = data.splitLines()
  let dists1 = wires[0].strip().split(",").map(parseStep).Dists
  let dists2 = wires[1].strip().split(",").map(parseStep).Dists
  result = high(int)
  for loc, dist in dists1.pairs:
    if dists2.contains(loc):
      let x = dist + dists2[loc]
      result = min(result, x)

when isMainModule:
  echo "Answer1: ", answer1()
  echo "Answer2: ", answer2()




