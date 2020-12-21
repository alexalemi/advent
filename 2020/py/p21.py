import time
from collections import Counter
from utils import data20

data = data20(21)

tests = [("""mxmxvkd kfcds sqjhc nhms (contains dairy, fish)
trh fvjkl sbzzf mxmxvkd (contains dairy)
sqjhc fvjkl (contains soy)
sqjhc mxmxvkd sbzzf (contains fish)""", 5)]


def process(inp):
  """Read the input and turn it into a generator of tuples of lists."""
  for line in inp.splitlines():
    front, rest = line.split(' (contains ')
    words = front.strip().split()
    allergens = rest.strip().strip(')').split(', ')
    yield words, allergens


def create_associations(info):
  """Create a mapping from each allergen to a set of words it could be."""
  associations = {}
  for words, allergens in info:
    for allergen in allergens:
      if allergen in associations:
        associations[allergen] = associations[allergen].intersection(set(words))
      else:
        associations[allergen] = set(words)
  associations = {k: v for k, v in associations.items() if v}
  return associations


def answer1(inp):
  info = list(process(inp))
  word_counter = Counter([word for words, _ in info for word in words])
  associations = create_associations(info)

  safe_words = set(word_counter.keys()) - set.union(*associations.values())
  return sum(word_counter[word] for word in safe_words)


tests2 = [(tests[0][0], "mxmxvkd,sqjhc,fvjkl")]


def answer2(inp):
  info = list(process(inp))
  associations = create_associations(info)

  def prune(associations, word):
    """Remove instances of allergen from associations."""
    new = {k: v - set([word]) for k, v in associations.items()}
    new = {k: v for k, v in new.items() if v}
    return new

  assignments = {}
  while associations:
    allergen, words = min(associations.items(), key=lambda x: len(x[1]))
    assert len(words) == 1, "Not unique!"
    word = list(words)[0]
    assignments[allergen] = word
    associations = prune(associations, word)

  return ','.join(x[1] for x in sorted(assignments.items(), key=lambda x: x[0]))


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
