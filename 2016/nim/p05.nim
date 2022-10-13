import md5
import parseUtils
import seqUtils


const testSalt = "abc"
const salt = "uqwqemis"


proc generatePassword(salt: string): string =
  result = ""
  var i = 0
  while len(result) < 8:
    let test = salt & $i
    let hash = getMD5(test)
    if hash[0 .. 4] == "00000":
      result &= hash[5]
      echo i, ": ", hash, " = ", result
    i += 1


let ans1 = generatePassword(salt)
echo "Answer1:", ans1

proc toString(x: openarray[char]): string =
  for ch in x:
    result &= ch


proc generatePassword2(salt: string): string =
  var password: array[8, char]
  for i in 0..7:
    password[i] = '_'
  var seen: array[8, bool]
  var pos: int
  var i = 0
  while not seen.all(proc (x: bool): bool = x):
    let test = salt & $i
    let hash = getMD5(test)
    if hash[0 .. 4] == "00000":
      if hash[5] in {'0'..'7'}:
        discard parseInt($hash[5], pos)
        if seen[pos] == false:
          password[pos] = hash[6]
          seen[pos] = true
          echo i, ": ", hash, " = ", toString(password)
    i += 1
  return toString(password)

echo "PART2"
echo ""

let ans2 = generatePassword2(salt)
echo "Answer2:", ans2
