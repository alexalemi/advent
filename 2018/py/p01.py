import itertools

diffs = list(map(int, open('../input/01.txt')))

print('Answer1: ', sum(diffs))

state = 0
seen = set()
for diff in itertools.cycle(diffs):
    state += diff
    if state in seen:
        break
    seen.add(state)

print('Size: ', len(seen))

print('Answer2: ', state)

