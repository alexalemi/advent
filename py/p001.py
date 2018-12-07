import itertools

diffs = map(int, open('001.txt'))

state = 0
seen = set()
for diff in itertools.cycle(diffs):
    state += diff
    if state in seen:
        break
    seen.add(state)

print(f'Finished: {state}')

