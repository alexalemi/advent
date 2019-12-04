from utils import data19

tests = []

data = data19(4)

if __name__ == "__main__":
    for case, ans in tests:
        assert process(case) == ans

