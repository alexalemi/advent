/* Day 18 */
import { getData } from "../../advent.ts";
import { assertEquals } from "https://deno.land/std@0.79.0/testing/asserts.ts";

const data = await getData(14, 2018).catch(console.error) || "";

type State = { recipes: number[]; first: number; second: number };

function newState(): State {
  return { recipes: [3, 7], first: 0, second: 1 };
}

function round(state: State) {
  let current1 = state.recipes[state.first];
  let current2 = state.recipes[state.second];
  let result = current1 + current2;
  let newnums = Array.from(result.toString()).map((x) => parseInt(x));
  state.recipes.push(...newnums);
  state.first = (state.first + current1 + 1) % state.recipes.length;
  state.second = (state.second + current2 + 1) % state.recipes.length;
}

function answer1(n: number): string {
  let state = newState();
  while (state.recipes.length < (n + 10)) {
    round(state);
  }

  return state.recipes.slice(n, n + 10).join("");
}

const n = parseInt(data.trim());
console.log("data = ", n);
console.log(answer1(n));

function arraysEqual(a: number[], b: number[]): boolean {
  if (a === b) return true;
  if (a == null || b == null) return false;
  if (a.length != b.length) return false;

  for (let i = 0; i < a.length; ++i) {
    if (a[i] !== b[i]) return false;
  }
  return true;
}

function answer2(n: number, test: string): number {
  let state = newState();
  let done = false;
  let lower = 0;

  while (!done) {
    for (let i = 0; i < n; ++i) {
      round(state);
    }
    let sol = Array.from(test.trim()).map((x) => parseInt(x));
    while (lower < (state.recipes.length - sol.length)) {
      if (arraysEqual(state.recipes.slice(lower, lower + sol.length), sol)) {
        return lower;
      }
      ++lower;
    }
  }
  return 0;
}

console.log(answer2(1000, data));

Deno.test("part1", () => {
  assertEquals(answer1(9), "5158916779");
  assertEquals(answer1(5), "0124515891");
  assertEquals(answer1(18), "9251071085");
  assertEquals(answer1(2018), "5941429882");
});

Deno.test("part2", () => {
  assertEquals(answer2(20, "51589"), 9);
  assertEquals(answer2(20, "01245"), 5);
  assertEquals(answer2(100, "92510"), 18);
  assertEquals(answer2(10000, "59414"), 2018);
});
