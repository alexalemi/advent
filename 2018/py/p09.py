"""Advent of Code Day 9"""

from collections import deque


def marble_game(players: int, last_marble: int) -> int:
    """Simulate the marble game."""
    marbles = deque([0])
    scores = [0] * players
    next_marble = 1
    for next_marble in range(1, last_marble + 1):
        if next_marble % 23 == 0:
            current_player = next_marble % players
            scores[current_player] += next_marble
            marbles.rotate(7)
            scores[current_player] += marbles.pop()
            marbles.rotate(-1)
        else:
            marbles.rotate(-1)
            marbles.append(next_marble)
    return max(scores)


assert marble_game(9, 25) == 32
assert marble_game(10, 1618) == 8317
assert marble_game(13, 7999) == 146373
assert marble_game(17, 1104) == 2764
assert marble_game(21, 6111) == 54718
assert marble_game(30, 5807) == 37305

# 424 players; last marble is worth 71482 points
print("Answer1:", marble_game(424, 71482))
print("Answer2:", marble_game(424, 100 * 71482))
