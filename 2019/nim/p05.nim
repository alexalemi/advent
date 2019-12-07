import strUtils, seqUtils, math

const data = staticRead("../input/05.txt")

func toCodes(s: string): seq[int] =
  return s.strip.split(",").map(parseInt)

func mode(op, arg: int): int =
  return (op div (10 ^ arg)) mod 10

func run(codes: seq[int], inputs: seq[int]): seq[int] =
  var loc = 0
  var codes = codes
  var inputs = inputs

  while true:
    let opCode = codes[loc] mod 100
    let params = codes[loc] div 100

    if opCode == 99: # END
      return

    let first = if mode(params, 0) == 0:
      codes[codes[loc + 1]]
      else: codes[loc + 1]

    if opCode == 3: # Input
      codes[codes[loc + 1]] = inputs.pop
      loc += 2
    elif opCode == 4: # Output
      result.add(first)
      loc += 2
    else:
      let second = if mode(params, 1) == 0:
        codes[codes[loc + 2]]
        else: codes[loc + 2]

      if opCode == 1: # Add
        codes[codes[loc + 3]] = first + second
        loc += 4
      elif opCode == 2: # Mul
        codes[codes[loc + 3]] = first * second
        loc += 4
      elif opCode == 5: # Jump if true
        loc = if first != 0: second else: loc + 3
      elif opCode == 6: # Jump if False
        loc = if first == 0: second else: loc + 3
      elif opCode == 7: # Less than
        codes[codes[loc + 3]] = if first < second: 1 else: 0
        loc += 4
      elif opCode == 8: # Equals
        codes[codes[loc + 3]] = if first == second: 1 else: 0
        loc += 4
      else:
        raise newException(ValueError, "Don't understand OpCode")


when isMainModule:
  # echo run("3,9,8,9,10,9,4,9,99,-1,8".toCodes, @[9])
  # echo run("3,3,1108,-1,8,3,4,3,99".toCodes, @[9])
  # echo run("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9".toCodes, @[1])
  # echo run("3,3,1105,-1,9,1101,0,0,12,4,12,99,1".toCodes, @[1])
  
  # let ans1 = run(data.toCodes, @[1])
  # echo ans1
  # echo "Answer1: ", ans1[ans1.high]

  # let ans2 = run(data.toCodes, @[5])
  # echo ans2
  # echo "Answer2: ", ans2[ans2.high]
  
  import os




