
import collections

two_count = 0
three_count = 0

with open('../input/002.txt') as f:
    for line in f:
        counter = collections.Counter(line)
        two_count += 2 in counter.values()
        three_count += 3 in counter.values()

print(two_count * three_count)

# part two


all_lines = []
with open('../input/002.txt') as f:
    for line in f:
        all_lines.append(line.strip())





