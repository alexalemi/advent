import os
import strUtils
import times

const data = staticRead(currentSourcePath.parentDir() / "../input/23.txt")

type num = uint32

type State = ref object
  children: seq[num]
  current: num

func child(state: State, x: num): num {.inline.} = state.children[x]

func read(state: State, n: int, start: num = 0): seq[num] {.inline.} =
  var current = (if start == 0: state.current else: start)
  result = newSeq[num](n)
  for i in 0..<n:
    current = state.child(current)
    result[i] = current


func process(inp: string, n: int = 0): State =
  let deck = inp.strip
  let size = (if n > 0: n else: deck.len)
  var children = newSeq[num](size+1)
  var x = 0.num
  let first = parseInt($(deck[0])).num
  var prev = first
  for i in 1..<size:
    x = (if (i < deck.len): parseInt($(deck[i])) else: i + 1).num
    (children[prev], prev) = (x, x)
  # set last element
  children[x] = first
  return State(children: children, current: first)

func len(state: State): Natural {.inline.} = state.children.len - 1

func dec(state: State, x: num): num {.inline.} = (if (x - 1 ==
    0): state.len.num else: x - 1)


func move(state: State) {.inline.} =
  # get hand
  let hand = state.read(3)
  # get the insertion point
  var insertAt = state.dec(state.current)
  while insertAt in hand:
    insertAt = state.dec(insertAt)
  # remove and insert hand
  (state.children[state.current], state.children[insertAt], state.children[hand[
      ^1]]) = (state.children[hand[^1]], hand[0], state.children[insertAt])
  state.current = state.children[state.current]

func `$`(state: State): string =
  let start = state.current
  var current = start
  result &= $start
  current = state.child(current)
  while current != start:
    result &= $current
    current = state.child(current)

func finalize(state: State): string =
  var current = state.child(1)
  while current != 1:
    result &= $current
    current = state.child(current)

func finalize2(state: State): int =
  let x = state.child(1)
  let y = state.child(x)
  result = x.int * y.int


proc answer1(inp: string): string =
  var state = process(inp)
  for i in countup(1, 100):
    state.move
  return state.finalize


proc answer2(inp: string): int =
  var state = process(inp, 1_000_000)
  for i in countup(1, 10_000_000):
    state.move
  return state.finalize2

when isMainModule:

  assert answer1("389125467") == "67384529", "First test failed!"
  var time = cpuTime()
  echo "Answer1: ", answer1(data), " in ", cpuTime() - time, " secs"

  assert answer2("389125467") == 149245887792, "Second test failed!"

  echo "running 2..."
  time = cpuTime()
  echo "Answer2: ", answer2(data), " in ", cpuTime() - time, " secs"
