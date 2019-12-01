
def fuel(mass):
    return max(0, mass // 3 - 2)

with open("../input/001", "r") as f:
    modules = [int(x) for x in f]

fuels = [fuel(mass) for mass in modules]


print(f"Answer1: {sum(fuels)}")

total = sum(fuels)

while sum(fuels) > 0:
    fuels = [fuel(mass) for mass in fuels]
    total += sum(fuels)

print(f"Answer2: {total}")

