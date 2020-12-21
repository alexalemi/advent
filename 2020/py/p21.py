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
  """Create a mapping from each 'word' to an allergen it could be associated with."""
  times = Counter(
      [allergen for (words, allergens) in info for allergen in allergens])
  # create a dictionary, where for each word, it is a dictionary
  # of the number of times each allergen appears associated with that
  # word.
  associations = {}
  for words, allergens in info:
    for word in words:
      for allergen in allergens:
        word_dict = associations.get(word, {})
        word_dict[allergen] = word_dict.get(allergen, 0) + 1
        associations[word] = word_dict

  # if we haven't seen the word occur with the allergen
  # at least as many times as the allergen appeared, then
  # this word can't be that allergen.
  for allergen, seen in times.items():
    for word, word_dict in associations.items():
      if word_dict.get(allergen, 0) < seen:
        if allergen in word_dict:
          del word_dict[allergen]

  # remove emptys
  associations = {k: v for k, v in associations.items() if v}
  return associations


def answer1(inp):
  info = list(process(inp))
  word_counter = Counter([word for words, _ in info for word in words])
  associations = create_associations(info)

  safe_words = set(word_counter.keys()) - {
      words for words, d in associations.items() if len(d) > 0
  }
  return sum(word_counter[word] for word in safe_words)


tests2 = [(tests[0][0], "mxmxvkd,sqjhc,fvjkl")]


def answer2(inp):
  info = list(process(inp))
  associations = create_associations(info)

  def prune(associations, allergen):
    """Remove instances of allergen from associations."""
    new = {
        k: {vk: vv for vk, vv in v.items() if vk != allergen
           } for k, v in associations.items()
    }
    new = {k: v for k, v in new.items() if v}
    return new

  assignments = {}
  while associations:
    word, assoc = min(associations.items(), key=lambda x: len(x[1]))
    assert len(assoc) == 1, "Not unique!"
    allergen = list(assoc.keys())[0]
    assignments[word] = allergen
    associations = prune(associations, allergen)

  return ','.join(x[0] for x in sorted(assignments.items(), key=lambda x: x[1]))


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
