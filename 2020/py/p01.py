from utils import data20

data = data20(1)


def answer1(inp):
  nums = set(int(x) for x in inp.splitlines())
  for x in nums:
    if (2020 - x) in nums:
      return x * (2020 - x)


def answer2(inp):
  nums = set(int(x) for x in inp.splitlines())
  for x in nums:
    for y in nums:
      if (2020 - x - y) in nums:
        return x * y * (2020 - x - y)


if __name__ == "__main__":
  ans1 = answer1(data)
  print("Answer1:", ans1)

  ans2 = answer2(data)
  print("Answer2:", ans2)
