import numpy as np
import collections

Order = collections.namedtuple('Order', 'pk left top width height')


def get_order(line):
  parts = line.split()
  pk = int(parts[0][1:])
  left, top = map(int, parts[2][:-1].split(','))
  width, height = map(int, parts[3].split('x'))
  return Order(pk, left, top, width, height)


orders = []
with open('../input/03.txt') as f:
  for line in f:
    orders.append(get_order(line))

board = np.zeros((1000, 1000), dtype='int32')


def board_slice(order):
  return np.s_[order.left:order.left + order.width,
               order.top:order.top + order.height]


for order in orders:
  board[board_slice(order)] += 1

print(f'{(board >= 2).sum()} square inches are in two or more.')

for order in orders:
  if np.all(board[board_slice(order)] == 1):
    print(f'{order.pk} is the winning order')
    break
