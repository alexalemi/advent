# The synacor virtual machine.
import os, streams, std/logging

var consoleLog = newConsoleLogger()
addHandler(consoleLog)

const HIGH = 32768
type Word = uint16
const HIGHREGISTER = 32775

type Machine = object
  memory: array[HIGH, Word]
  registers: array[HIGH..HIGHREGISTER, Word]
  stack: seq[Word]
  clock: uint64
  terminated: bool

proc `$`(x: Machine): string =
  "VM(clock=" & $x.clock & ", terminated=" & $x.terminated & ", registers=" & $x.registers & ", stack=" & $x.stack & ")"

proc loadData(machine: var Machine, data: seq[int], start = 0'u16) =
  for i, x in data:
    machine.memory[start + Word(i)] = Word(x)

proc loadFile(machine: var Machine, flname: string, start = 0'u16): int =
  let stream = newFileStream(flname, mode = fmRead)
  defer: stream.close()
  return stream.readData(machine.memory.addr, HIGH)

func value(vm: Machine, loc: Word): Word =
  var value = vm.memory[loc]
  if value >= HIGH:
    vm.registers[value]
  else:
    return value

func read(vm: Machine, loc: Word): Word =
  if loc >= HIGH:
    return vm.registers[loc]
  else:
    return vm.memory[loc]

proc write(vm: var Machine, loc: Word, val: Word) =
  if loc >= HIGH:
    vm.registers[loc] = val
  else:
    vm.memory[loc] = val

proc set(vm: var Machine, register: Word, val: Word) =
  vm.registers[register] = val

proc push(vm: var Machine, val: Word) =
  vm.stack.add val

proc pop(vm: var Machine): Word =
  assert(vm.stack.len > 0, "ERROR! tried to pop from empty stack!")
  result = vm.stack.pop

proc tic(vm: var Machine, address: Word): Word =
  inc vm.clock
  let op = vm.read(address)
  case op:
    of 0: # halt
      vm.terminated = true
      return address
    of 1: # set 1 a b - set register <a> to the value of <b>
      let a = vm.read(address+1)
      let b = vm.value(address+2)
      vm.write(a, b)
      return address + 3
    of 2: # push: 2 a - push <a> onto the stack
      let a = vm.value(address + 1)
      vm.push(a)
      return address + 2
    of 3: # pop: 3 a - remove the top element from the stack and write it into <a>; empty stack = error
      let a = vm.read(address + 1)
      let val = vm.pop
      vm.write(a, val)
      return address + 2
    of 4: # eq: 4 a b c  - set <a> to 1 if <b> is equal to <c>; set it to 0 otherwise
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let c = vm.value(address + 3)
      vm.write(a, if b == c: 1 else: 0)
      return address + 4
    of 5: # gt: 5 a b c - set <a> to 1 if <b> is greater than <c>; set it to 0 otherwise
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let c = vm.value(address + 3)
      vm.write(a, if b > c: 1 else: 0)
      return address + 4
    of 6: # jmp 6 a - jump to <a>
      let a = vm.value(address + 1)
      return a
    of 7: # jt 7 a b - if a is nonzero jump to b
      let a = vm.value(address + 1)
      let b = vm.value(address + 2)
      if a != 0:
        return b
      return address + 3
    of 8: # jf: 8 a b  - if <a> is zero, jump to <b>
      let a = vm.value(address + 1)
      let b = vm.value(address + 2)
      if a == 0:
        return b
      return address + 3
    of 9: # add: 9 a b c - assign into <a> the sum of <b> and <c> (modulo 32768)
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let c = vm.value(address + 3)
      vm.write(a, Word((int(b) + int(c)) mod HIGH))
      return address + 4
    of 10: # mult: 10 a b c - store into <a> the product of <b> and <c> (modulo 32768)
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let c = vm.value(address + 3)
      vm.write(a, Word((int(b) * int(c)) mod HIGH))
      return address + 4 
    of 11: # mod: 11 a b c - store into <a> the remainder of <b> divided by <c>
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let c = vm.value(address + 3)
      vm.write(a, (b mod c))
      return address + 4
    of 12: # and: 12 a b c - stores into <a> the bitwise and of <b> and <c>
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let c = vm.value(address + 3)
      vm.write(a, (b and c))
      return address + 4
    of 13: # or: 13 a b c - stores into <a> the bitwise or of <b> and <c>
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let c = vm.value(address + 3)
      vm.write(a, (b or c))
      return address + 4
    of 14: # not: 14 a b - stores 15-bit bitwise inverse of <b> in <a>
      let a = vm.read(address + 1)
      var b = vm.value(address + 2)
      let c = (not b) and (HIGH - 1)
      vm.write(a, c)
      return address + 3
    of 15: # rmem: 15 a b - read memory at address <b> and write it to <a>
      let a = vm.read(address + 1)
      let b = vm.value(address + 2)
      let val = vm.read(b)
      vm.write(a, val)
      return address + 3
    of 16: # wmem: 16 a b - write the value from <b> into memory at address <a>
      let a = vm.value(address + 1)
      let b = vm.value(address + 2)
      vm.write(a, b)
      return address + 3
    of 17: # call: 17 a - write the address of the next instruction to the stack and jump to <a>
      vm.push((address + 2) mod HIGH)
      let a = vm.value(address + 1)
      return a
    of 18: # ret: 18 - remove the top element from the stack and jump to it; empty stack = halt
      if vm.stack.len == 0:
        vm.terminated = true
        return address
      else:
        let a = vm.pop
        return a
    of 19: # out a - write the ascii character represented by a to stdout
      var code = vm.value(address + 1)
      assert(code < 128, "Invalid ascii code <" & $code &  ">=" & $vm.read(code) & " @" & $vm)
      stdout.write(char(code))
      return address + 2
    of 20: # in: 20 a - read a character from the terminal and write its ascii code to <a>; it can be assumed that once input starts, it will continue until a newline is encountered; this means that you can safely read whole lines from the keyboard and trust that they will be fully read
      let a = vm.read(address + 1)
      let input = readLine(stdin)
      vm.write(a, Word(ord(input[0])))
      return address + 2
    of 21: # noop
      return address + 1
    else:
      error("Don't understand code: " & $op & "!")

proc run(vm: var Machine) =
  var address: Word = 0
  while (not vm.terminated):
    address = vm.tic(address)


when isMainModule:
  info("Booting Synacor VM...")
  var vm: Machine = Machine()
  let readBytes = vm.loadFile(paramStr(1))
  info("Read in " & $readBytes & " bytes")
  info("Running...")
  vm.run
  info("Exiting.")
  info("VM=" & $vm)
