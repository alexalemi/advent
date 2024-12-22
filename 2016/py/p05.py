from utils import data16

data = data16(5)

tests = []


def answer1(inp):
    return None


tests2 = []


def answer2(inp):
    return None


if __name__ == "__main__":
    for inp, ans in tests:
        myans = answer1(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}"
    print("Answer1:", answer1(data))

    for inp, ans in tests2:
        myans = answer2(inp)
        assert myans == ans, f"Failed on {inp} == {ans}, got {myans}!"

    print("Answer2:", answer2(data))
