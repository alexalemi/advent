import { getData } from "../../advent.ts";
import {
  assert,
  assertEquals,
} from "https://deno.land/std@0.79.0/testing/asserts.ts";
import * as R from "https://x.nest.land/ramda@0.27.0/source/index.js";

const data = await getData(2, 2020).catch(console.error) || "";

type Password = { low: number; high: number; letter: string; password: string };

function parse(line: string): Password {
  const [front, password, ...rest1] = line.split(": ");
  const [front2, letter, ...rest2] = front.split(" ");
  const [low, high, ...rest3] = front2.split("-").map(parseFloat);
  return { low, high, letter, password };
}

function isValid(pass: Password): boolean {
  return R.compose(
    R.both(R.lte(pass.low), R.gte(pass.high)),
    R.compose(R.length, R.filter(R.equals(pass.letter)), Array.from),
  )(
    pass.password,
  );
}

const pipeline = (testFunc: ((pass: Password) => boolean)) =>
  R.compose(
    R.length,
    R.filter(R.identity),
    R.map(R.compose(testFunc, parse)),
    R.split("\n"),
    R.trim,
  );
const answer1 = pipeline(isValid);

const ans1 = answer1(data);
console.log("Answer1:", ans1);

function isValid2(pass: Password): boolean {
  return (pass.password[pass.low - 1] === pass.letter) !==
    (pass.password[pass.high - 1] === pass.letter);
}
const answer2 = pipeline(isValid2);

const ans2 = answer2(data);
console.log("Answer2:", ans2);

// Simple name and function, compact form, but not configurable
Deno.test("isValid", () => {
  assert(isValid({ low: 1, high: 3, letter: "a", password: "abcdef" }));
});

Deno.test("!isValid", () => {
  assert(!isValid({ low: 1, high: 3, letter: "b", password: "cdefg" }));
});

const test = `1-3 a: abcde
1-3 b: cdefg
2-9 c: ccccccccc`;
Deno.test("example1", () => {
  assertEquals(answer1(test), 2);
});

Deno.test("example2", () => {
  assertEquals(answer2(test), 1);
});
