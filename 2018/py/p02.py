import numpy as np
# import collections

# two_count = 0
# three_count = 0
#
# with open('../input/002.txt') as f:
#     for line in f:
#         counter = collections.Counter(line)
#         two_count += 2 in counter.values()
#         three_count += 3 in counter.values()
#
# print(two_count * three_count)

# part two

lines = open('../input/02.txt').readlines()
ords = np.array([list(map(ord, line)) for line in lines])
sol, _ = np.where(np.sum(ords[:, None, :] != ords[None, :, :], axis=-1) == 1)

a, b = sol

print(''.join([x for x, y in zip(lines[a], lines[b]) if x == y]))
