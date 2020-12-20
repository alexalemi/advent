from collections import defaultdict, Counter
import time
import math
from utils import data20
import numpy as np

data = data20(20)

tests = [("""Tile 2311:
..##.#..#.
##..#.....
#...##..#.
####.#...#
##.##.###.
##...#.###
.#.#.#..##
..#....#..
###...#.#.
..###..###

Tile 1951:
#.##...##.
#.####...#
.....#..##
#...######
.##.#....#
.###.#####
###.##.##.
.###....#.
..#.#..#.#
#...##.#..

Tile 1171:
####...##.
#..##.#..#
##.#..#.#.
.###.####.
..###.####
.##....##.
.#...####.
#.##.####.
####..#...
.....##...

Tile 1427:
###.##.#..
.#..#.##..
.#.##.#..#
#.#.#.##.#
....#...##
...##..##.
...#.#####
.#.####.#.
..#..###.#
..##.#..#.

Tile 1489:
##.#.#....
..##...#..
.##..##...
..#...#...
#####...#.
#..#.#.#.#
...#.#.#..
##.#...##.
..##.##.##
###.##.#..

Tile 2473:
#....####.
#..#.##...
#.##..#...
######.#.#
.#...#.#.#
.#########
.###.#..#.
########.#
##...##.#.
..###.#.#.

Tile 2971:
..#.#....#
#...###...
#.#.###...
##.##..#..
.#####..##
.#..####.#
#..#.#..#.
..####.###
..#.#.###.
...#.#.#.#

Tile 2729:
...#.#.#.#
####.#....
..#.#.....
....#..#.#
.##..##.#.
.#.####...
####.#.#..
##.####...
##..#.##..
#.##...##.

Tile 3079:
#.#.#####.
.#..######
..#.......
######....
####.#..#.
.#...#.##.
#.#####.##
..#.###...
..#.......
..#.###...
""", 20899048083289)]


def process(inp):
  tiles = {}
  pieces = inp.strip().split('\n\n')
  for piece in pieces:
    top, *rest = piece.splitlines()
    assert top.startswith('Tile '), f"Not the tile header! {top}"
    tileno = int(top[5:].split(':')[0])
    tiles[tileno] = np.array(
        [[1 if c == '#' else 0 for c in line] for line in rest])
  return tiles


flipud = np.flipud
fliplr = np.fliplr
rot90 = np.rot90


def edges(tile):
  """Return the top and sides of a tile."""
  top = tuple(tile[:, 0])
  bottom = tuple(tile[::-1, -1])
  left = tuple(tile[0, ::-1])
  right = tuple(tile[-1, :])
  return top, right, bottom, left


def orientations(tile):
  for i in range(4):
    yield rot90(tile, i)
  tile = fliplr(tile)
  for i in range(4):
    yield rot90(tile, i)


def alledges(tile):
  return edges(tile) + edges(fliplr(tile))


def opposite(edgeno):
  orientation = edgeno // 4
  side = edgeno % 4
  newside = (side + 2) % 4
  return orientation * 4 + newside


def answer1(inp):
  tiles = process(inp)
  seen_edges = defaultdict(set)
  for tileno, tile in tiles.items():
    for (i, edge) in enumerate(alledges(tile)):
      seen_edges[edge].add(tileno)
  unique_edges = {k: v.pop() for k, v in seen_edges.items() if len(v) == 1}
  times_seen = defaultdict(int)
  for k, v in unique_edges.items():
    times_seen[v] += 1
  corners = [x for x, v in times_seen.items() if v == 4]
  assert len(corners) == 4, "Not 4 corners."
  return math.prod(corners)


tests2 = [(tests[0][0], 273)]


def neighbor_locs(position):
  x, y = position
  return [(x - 1, y), (x, y + 1), (x + 1, y), (x, y - 1)]


NEIGHBOR_EDGE = (2, 3, 0, 1)


