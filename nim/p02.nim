import seqUtils, tables

let data = toSeq(open("../input/02.txt").lines)

func checksum(data: seq[string]): int =
  var twos, threes = 0
  for line in data:
    let countCounts = toSeq(line.toCountTable.values).toCountTable
    if countCounts.getOrDefault(2) > 0:
      twos += 1
    if countCounts.getOrDefault(3) > 0:
      threes += 1
  return twos * threes

echo "Answer1: ", data.checksum
