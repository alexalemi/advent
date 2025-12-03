import { expect } from "bun:test";

const path = "../input/03.txt";
const file = Bun.file(path);
const dataString = await file.text();

const testString = `987654321111111
811111111111119
234234234234278
818181911112111`;

function process(inp: string): number[][] {
  return inp.trim().split("\n").map((line) => line.split("").map(Number));
}

const data = process(dataString);
const testData = process(testString);

function maxi(arr: number[]): [number, number] | undefined {
  let loc = 0;
  let val = undefined;
  for (let i = 0; i < arr.length; i++) {
    if ((val === undefined) || (arr[i] > val)) {
      loc = i;
      val = arr[i];
    }
  }
  return [val, loc];
}


function maxNum(arr: number[], n: number = 2): number {
	/* Greedily create the largest n digit number you can from an array, keeping digits in order */
  let ans = 0;
  let head = 0;
  for (let i = 0; i < n; i++) {
    const [x, loc] = maxi(arr.slice(head, arr.length - (n - i - 1)));
    head += loc + 1;
    ans *= 10;
    ans += x;
  }
  return ans;
}

function part1(data: number[][]): number {
  return data.map((x) => maxNum(x, 2)).reduce((acc, val) => acc + val, 0);
}

expect(testData.map((x) => maxNum(x, 2))).toEqual([98, 89, 78, 92]);
expect(part1(testData)).toEqual(98 + 89 + 78 + 92);

const ans1 = part1(data);
console.log(`Answer 1: ${ans1}`);

function part2(data: number[][]): number {
  return data.map((x) => maxNum(x, 12)).reduce((acc, val) => acc + val, 0);
}

expect(testData.map((x) => maxNum(x, 12))).toEqual([
  987654321111,
  811111111119,
  434234234278,
  888911112111,
]);
expect(part2(testData)).toEqual(
  987654321111 + 811111111119 + 434234234278 + 888911112111,
);

const ans2 = part2(data);
console.log(`Answer 2: ${ans2}`);

