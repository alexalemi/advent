
text = open('../input/005.txt').read().strip()

print("Initial text length: ", len(text))

def remove(st):
    current = 0
    out = []
    while current < len(st)-1:
        a = st[current]
        b = st[current+1]
        if a.islower() and a.upper() == b:
            current += 2 
        elif a.isupper() and a.lower() == b:
            current += 2
        else:
            out.append(a)
            current += 1
    if current < len(st):
        out.append(st[-1])
    return ''.join(out)

def fixed_point(f, x):
  new = x
  while new != f(new):
    new = f(new)
  return new

assert len(fixed_point(remove, 'aA')) == 0, "Failed test"
assert len(fixed_point(remove, 'abBA')) == 0, "Failed test"
assert len(fixed_point(remove, 'abAB')) == 4, "Failed test"
assert len(fixed_point(remove, 'aabAAB')) == 6, "Failed test"
assert len(fixed_point(remove, 'aabAABc')) == 7, "Failed test"
assert len(fixed_point(remove, 'aabAABcC')) == 6, "Failed test"

simple_test = "dabAcCaCBAcCcaDA"
assert len(fixed_point(remove, simple_test)) == 10, "Failed test"

output = fixed_point(remove, text)
print()
print("Answer: ", len(output))

vals = {}

for i in range(26):
  c = chr(ord('a') + i)
  foo = output.replace(c, '').replace(c.upper(), '')  
  vals[c] = len(fixed_point(remove, foo))
  print(f'{c}: {vals[c]}')

print('Best Answer: ', min(vals.items(), key=lambda x: x[1]))


