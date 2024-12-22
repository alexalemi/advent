"""Try to work out the coin puzzle."""

import itertools

coins = {2: "red", 9: "blue", 5: "shiny", 7: "concave", 3: "corroded"}


def formula(args):
    a, b, c, d, e = args
    return a + b * c**2 + d**3 - e == 399


perms = itertools.permutations(coins.keys())


def first_true(iterable, default=False, pred=None):
    """Returns the first true value in the iterable.

    If no true value is found, returns *default*

    If *pred* is not None, returns the first item
    for which pred(item) is true.

    """
    # first_true([a,b,c], x) --> a or b or c or x
    # first_true([a,b], x, f) --> a if f(a) else b if f(b) else x
    return next(filter(pred, iterable), default)


answer = first_true(perms, pred=formula)

print(f"Answer is {answer} or {[coins[n] for n in answer]}")
