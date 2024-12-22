beats = {"rock": "scissors", "paper": "rock", "scissors": "paper"}

lookup = {
    "A": "rock",
    "B": "paper",
    "C": "scissors",
    "X": "rock",
    "Y": "paper",
    "Z": "scissors",
}

shape = {"rock": 1, "paper": 2, "scissors": 3}


def score(game):
    opponent, you = game
    shape_score = shape[you]
    if opponent == you:
        # draw
        return shape_score + 3
    elif beats[opponent] == you:
        # you lost
        return shape_score
    else:
        # you won
        return shape_score + 6


data = []
with open("../input/02.txt") as f:
    for line in f:
        data.append(tuple(line.strip().split()))


part1 = sum(score(map(lookup.get, game)) for game in data)
print(f"Answer1: {part1}")

loses_to = {v: k for k, v in beats.items()}


def part2_score(game):
    first, requirement = game
    opponent = lookup[first]
    if requirement == "X":
        # must lose
        you = beats[opponent]
    elif requirement == "Y":
        # must draw
        you = opponent
    else:
        # must win
        you = loses_to[opponent]
    return score((opponent, you))


part2 = sum(part2_score(game) for game in data)
print(f"Answer2: {part2}")
