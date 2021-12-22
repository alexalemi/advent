# The synacor virtual machine.
import os, bitops, streams, std/logging

var consoleLog = newConsoleLogger()
addHandler(consoleLog)

const HIGH = 32768
type Word = uint16
const NUMREGISTERS = 8

type Machine = object
  memory: array[HIGH, Word]
  registers: array[HIGH..(HIGH+NUMREGISTERS-1), Word]
  stack: seq[Word]
  loc: Word
  clock: uint64
  terminated: bool

proc `$`(x: Machine): string =
  "VM(loc=" & $x.loc & ", clock=" & $x.clock & ", terminated=" & $x.terminated & ", registers=" & $x.registers & ", stack=" & $x.stack & ")"

proc loadData(machine: var Machine, data: seq[int], start = 0'u16) =
  for i, x in data:
    machine.memory[start + Word(i)] = Word(x)

proc loadFile(machine: var Machine, flname: string, start = 0'u16): int =
  let stream = newFileStream(flname, mode = fmRead)
  defer: stream.close()
  return stream.readData(machine.memory.addr, HIGH)

func readValAt(vm: Machine, loc: Word): Word =
  var value = vm.memory[loc]
  if value >= HIGH:
    vm.registers[value]
  else:
    return value

func readVal(vm: Machine): Word =
  vm.readValAt(vm.loc)

func raw(vm: Machine, offset = 0'u16): Word =
  vm.memory[vm.loc + offset]

proc read(vm: Machine, loc: Word): Word =
  var l = loc
  if l >= HIGH:
    return vm.memory[vm.registers[l]]
  else:
    return vm.memory[l]

proc write(vm: var Machine, loc: Word, val: Word) =
  var l = loc
  if l >= HIGH:
    vm.registers[l] = val
  else:
    vm.memory[l] = val

proc inc(vm: var Machine) =
  inc vm.loc

proc dec(vm: var Machine) =
  dec vm.loc

proc jmp(vm: var Machine, to: Word) =
  vm.loc = to
  dec vm

proc set(vm: var Machine, register: Word, val: Word) =
  vm.registers[register] = val

proc push(vm: var Machine, val: Word) =
  # debug("push " & $val)
  vm.stack.add val

proc pop(vm: var Machine): Word =
  assert(vm.stack.len > 0, "ERROR! tried to pop from empty stack!")
  result = vm.stack.pop
  # debug("pop " & $result)

proc tic(vm: var Machine) =
  let op = vm.readVal
  case op:
    of 0: # halt
      vm.terminated = true
      dec vm
    of 1: # set 1 a b - set register <a> to the value of <b>
      inc vm
      let r = vm.raw
      inc vm
      let b = vm.readVal
      vm.write(r, b)
    of 2: # push: 2 a - push <a> onto the stack
      inc vm
      let a = vm.readVal
      vm.push(a)
    of 3: # pop: 3 a - remove the top element from the stack and write it into <a>; empty stack = error
      inc vm
      let a = vm.raw
      let val = vm.pop
      vm.write(a, val)
    of 4: # eq: 4 a b c  - set <a> to 1 if <b> is equal to <c>; set it to 0 otherwise
      inc vm
      let a = vm.raw
      inc vm
      let b = vm.readVal
      inc vm
      let c = vm.readVal
      vm.write(a, if b == c: 1 else: 0)
    of 5: # gt: 5 a b c - set <a> to 1 if <b> is greater than <c>; set it to 0 otherwise
      inc vm
      let a = vm.raw
      inc vm
      let b = vm.readVal
      inc vm
      let c = vm.readVal
      vm.write(a, if b > c: 1 else: 0)
    of 6: # jmp 6 a - jump to <a>
      inc vm
      let a = vm.readVal
      vm.jmp(a)
    of 7: # jt 7 a b - if a is nonzero jump to b
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      if a != 0:
        vm.jmp(b)
    of 8: # jf: 8 a b  - if <a> is zero, jump to <b>
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      if a == 0:
        vm.jmp(b)
    of 9: # add: 9 a b c - assign into <a> the sum of <b> and <c> (modulo 32768)
      inc vm
      let r = vm.raw
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      vm.write(r, (a + b) mod HIGH)
    of 10: # mult: 10 a b c - store into <a> the product of <b> and <c> (modulo 32768)
      inc vm
      let r = vm.raw
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      vm.write(r, (a * b) mod HIGH)
    of 11: # mod: 11 a b c - store into <a> the remainder of <b> divided by <c>
      inc vm
      let r = vm.raw
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      vm.write(r, (a mod b) mod HIGH)
    of 12: # and: 12 a b c - stores into <a> the bitwise and of <b> and <c>
      inc vm
      let r = vm.raw
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      vm.write(r, bitand(a, b) mod HIGH)
    of 13: # or: 13 a b c - stores into <a> the bitwise or of <b> and <c>
      inc vm
      let r = vm.raw
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      vm.write(r, bitor(a, b) mod HIGH)
    of 14: # not: 14 a b - stores 15-bit bitwise inverse of <b> in <a>
      inc vm
      let a = vm.raw
      inc vm
      var b = vm.readVal
      b = bitnot(b)
      b.clearBits(15)
      vm.write(a, b)
    of 15: # rmem: 15 a b - read memory at address <b> and write it to <a>
      inc vm
      let a = vm.raw
      inc vm
      let b = vm.raw
      let val = vm.read(b)
      vm.write(a, val)
    of 16: # wmem: 16 a b - write the value from <b> into memory at address <a>
      inc vm
      let a = vm.readVal
      inc vm
      let b = vm.readVal
      vm.write(a, b)
    of 17: # call: 17 a - write the address of the next instruction to the stack and jump to <a>
      vm.push((vm.loc + 2) mod HIGH)
      inc vm
      let a = vm.readVal
      vm.jmp(a)
    of 18: # ret: 18 - remove the top element from the stack and jump to it; empty stack = halt
      if vm.stack.len == 0:
        vm.terminated = true
        dec vm
      else:
        let a = vm.pop
        vm.jmp(a)
    of 19: # out a - write the ascii character represented by a to stdout
      inc vm
      let code = vm.readVal
      assert(code < 128, "Invalid ascii code <" & $code & "> @" & $vm)
      # debug("out a=" & $code & " vm=" & $vm)
      stdout.write(char(code))
    of 20: # in: 20 a - read a character from the terminal and write its ascii code to <a>; it can be assumed that once input starts, it will continue until a newline is encountered; this means that you can safely read whole lines from the keyboard and trust that they will be fully read
      inc vm
      let a = vm.raw
      let input = readLine(stdin)
      vm.write(a, Word(ord(input[0])))
    of 21: # noop
      discard  # do nothing
    else:
      error("Don't understand code: " & $op & "!")
  inc vm.loc
  inc vm.clock

proc run(vm: var Machine) =
  while not vm.terminated:
    vm.tic


when isMainModule:
  info("Booting Synacor VM...")
  var vm: Machine = Machine()
  let readBytes = vm.loadFile(paramStr(1))
  info("Read in " & $readBytes & " bytes")
  info("Running...")
  vm.run
  info("Exiting.")
