"""Implementing the Synacor VM in python."""

import logging
import sys
from struct import unpack
from dataclasses import dataclass
from typing import List

HIGH = 32768

Word = int

def read_file(flname: str) -> List[Word]:
    file_array = []
    with open(flname, 'rb') as f:
        while True:
            try:
                file_array.append(unpack("<H", f.read(2))[0])
            except:
                break
        return file_array

@dataclass
class VM:
    memory = [0 for _ in range(HIGH)]
    registers = { i:0 for i in range(HIGH, HIGH+8) }
    stack = []
    address = 0
    terminated = False


    def load_file(self, flname: str):
        for index, command in enumerate(read_file(flname)):
            self.memory[index] = command

    def value(self, addr: Word) -> Word:
        "Read a value from a location."
        val = self.memory[addr]
        if val < HIGH:
            return val
        else:
            return self.registers[val]

    def read(self, addr: Word) -> Word:
        if addr < HIGH:
            return self.memory[addr]
        else:
            return self.registers[addr]

    def write(self, addr: Word, val: Word):
        if addr < HIGH:
            self.memory[addr] = val
        else:
            self.registers[addr] = val

    def run(self, address: Word=0):
        while not self.terminated:
            address = self.tic(address)

    def tic(self, address):
        inst = self.memory[address]
        # logging.debug(f"@{address} {inst}")
        if inst == 0: # halt
            self.terminated = True
            return address 
        elif inst == 1: # set a b - set register a to the value of b
            a = self.read(address + 1)
            b = self.value(address + 2)
            self.write(a, b)
            return address + 3
        elif inst == 2: # push a - push <a> onto the stack
            a = self.value(address + 1)
            self.stack.append(a)
            return address + 2
        elif inst == 3: # pop a - remove the top element from the stack and write it to <a>; empty stack = error
            if self.stack:
                a = self.read(address + 1)
                val = self.stack.pop()
                self.write(a, val)
            else:
                raise ValueError("Cannot pop an empty stack!")
            return address + 2
        elif inst == 4: # eq a b c - set <a> to 1 if <b> is equal to <c>; set to 0 otherwise
            a = self.read(address + 1)
            b = self.value(address + 2)
            c = self.value(address + 3)
            self.write(a, 1 if b == c else 0)
            return address + 4
        elif inst == 5: # gt a b c - set <a> to 1 if <b> is greater than <c>; set to 0 otherwise
            a = self.read(address + 1)
            b = self.value(address + 2)
            c = self.value(address + 3)
            self.write(a, 1 if b > c else 0)
            return address + 4
        elif inst == 6: # jmp a - jump to <a>
            a = self.value(address + 1)
            return a
        elif inst == 7: # jt a b - if <a> is nonzero, jump to <b>
            a = self.value(address + 1)
            b = self.value(address + 2)
            if a != 0:
                return b
            else:
                return address + 3
        elif inst == 8: # jf a b - if <a> is zero, jump to <b>
            a = self.value(address + 1)
            b = self.value(address + 2)
            if a == 0:
                return b
            else:
                return address + 3
        elif inst == 9: # add a b c - assign into <a> the sum of <b> and <c> (modulo 32768)
            a = self.read(address + 1)
            b = self.value(address + 2)
            c = self.value(address + 3)
            self.write(a, (b + c) % HIGH)
            return address + 4
        elif inst == 10: # mult a b c 
            a = self.read(address + 1)
            b = self.value(address + 2)
            c = self.value(address + 3)
            self.write(a, (b * c) % HIGH)
            return address + 4
        elif inst == 11: # mod a b c 
            a = self.read(address + 1)
            b = self.value(address + 2)
            c = self.value(address + 3)
            self.write(a, (b % c) % HIGH)
            return address + 4
        elif inst == 12: # and a b c
            a = self.read(address + 1)
            b = self.value(address + 2)
            c = self.value(address + 3)
            self.write(a, (b & c) % HIGH)
            return address + 4
        elif inst == 13: # or a b c
            a = self.read(address + 1)
            b = self.value(address + 2)
            c = self.value(address + 3)
            self.write(a, (b | c) % HIGH)
            return address + 4
        elif inst == 14: # not a b
            a = self.read(address + 1)
            b = self.value(address + 2)
            self.write(a, (~b) & (HIGH - 1))
            return address + 3
        elif inst == 15: # rmem a b - read memory at address <b> and write it to <a>
            a = self.read(address + 1)
            b = self.value(address + 2)
            val = self.read(b)
            self.write(a, val)
            return address + 3
        elif inst == 16: # wmem a b - write the value from <b> into memory at address <a>
            a = self.value(address + 1)
            b = self.value(address + 2)
            self.write(a, b)
            return address + 3
        elif inst == 17: # call a - write the address of the next instruction to the stack and jump to <a>
            a = self.value(address + 1)
            self.stack.append(address + 2)
            return a
        elif inst == 18: # ret - remove the top element from the stack and jump to it; empty stack = halt
            if self.stack:
                return self.stack.pop()
            else:
                self.terminated = True
        elif inst == 19: # out a - write the character represented by the ascii code <a> to the terminal.
            a = self.value(address + 1)
            # logging.debug(f"out {a}")
            print(chr(a), end="", flush=True)
            return address + 2
        elif inst == 20: # in a - read a character from the terminal and write its ascii code to <a>;
            # it can be assumed that once input starts, it will continue until a newline is encountered;
            # this means that you can safely read whole lines from the keyboard and trust that they will
            # be fully read
            a = self.read(address + 1)
            inp = sys.stdin.read( 1 )
            num = ord(inp)
            self.write(a, num)
            return address + 2
        elif inst == 21: # noop
            return address + 1
        else:
            raise ValueError(f"UNKNOWN inst: {inst}")


if __name__ == "__main__":
    logging.basicConfig(level=logging.DEBUG)
    logging.info("Starting the Synacor VM...")
    vm = VM()
    logging.info("Loading the challenge image...")
    vm.load_file("challenge.bin")
    logging.info("Running...")
    vm.run()
    logging.info("Finished.")



