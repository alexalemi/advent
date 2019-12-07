import strUtils, seqUtils, math, streams, os

type Word = uint64

func parseWord(x: string): Word = parseUInt(x)

func toCodes(s: string): seq[Word] =
  return s.strip.split(",").mapIt(parseWord(strip(it)))

func mode(op, arg: Word): Word =
  return ((op.int div (10 ^ arg.int)) mod 10).Word

proc interpret*(codes: seq[Word], input, output: Stream) =
  var loc = 0
  var codes = codes

  while true:
    let opCode = codes[loc] mod 100
    let params = codes[loc] div 100

    if opCode == 99: # END
      return

    let first = if mode(params, 0) == 0:
      codes[codes[loc + 1]]
      else: codes[loc + 1]

    if opCode == 3: # Input
      codes[codes[loc + 1]] = input.readLine.parseWord
      loc += 2
    elif opCode == 4: # Output
      output.writeLine first
      loc += 2
    else:
      let second = if mode(params, 1) == 0:
        codes[codes[loc + 2]]
        else: codes[loc + 2]

      if opCode == 1: # Add
        codes[codes[loc + 3]] = first + second
        loc += 4
      elif opCode == 2: # Mul
        codes[codes[loc + 3]] = first * second
        loc += 4
      elif opCode == 5: # Jump if true
        loc = if first != 0: second.int else: loc + 3
      elif opCode == 6: # Jump if False
        loc = if first == 0: second.int else: loc + 3
      elif opCode == 7: # Less than
        codes[codes[loc + 3]] = if first < second: 1 else: 0
        loc += 4
      elif opCode == 8: # Equals
        codes[codes[loc + 3]] = if first == second: 1 else: 0
        loc += 4
      else:
        raise newException(ValueError, "Don't understand OpCode")

#[
proc interpret*(codes: seq[Word], input, output: Stream) =
  for elem in interpret(codes, input.readAll.splitLines.map(parseWord)):
    output.writeLine elem
]#

proc interpret*(codes: string, input, output: Stream) = interpret(codes.toCodes, input, output)

proc interpret*(code, input: string): string =
  ## Interprets the brainfuck `code` string, reading from `input` and returning
  ## the result directly.
  var outStream = newStringStream()
  interpret(code, input.newStringStream, outStream)
  result = outStream.data

proc interpret*(code: string) =
  ## Interprets the brainfuck `code` string, reading from stdin and writing to
  ## stdout.
  interpret(code, stdin.newFileStream, stdout.newFileStream)

# macro compileFile*(filename: string) =
#   interpret(staticRead(filename.strVal))

const data = staticRead("fibonacci.intcode")

when isMainModule:
  interpret(data)

#[
  import docopt, tables, strutils

  let doc = """
intcode

Usage:
  intcode interpret [<file.intcode>]
  intcode compile <file.intcode>
  intcode (-h | --help)
  intcode (-v | --version)

Options:
  -h --help     Show this screen.
  -v --version  Show version.
"""

  let args = docopt(doc, version = "intcode 1.0")

  if args["interpret"]:
    let code = if args["<file.intcode>"]:
      readFile($args["<file.intcode>"])
    else: readAll stdin
    interpret(code)
  # if args["compile"]:
  #   compileFile($args["<file.intcode>"])
]#


  # echo run("3,9,8,9,10,9,4,9,99,-1,8".toCodes, @[9])
  # echo run("3,3,1108,-1,8,3,4,3,99".toCodes, @[9])
  # echo run("3,12,6,12,15,1,13,14,13,4,13,99,-1,0,1,9".toCodes, @[1])
  # echo run("3,3,1105,-1,9,1101,0,0,12,4,12,99,1".toCodes, @[1])
  
  # let ans1 = run(data.toCodes, @[1])
  # echo ans1
  # echo "Answer1: ", ans1[ans1.high]

  # let ans2 = run(data.toCodes, @[5])
  # echo ans2
  # echo "Answer2: ", ans2[ans2.high]
  




