import algorithm
import os
import sets
import strUtils
# import seqUtils
import sugar

const data = staticRead(currentSourcePath.parentDir() / "../input/15.txt")

type Coord = tuple
  x: int
  y: int

iterator neighbors(z: Coord): Coord =
  yield (x: z.x, y: z.y-1)
  yield (x: z.x-1, y: z.y)
  yield (x: z.x+1, y: z.y)
  yield (x: z.x, y: z.y+1)

type uKind = enum Goblin, Elf


const defaultAttack = 3
const defaultHP = 200

type Unit = object
  kind: uKind
  coord: Coord
  attack: int
  hp: int


type State = ref object
  round: int
  turn: int
  units: seq[Unit]
  map: HashSet[Coord]
  done: bool


func process(data: string): State =
  new(result);
  result.round = 0
  result.turn = 0
  result.done = false
  for x, line in pairs(data.splitLines):
    for y, c in pairs(line):
      if c == '.':
        result.map.incl (x: x, y: y)
      elif c == 'E':
        result.units.add Unit(kind: Elf, coord: (x: x, y: y),
            attack: defaultAttack, hp: defaultHP)
        result.map.incl (x: x, y: y)
      elif c == 'G':
        result.units.add Unit(kind: Goblin, coord: (x: x, y: y),
            attack: defaultAttack, hp: defaultHP)
        result.map.incl (x: x, y: y)


proc targets(state: var State, which: int): seq[Unit] =
  result = collect(newSeq):
    for unit in state.units:
      if unit.kind != state.units[which].kind: unit


proc doTurn(state: var State) =
  let targets = state.targets(state.turn)
  # if there aren't any targets left, we're done
  if targets.len == 0:
    state.done = true
    return

  # get the occupied spaces.
  let occupied = initHashSet.collect:
    for unit in state.units: {unit.coord}

  # get the open squares next to targets
  let openSquares = initHashSet.collect:
    for j, unit in pairs(targets):
      for neighbor in unit.coord.neighbors:
        if (neighbor in state.map) and (neighbor notin occupied): {neighbor}

  let currentUnit = state.units[state.turn]
  let adjacent = currentUnit.coord in openSquares
  state.turn += 1
  if (openSquares.len == 0) and not adjacent:
    return
  if not adjacent:
    ## move
    discard

  let neighborCoords = collect(initHashSet):
    for neighbor in currentUnit.coord.neighbors: {neighbor}

  var minUnit: Unit
  var minHP = defaultHP+1
  for target in targets:
    if target.coord in neighborCoords:
      if target.hp < minHP:
        minUnit = target
        minHP = target.hp

  if minHP == defaultHP + 1:
    echo "no targets in range."
  else:
    discard

  echo minUnit



proc cmp(a: Unit, b: Unit): int =
  let y = cmp(a.coord.y, b.coord.y)
  if y == 0: cmp(a.coord.x, b.coord.y) else: y


proc doRound(state: var State) =
  while state.turn < state.units.len:
    state.doTurn
  state.units.sort(cmp)
  state.round += 1


when isMainModule:
  var state = process(data)
  echo state.round
  state.doTurn 
  echo state.turn
  echo "Done."

