import seqUtils, tables

let data = toSeq(open("/home/alexalemi/Projects/advent/input/02.txt").lines)

proc checksum(data: seq[string]): int =
  var twos, threes = 0
  for line in data:
    let countCounts = toSeq(line.toCountTable.values).toCountTable
    if countCounts.getOrDefault(2) > 0:
      twos += 1
    if countCounts.getOrDefault(3) > 0:
      threes += 1
  return twos * threes


const testdata1 = @[
  "abcdef",
  "bababc",
  "abbcde",
  "abcccd",
  "aabcdd",
  "abcdee",
  "ababab"]

assert checksum(testdata1) == 12

echo "Answer1: ", data.checksum

proc oneoff(s1: string, s2: string): bool =
  assert s1.len == s2.len
  result = false
  for i in 0..<s1.len:
    if s1[i] != s2[i]:
      if result:
        return false
      else:
        result = true

proc common(s1: string, s2: string): string =
  assert s1.len == s2.len
  for i in 0..<s1.len:
    if s1[i] == s2[i]:
      result.add(s1[i])


proc part2(data: seq[string]): string =
  for i in 0..<data.len:
    for j in i+1..<data.len:
      if oneoff(data[i], data[j]):
        return common(data[i], data[j])

let testdata = @[
  "abcde",
  "fghij",
  "klmno",
  "pqrst",
  "fguij",
  "axcye",
  "wvxyz"]

assert part2(testdata) == "fgij"

echo "Answer2: ", part2(data)
