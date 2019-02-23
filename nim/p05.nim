#[
    Advent of Code Day 5, we need to try do do some reductions
]#

import os, strutils, strformat, sequtils

let dataPath = currentSourcePath().parentDir.joinPath("../input/05.txt")
let data = open(dataPath).readAll()

type
    Entry = ref object
        next, prev: Entry
        value: char
    Deque = tuple[head: Entry, tail: Entry]

proc newEntry(): Entry =
    result = Entry(next: nil, prev: nil, value: '\0')

proc toDeque(data: string): Deque =
    ## Read in the string as a Deque
    result.head = newEntry()
    result.tail = newEntry()
    var prev = result.head
    for letter in data:
        let entry = Entry(prev: prev, next: nil, value: letter)
        prev.next = entry
        prev = entry
    prev.next = result.tail

proc len(x: Deque): int =
    result = -1
    var node = x.head
    while node.next != x.tail:
        inc result
        node = node.next

iterator items(x: Deque): Entry =
    ## Iterate over the Entries in a Deque
    var node = x.head.next
    while node != x.tail:
        yield node
        node = node.next

proc `$`(x: Deque): string =
    for entry in x:
        result &= entry.value

proc copy(x: Deque): Deque =
    result.head = newEntry()
    result.tail = newEntry()
    var prev = result.head
    for entry in x:
        let newEntry = Entry(next: nil, prev: prev, value: entry.value)
        prev.next = newEntry
        prev = newEntry
    prev.next = result.tail

proc `~=`(x, y: char): bool =
    if x.isLowerAscii and y.isUpperAscii:
        return x == y.toLowerAscii
    elif x.isUpperAscii and y.isLowerAscii:
        return x.toLowerAscii == y
    else:
        return false

assert(not ('a' ~= 'b'))
assert(not ('a' ~= 'B'))
assert(not ('a' ~= 'a'))
assert(not ('B' ~= 'a'))
assert(not ('\0' ~= 'a'))
assert ('a' ~= 'A')
assert ('A' ~= 'a')

proc `~=`(x, y: Entry): bool =
    return x.value ~= y.value

proc consume(x: Deque) =
    ## Eat up all of the matching characters
    var current = x.head
    while current.next != x.tail:
        if current ~= current.next:
            # delete both current and the next one
            current.prev.next = current.next.next
            current.next.next.prev = current.prev
            current = current.prev
        else:
            current = current.next

proc simplify(x: Deque): Deque =
    result = copy(x)
    consume(result)

assert($"aA".toDeque.simplify == "")
assert($"abBA".toDeque.simplify == "")
assert($"abAB".toDeque.simplify == "abAB")
assert($"aabAAB".toDeque.simplify == "aabAAB")
assert($"dabAcCaCBAcCcaDA".toDeque.simplify == "dabCBAcaDA")

proc removeInstances(x: Deque, y: char) =
    ## Remove all of the instances of y in place.
    var current = x.head
    while current.next != x.tail:
        if current.value.toLowerAscii == y.toLowerAscii:
            # remove this node
            current.prev.next = current.next
            current.next.prev = current.prev
        current = current.next

proc clean(x: Deque, y: char): Deque =
    ## Return a new Deque with the instances removed
    result = copy(x)
    result.removeInstances(y)

assert($"aaabbb".toDeque.clean('a') == "bbb")
assert($"aaAAabbb".toDeque.clean('a') == "bbb")
assert($"abcdefabcdef".toDeque.clean('a') == "bcdefbcdef")

proc remover(x: Deque): auto =
    return proc (y: char): Deque =
        return x.clean(y)

proc main = 
    let deque = toDeque(data)

    let simplified = deque.simplify
    let partOne = simplified.len
    echo &"partOne = {partOne}"

    let partTwo = toSeq(items("abcdefghijklmnopqrstuvwxyz")).mapIt(simplified.clean(it).simplify.len).min
    echo &"partTwo = {partTwo}"

when isMainModule:
    main()