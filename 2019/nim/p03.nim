import os, strUtils, tables, complex, hashes

const data = staticRead(currentSourcePath.parentDir() / "../input/03.txt")


func hash(x: Complex64): Hash =
    hash(x.re) xor hash(x.im)

proc process(path: string) = 
    var counts = initCountTable[Complex64]()
    var loc = complex(0.0, 0.0)
    var dx = complex(0.0, 0.0)
    counts.inc(loc)
    for part in path.strip().split(","):
        case part[0]:
            of 'U':
                dx = complex(0.0, 1.0)
            of 'D':
                dx = complex(0.0, -1.0)
            of 'R':
                dx = complex(1.0, 0.0)
            of 'L':
                dx = complex(-1.0, 0.0)
            else:
                echo "FAILURE"
                # raise newException("Didn't understand case:" & $part[0])
        let rest = part.strip()
        try:
            let num = parseInt(rest[1..<rest.high])
            for i in 0..<num:
                loc += dx
                counts.inc(loc)
        except:
            echo "DIDNT WORK:" & $rest
    # echo counts


when isMainModule:
    data.splitLines()[0].process()