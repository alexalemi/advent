import strUtils, seqUtils, os

const data = staticRead(currentSourcePath.parentDir() / "../input/02.txt")

const testCases = [
  ("1,9,10,3,2,3,11,0,99,30,40,50", 3500),
  ("1,0,0,0,99", 2),
  ("2,3,0,3,99", 2),
  ("2,4,4,5,99,0", 2),
  ("1,1,1,4,99,5,6,0,99", 30)
  ]


func toCodes(s: string): seq[int] =
  return s.strip.split(",").map(parseInt)

func run(inp: string, noun: int = -1, verb: int = -1): int =
  var codes = inp.toCodes
  var loc = 0
  if noun >= 0:
    codes[1] = noun
  if verb >= 0:
    codes[2] = verb
  while true:
    case codes[loc]:
      of 1:
        codes[codes[loc+3]] = codes[codes[loc+1]] + codes[codes[loc+2]]
        loc += 4
      of 2:
        codes[codes[loc+3]] = codes[codes[loc+1]] * codes[codes[loc+2]]
        loc += 4
      of 99:
        return codes[0]
      else:
        raise newException(ValueError, "Didn't recognize op code: " & $codes[loc])


func partTwo(): int =
  for noun in 1..<100:
    for verb in 1..<100:
      if run(data, noun, verb) == 19690720:
        return 100 * noun + verb


when isMainModule:
  #[ for (inp, ans) in testCases:
    assert run(inp) == ans
  ]#

  echo "Answer1: ", run(data, 12, 2)

  let answer2 = partTwo()
  echo "Answer2: ", answer2


