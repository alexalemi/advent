# Advent of Code Day 4

import sequtils, strutils, strscans, tables, algorithm, os

let dataPath = currentSourcePath().parentDir.joinPath("../input/04.txt")
let data = toSeq(open(dataPath).lines)

type
  StatusKind = enum
    skStart
    skSleep
    skWake

  GuardID = int

  Datum = object
    year: int
    month: int
    day: int
    hour: int
    minute: int
    status: string
    case kind: StatusKind
    of skStart: guard: GuardID
    of skSleep: discard
    of skWake: discard


#[
# example lines
  [1518-06-05 00:46] falls asleep
  [1518-06-27 00:21] falls asleep
  [1518-11-10 23:52] Guard #881 begins shift
  [1518-03-21 00:34] wakes up
]#
proc readLine(line: string): Datum =
  if not scanf(line, "[$i-$i-$i $i:$i] $s",
    result.year, result.month, result.day,
    result.hour, result.minute):
    raise newException(IOError, "Cannot parse line")
  result.status = line[19..<line.len]
  case result.status[0]:
    of 'G':
      result.kind = skStart
      if not scanf(result.status, "Guard #$i begins shift",
        result.guard):
        raise newException(IOError, "Could not read Gaurd #")
    of 'f':
      result.kind = skSleep
    of 'w':
      result.kind = skWake
    else:
      raise newException(IOError, "Status not understood.")


proc gatherData(data: seq[Datum]): tuple[time: CountTableRef[GuardID], minutes: TableRef[GuardID, array[60, int]]]  =
  var currentGuard: GuardID
  var startTime: int
  var asleepTime = newCountTable[GuardID]()
  var asleepMinutes = newTable[GuardID, array[60, int]]()
  for datum in data:
    case datum.kind:
      of skStart:
        currentGuard = datum.guard
      of skSleep:
        startTime = datum.minute
      of skWake:
        inc(asleepTime, currentGuard, datum.minute - startTime)
        var startingArray = asleepMinutes.getOrDefault(currentGuard)
        for min in startTime..<datum.minute:
          inc startingArray[min]
        asleepMinutes[currentGuard] = startingArray
        startTime = -1
  return (time: asleepTime, minutes: asleepMinutes)


proc mostAsleep(asleepTime: CountTableRef[GuardID], asleepMinutes: TableRef[GuardID, array[60, int]]): tuple[guard: GuardID, minute: int] =
  let (maxGuard, time) = largest(asleepTime)
  var maxMinute = -1
  var maxCount = 0
  for minute in 0..<60:
    if asleepMinutes[maxGuard][minute] > maxCount:
      maxCount = asleepMinutes[maxGuard][minute]
      maxMinute = minute

  return (guard: maxGuard, minute: maxMinute)

proc mostMinute(asleepMinutes: TableRef[GuardID, array[60, int]]): tuple[guard: GuardID, minute: int] =
  var maxGuard: GuardID
  var maxMinute: int
  var maxAmount: int

  for guard, minarray in asleepMinutes:
    for minute, count in minarray:
      if count > maxAmount:
        maxGuard = guard
        maxMinute = minute
        maxAmount = count
  return (guard: maxGuard, minute: maxMinute)

when isMainModule:
  let (asleepTime, asleepMinutes) = gatherData(sorted[string](data, cmp).map(readLine))
  let answer1 = mostAsleep(asleepTime, asleepMinutes)
  echo("intermediate answer: ", answer1)
  echo("Answer1 = ", answer1.guard * answer1.minute)
  let answer2 = mostMinute(asleepMinutes)
  echo("intermediate answer: ", answer2)
  echo("Answer2 = ", answer2.guard * answer2.minute)

