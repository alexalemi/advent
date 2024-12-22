"""Write a disassembler."""

from arch import read_file

program = read_file("challenge.bin")

HIGH = 32768
insts = {
    0: ("halt", 0),
    1: ("set", 2),
    2: ("push", 1),
    3: ("pop", 1),
    4: ("eq", 3),
    5: ("gt", 3),
    6: ("jmp", 1),
    7: ("jt", 2),
    8: ("jf", 2),
    9: ("add", 3),
    10: ("mult", 3),
    11: ("mod", 3),
    12: ("and", 3),
    13: ("or", 3),
    14: ("not", 2),
    15: ("rmem", 2),
    16: ("wmem", 2),
    17: ("call", 1),
    18: ("ret", 0),
    19: ("out", 1),
    20: ("in", 1),
    21: ("noop", 0),
}


def format_value(val):
    if val < HIGH:
        return f"{val:05d}"
    else:
        return f"reg{val-HIGH}"


with open("challenge.txt", "w") as outfile:
    it = enumerate(program)
    while it:
        try:
            loc, inst = next(it)
            try:
                name, fields = insts[inst]
                if name == "out":
                    args = chr(next(it)[1])
                else:
                    args = (format_value(next(it)[1]) for _ in range(fields))
                outfile.write(f"{loc:05d}: {name} " + " ".join(args) + "\n")
            except KeyError:
                outfile.write(f"{loc:05d}: {inst:05d}!\n")
        except StopIteration:
            break
