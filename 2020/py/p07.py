import time
import library
from utils import data20
import myparser as p

data = data20(7)

tests = [("""light red bags contain 1 bright white bag, 2 muted yellow bags.
dark orange bags contain 3 bright white bags, 4 muted yellow bags.
bright white bags contain 1 shiny gold bag.
muted yellow bags contain 2 shiny gold bags, 9 faded blue bags.
shiny gold bags contain 1 dark olive bag, 2 vibrant plum bags.
dark olive bags contain 3 faded blue bags, 4 dotted black bags.
vibrant plum bags contain 5 faded blue bags, 6 dotted black bags.
faded blue bags contain no other bags.
dotted black bags contain no other bags.""", 4)]


def parse2(txt):
  bagtype = p.map(p.chain(p.word, p.space, p.word), p.join)
  holding = p.map(
      p.chain(
          p.integer, p.ignore(p.space), bagtype,
          p.ignore(
              p.chain(p.space, p.alternatives([p.exact('bags'),
                                               p.exact('bag')]),
                      p.optional(p.exact(", "))))), tuple)
  line = p.chain(
      bagtype, p.ignore(p.exact(" bags contain ")),
      p.alternatives(
          [p.plus(holding),
           p.map(p.exact('no other bags'), lambda x: '')]),
      p.ignore(p.atom('.')))
  return dict(list(map(lambda x: p.parse(line, x)[0], txt.splitlines())))


def parse(txt):
  result = {}
  for line in txt.splitlines():
    front, back = line.split(' bags contain ')
    if back.startswith('no other'):
      result[front.strip()] = None
    else:
      things = []
      for part in back.split(', '):
        num = library.ints(part)[0]
        rest = part[len(str(num)):].split('bag')[0].strip()
        things.append((num, rest))
      result[front.strip()] = things
  return result


def answer1(inp, query='shiny gold'):
  network = parse(inp)

  def is_parent(key):
    if network[key] is None:
      return False
    if query in set(x[1] for x in network[key]):
      return True
    return any(is_parent(x[1]) for x in network[key])

  count = 0
  for key in network:
    if is_parent(key):
      count += 1
  return count


tests2 = [(tests[0][0], 32),
          ("""shiny gold bags contain 2 dark red bags.
dark red bags contain 2 dark orange bags.
dark orange bags contain 2 dark yellow bags.
dark yellow bags contain 2 dark green bags.
dark green bags contain 2 dark blue bags.
dark blue bags contain 2 dark violet bags.
dark violet bags contain no other bags.""", 126)]


def answer2(inp, query='shiny gold'):
  network = parse(inp)

  def contains(key):
    if network[key] is None:
      return 1
    return 1 + sum(n * contains(child) for (n, child) in network[key])

  return contains(query) - 1


if __name__ == "__main__":
  for inp, ans in tests:
    myans = answer1(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
  start = time.time()
  ans1 = answer1(data)
  end = time.time()
  print("Answer1:", ans1, f" in {end - start:0.3e} secs")

  for inp, ans in tests2:
    myans = answer2(inp)
    assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

  start = time.time()
  ans2 = answer2(data)
  end = time.time()
  print("Answer2:", ans2, f" in {end - start:0.3e} secs")
