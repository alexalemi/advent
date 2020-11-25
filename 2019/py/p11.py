from utils import data19
import intcode

data = data19(11)

tests = []

def answer1(inp, x=None):
    location = 0+0j
    direction = 1j
    colors = {}
    seen = set()
    seen.add(location)

    computer = intcode.Computer(intcode.getcodes(inp))

    while not computer.finished:
        color, turn = computer.run(colors.get(location, 0))
        if color:
            colors[location] = 1
        else:
            colors[location] = 0
        if turn:
            direction = direction * 1j
        else:
            direction = direction * -1j
        location = location + direction
        seen.add(location)

    return len(seen)

def answer2(inp, x=None):
    location = 0+0j
    direction = 1j
    colors = {}
    colors[location] = 1

    computer = intcode.Computer(intcode.getcodes(inp))

    while not computer.finished:
        color, turn = computer.run(colors.get(location, 0))
        if color:
            colors[location] = 1
        else:

            colors[location] = 0
        if turn:
            direction = direction * 1j
        else:
            direction = direction * -1j
        location = location + direction
        seen.add(location)

    return colors


tests2 = []


if __name__ == "__main__":
    ans1 = answer1(data)
    print("Answer1:", ans1) 
   
    colors = answer2(data)
    xmin = int(min([x.real for x in colors.keys()]))
    xmax = int(max([x.real for x in colors.keys()]))
    ymin = int(min([y.imag for y in colors.keys()]))
    ymax = int(max([y.imag for y in colors.keys()]))

    s = ""
    for y in range(ymax, ymin-1, -1):
        s += "".join(["█" if colors.get(x + y*1j, 0) else " " for x in range(xmax, xmin-1, -1)])
        s += "\n"

    print("Answer2:")
    print(s)
