import numpy as np


coords = []
with open('../input/006.txt') as f:
    for line in f:
        coords.append(list(map(int, line.strip().split(','))))
coords = np.array(coords)

# coords = np.array([[1, 1],
#         [1, 6],
#         [8, 3],
#         [3, 4],
#         [5, 5],
#         [8, 9],])

N = len(coords)

left = coords[:,0].min() - 1
width = coords[:,0].max() - left + 1
top = coords[:,1].min() - 1 
height = coords[:,1].max() - top + 1

dists = np.abs((np.mgrid[0:width, 0:height] + np.array([left, top])[:,None,None]) - coords[:,:,None,None]).sum(1)

fillboard = dists.argmin(0)

# Fix the equal members
sdists = np.sort(dists, axis=0)
shared = sdists[0] == sdists[1]

fillboard = fillboard * ~shared + shared * N

edgeguys = set(fillboard[:,0]).union(set(fillboard[0,:])).union(set(fillboard[:,-1])).union(set(fillboard[-1,:]))

counts = dict(enumerate(np.bincount(fillboard.ravel())))

answer = max([(v,k) for k,v in counts.items() if k not in edgeguys])

print(f'Answer loc: {answer}')

print(f'Intermediate answer: {answer[0]}')

# PART TWO

good = dists.sum(0) < 10000

print(f'Part Two: {good.sum()}')
