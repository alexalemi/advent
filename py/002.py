
with open("../input/002") as f:
    data = f.read()



def getcodes(s):
    return list(map(int, s.split(",")))

def run(codes, noun=12, verb=2):
    done = False
    loc = 0
    if noun:
        codes[1] = noun
    if verb:
        codes[2] = verb
    while not done:
        current_code = codes[loc]
        if current_code == 1:
            from1, from2, to = codes[loc+1:loc+4]
            codes[to] = codes[from1] + codes[from2]
            loc += 4
        elif current_code == 2:
            from1, from2, to = codes[loc+1:loc+4]
            codes[to] = codes[from1] * codes[from2]
            loc += 4
        elif current_code == 99:
            done = True
    return codes[0]
            


tests = (
        ("1,9,10,3,2,3,11,0,99,30,40,50", 3500),
        ("1,0,0,0,99", 2),
        ("2,3,0,3,99", 2),
        ("2,4,4,5,99,0", 2),
        ("1,1,1,4,99,5,6,0,99", 30))

for inp, ans in tests:
    assert run(getcodes(inp), None, None) == ans, "Failed test {}:{}".format(inp, ans)

codes = getcodes(data)
ans = run(codes)
print("Answer1: {}".format(ans))

desired = 19690720

for noun in range(100):
    for verb in range(100):
        if run(codes[:], noun, verb) == desired:
            print("Answer2:", 100*noun + verb)
            break





