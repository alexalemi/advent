import seqUtils, strUtils, math

func fuel(mass: seq[int]): seq[int] = mass.mapIt(max(0, it div 3 - 2))

let mass = toSeq(lines("../input/01.txt")).map(parseInt).fuel

when isMainModule:
  echo "Answer1: ", mass.sum

  var numbers = mass
  var total = numbers.sum

  while numbers.sum > 0:
    numbers = numbers.fuel
    total += numbers.sum

  echo "Answer2: ", total

