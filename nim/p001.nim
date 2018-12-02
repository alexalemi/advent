import strutils

# Add up all the numbers

var
  sum = 0

for line in stdin.lines:
  sum += parseInt(line)


assert sum == 585
echo sum