def assemble_puzzle(inp):
  tiles = process(inp)
  seen_edges = defaultdict(set)
  for tileno, tile in tiles.items():
    for l, edge in enumerate(alledges(tile)):
      seen_edges[edge].add((tileno, l))
  corners = [
      k for k, v in Counter(
          [list(v)[0][0]
           for k, v in seen_edges.items()
           if len(v) == 1]).items() if v == 4
  ]

  next_tileno = corners[0]
  placed_tiles = {next_tileno}
  boundary = []
  position = (0, 0)
  board = {}
  next_tile = tiles[next_tileno]
  board[position] = next_tile

  def populate_boundary(tile, position):
    for edge, position, etype in zip(
        edges(tile), neighbor_locs(position), NEIGHBOR_EDGE):
      if position not in board:
        boundary.append((edge[::-1], position, etype))

  populate_boundary(next_tile, position)
  while boundary:
    next_edge, position, edge_type = boundary.pop()
    neighbor = [x for x in seen_edges[next_edge] if x[0] not in placed_tiles]
    if neighbor:
      # Given the edge, find the connecting tile.
      next_tileno, _ = neighbor.pop()
      # given the tile, test all orientations to find the right one.
      next_tile = next(
          t for t in orientations(tiles[next_tileno])
          if alledges(t)[edge_type] == next_edge)
      assert position not in board, "Position seen before!"
      # Set the board position to the given tile.
      board[position] = next_tile
      placed_tiles.add(next_tileno)
      populate_boundary(next_tile, position)
  return board


def assemble_picture(board):
  minx = min(z[0] for z in board.keys())
  maxx = max(z[0] for z in board.keys())
  miny = min(z[1] for z in board.keys())
  maxy = max(z[1] for z in board.keys())
  width, height = board[(0, 0)].shape
  pic = np.zeros(
      ((width - 2) * (maxy - miny + 1), (height - 2) * (maxx - minx + 1)),
      dtype=bool)
  for y in range(miny, maxy + 1):
    for x in range(minx, maxx + 1):
      pic[(y - miny) * (width - 2):(y - miny + 1) * (width - 2), (x - minx) *
          (height - 2):(x - minx + 1) * (height - 2)] = board[(x, y)][1:-1,
                                                                      1:-1]
  return pic


DRAGON_TEMPLATE = np.array(
    [[0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 0],
     [1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 0, 0, 0, 0, 1, 1, 1],
     [0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 1, 0, 0, 0]])


def count_dragons(pic):
  dragon_pks = np.where(DRAGON_TEMPLATE)
  Y, X = pic.shape
  dY, dX = DRAGON_TEMPLATE.shape
  count = 0
  for y in range(Y - dY):
    for x in range(X - dX):
      if np.all(pic[y:, x:][dragon_pks]):
        count += 1
  return count


def answer2(inp):
  board = assemble_puzzle(inp)
  pic = assemble_picture(board)
  dragons = max(count_dragons(orientation) for orientation in orientations(pic))
  return pic.sum() - dragons * DRAGON_TEMPLATE.sum()


if __name__ == "__main__":

  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on test1 {ans}, got {myans}"
  start = time.time()
  ans1 = answer1(data)
  end = time.time()
  print("Answer1:", ans1, f" in {end - start:0.3e} secs")

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on test2 {ans}, got {myans}!"

  start = time.time()
  ans2 = answer2(data)
  end = time.time()
  print("Answer2:", ans2, f" in {end - start:0.3e} secs")

  # This was for creating printable images.
  # full = np.zeros((12*12, 12*12), dtype=bool)
  # full2 = np.zeros((12*12, 12*12), dtype=bool)
  # for i,(tileno, tile) in enumerate(tiles.items()):
  #   row = i % 12
  #   col = i // 12
  #   full[row*12:row*12+10, col*12:col*12+10] = tile
  #   full2[row*12:row*12+10, col*12:col*12+10] = tile

  # full2 = fliplr(full2)
  # bigger = np.repeat(np.repeat(full, 10, 0), 10, 1)
  # bigger2 = np.repeat(np.repeat(full2, 10, 0), 10, 1)
  #
  # import imageio
  # imageio.imsave('puzzle.png', (~bigger * 255).astype('uint8'))
  # imageio.imsave('puzzle2.png', (~bigger2 * 255).astype('uint8'))
