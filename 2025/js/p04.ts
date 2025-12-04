import { expect } from "bun:test";

const path = "../input/04.txt";
const file = Bun.file(path);
const dataString = await file.text();

const testString = `..@@.@@@@.
@@@.@.@.@@
@@@@@.@.@@
@.@@@@..@.
@@.@@@@.@@
.@@@@@@@.@
.@.@.@.@@@
@.@@@.@@@@
.@@@@@@@@.
@.@.@@@.@.`;

type Position = [number, number];
type Rolls = Map<string, Position>;

function coord(row: number, col: number): string {
  return `${row},${col}`;
}

function process(inp: string): Rolls {
  const rolls: Rolls = new Map();
  inp.split("\n").forEach((line, row) => {
    [...line].forEach((chr, col) => {
      if (chr === "@") {
        rolls.set(coord(row, col), [row, col]);
      }
    });
  });
  return rolls;
}

function* neighbors([row, col]: Position): Generator<string> {
  yield coord(row + 1, col);
  yield coord(row - 1, col);
  yield coord(row, col + 1);
  yield coord(row, col - 1);
  yield coord(row + 1, col + 1);
  yield coord(row - 1, col + 1);
  yield coord(row + 1, col - 1);
  yield coord(row - 1, col - 1);
}

const data = process(dataString);
const testData = process(testString);

function reachable(data: Rolls): Rolls {
  const canReach: Rolls = new Map();
  for (const [key, pos] of data) {
    let neighborCount = 0;
    for (const nkey of neighbors(pos)) {
      if (data.has(nkey)) neighborCount++;
    }
    if (neighborCount < 4) {
      canReach.set(key, pos);
    }
  }
  return canReach;
}

function part1(data: Rolls): number {
  return reachable(data).size;
}

expect(part1(testData)).toEqual(13);

const ans1 = part1(data);
console.log(`Answer 1: ${ans1}`);

function part2(input: Rolls): number {
  const data: Rolls = new Map(input);
  let canReach;
  let total = 0;
  while ((canReach = reachable(data)).size) {
    total += canReach.size;
    for (const key of canReach.keys()) {
      data.delete(key);
    }
  }
  return total;
}

expect(part2(testData)).toEqual(43);

const ans2 = part2(data);
console.log(`Answer 2: ${ans2}`);
