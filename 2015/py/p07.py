from utils import data15
import math
import re
import string

data = data15(7).splitlines()

DIGITS = set(string.digits)

test_data = """123 -> x
456 -> y
x AND y -> d
x OR y -> e
x LSHIFT 2 -> f
y RSHIFT 2 -> g
NOT x -> h
NOT y -> i""".splitlines()

def process_target(target):
    try:
        return int(target)
    except:
        return target

def process_line(line):
    left, right = line.split(" -> ")
    if all(c in DIGITS for c in left):
        op = ("SET", process_target(left))
    elif left[0] == 'N':
        op = ("NOT", process_target(left[4:]))
    elif ' ' not in set(left):
        op = ('LOOKUP', left)
    else:
        a, operand, b = left.split()
        op = (operand, process_target(a), process_target(b))
    return (right, op)


def process(data):
    prog = {}
    for line in data:
        key, val = process_line(line)
        prog[key] = val
    return prog


program = process(data)
test_program = process(test_data)

N = 65536

cache = {}
def lookup(program, register):
    if register in cache:
        return cache[register]
    if isinstance(register, int):
        ans = register
    else:
        op, *args = program[register]
        if op == 'SET': 
            ans = lookup(program, args[0])
        elif op == 'LOOKUP':
            ans = lookup(program, args[0])
        elif op == 'NOT':
            ans = ~lookup(program, args[0])
        elif op == 'AND':
            ans = lookup(program, args[0]) & lookup(program, args[1])
        elif op == 'OR':
            ans = lookup(program, args[0]) | lookup(program, args[1])
        elif op == 'LSHIFT':
            ans = lookup(program, args[0]) << lookup(program, args[1])
        elif op == 'RSHIFT':
            ans = lookup(program, args[0]) >> lookup(program, args[1])
        else:
            raise NotImplemented(f"Don't understand {op} with {args}!")
    cache[register] = ans
    return ans


answer1 = lookup(program, "a")
print(f"The Answer for part1: {answer1}")


cache = {"b": answer1}
answer2 = lookup(program, "a")
print(f"The Answer for part2: {answer2}")
