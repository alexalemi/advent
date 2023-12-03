import parse
import math
from utils import data23
from collections.abc import Iterator

data = data23(2)

test_data = """Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green"""


def find_color(line: str, color: str) -> Iterator[int]:
    return (match["num"] for match in parse.findall(f"{{num:d}} {color}", line))


Summary = dict[str, int]


def summary(line: str) -> Summary:
    return {color: max(find_color(line, color)) for color in ("red", "green", "blue")}


def game_id(line: str) -> int:
    return parse.search("Game {game_id:d}", line)["game_id"]


def is_valid(total: Summary) -> bool:
    return (total["red"] <= 12) and (total["green"] <= 13) and (total["blue"] <= 14)


def part1(data: str) -> int:
    summaries = {game_id(line): summary(line) for line in data.splitlines()}
    valids = {k: is_valid(v) for k, v in summaries.items()}
    return sum(k for k, v in valids.items() if v)


assert part1(test_data) == 8

ans1 = part1(data)

assert ans1 == 2541


def power(summary: Summary) -> int:
    return math.prod(summary.values())


def part2(data: str) -> int:
    summaries = (summary(line) for line in data.splitlines())
    powers = (power(x) for x in summaries)
    return sum(powers)


assert part2(test_data) == 2286

ans2 = part2(data)

assert ans2 == 66016


if __name__ == "__main__":
    print("Answer1:", ans1)

    print("Answer2:", ans2)
