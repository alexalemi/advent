import { expect } from "bun:test";
import fp from "lodash/fp";

// Load data.
const path = "../input/01.txt";
const file = await Bun.file(path).text();
const data = file.trim();
const testData = `3   4
4   3
2   5
1   3
3   9
3   3`;

// Utilities

type NumberPair = [number, number];
type NumberList = number[];

const parseRow = fp.flow(
  fp.split(/\s+/),
  fp.map(Number),
) as (s: string) => NumberPair;

const process = fp.flow(
  fp.split("\n"),
  fp.map(parseRow),
  fp.unzip,
) as (s: string) => [NumberList, NumberList];

const part1 = fp.flow(
  process,
  fp.map(fp.sortBy(fp.identity)),
  fp.spread(fp.zip),
  fp.map(([a, b]) => Math.abs(a - b)),
  fp.sum,
);

expect(part1(testData)).toBe(11);
expect(part1(data)).toBe(1222801);

const ans1 = part1(data);
console.log("Answer1:", ans1);

// Part 2

const part2 = fp.flow(
  process,
  ([one, two]) => ({
    sorted: fp.sortBy(fp.identity, one),
    counts: fp.countBy(fp.identity, two),
  }),
  ({ sorted, counts }) =>
    fp.reduce(
      (acc: number, val: number) => acc + (val * (counts[val] ?? 0)),
      0,
    )(sorted),
);

expect(part2(testData)).toBe(31);
expect(part2(data)).toBe(22545250);

const ans2 = part2(data);
console.log("Answer2:", ans2);
