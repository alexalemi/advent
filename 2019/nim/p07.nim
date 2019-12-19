import intcode, strUtils

const data = staticRead("../input/07.txt")


proc perm[T](a: openarray[T], n: int, use: var seq[bool]): seq[seq[T]] =
  result = newSeq[seq[T]]()
  if n <= 0: return
  for i in 0 .. a.high:
    if not use[i]:
      if n == 1:
        result.add(@[a[i]])
      else:
        use[i] = true
        for j in perm(a, n - 1, use):
          result.add(a[i] & j)
        use[i] = false

proc permutations[T](a: openarray[T], n: int): seq[seq[T]] =
  var use = newSeq[bool](a.len)
  perm(a, n, use)

proc thruster(prog: string, phase: seq[int]): int =
  var second = 0
  for p in phase:
    let inp = $p & "\n" & $second
    let output = interpret(prog.strip, inp)
    # echo "inp=", inp, " output=", output
    second = output.strip.parseInt
  result = second

proc answer1(): int =
  result = 0
  for c in permutations(@[0,1,2,3,4], 5):
    let o = thruster(data, c)
    if o > result:
      result = o

proc megathruster(prog: string, phase: seq[int]): int =
  var second = 0
  for p in phase:
    let inp = $p & "\n" & $second
    let output = interpret(prog.strip, inp)
    # echo "inp=", inp, " output=", output
    second = output.strip.parseInt
  result = second

proc answer2(): int =
  result = 0
  for c in permutations(@[5,6,7,8,9], 5):
    let o = thruster(data, c)
    if o > result:
      result = o

when isMainModule:

  # assert thruster("3,15,3,16,1002,16,10,16,1,16,15,15,4,15,99,0,0", @[4,3,2,1,0]) == 43210
  # assert thruster("3,23,3,24,1002,24,10,24,1002,23,-1,23,101,5,23,23,1,24,23,23,4,23,99,0,0", @[0,1,2,3,4]) == 54321
  # assert thruster("3,31,3,32,1002,32,10,32,1001,31,-2,31,1007,31,0,33,1002,33,7,33,1,33,31,31,1,32,31,31,4,31,99,0,0,0", @[1,0,4,3,2]) == 65210
  # echo interpret(ex1, "4\n0")
  
  echo "Answer1:", answer1()
