import numpy as np
import collections

import datetime
import re

lines = open('../input/04.txt').readlines()

info = sorted(lines)
# dates = [datetime.datetime.strptime(line[1:17], '%Y-%m-%d %H:%M') for line in lines]

current_gaurd = None
current_asleep = None
asleep_times = {}
asleep_minutes = {}


def get_match(line):
  return re.findall('(?:#(\d+).*(begins|ends))|(wakes|asleep)', line)[0]


def get_datetime(line):
  return datetime.datetime.strptime(line[1:17], '%Y-%m-%d %H:%M')


for line in info:
  match = get_match(line)
  if match[0] and match[1] == 'begins':
    current_gaurd = match[0]
  elif match[2] == 'asleep':
    current_asleep = get_datetime(line).minute
  elif match[2] == 'wakes':
    end = get_datetime(line).minute
    asleep_times[current_gaurd] = asleep_times.get(current_gaurd,
                                                   0) + end - current_asleep
    asleep_minutes.setdefault(current_gaurd,
                              np.zeros(60,
                                       dtype='int32'))[current_asleep:end] += 1
    current_asleep = None

in_order = sorted(asleep_times.items(), key=lambda k: k[1], reverse=True)
gaurd = in_order[0][0]
max_time = asleep_minutes[gaurd].argmax()
print(f'ANSWER: {max_time * int(gaurd)}')

outcome = max([(v.max(), v.argmax(), g) for g, v in asleep_minutes.items()])

print(f'ANSWER2: {outcome[1] * int(outcome[2])}')
