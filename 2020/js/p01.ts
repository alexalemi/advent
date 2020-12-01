import { getData } from "../../advent.ts";
// import * as R from "https://x.nest.land/ramda@0.27.0/source/index.js";

const data = await getData(1, 2020).catch(console.error) || "";
const nums: number[] = data.split("\n").map(parseFloat);

function answer1(nums: number[]): number {
  for (const x of nums) {
    for (const y of nums) {
      if (x + y == 2020) {
        return x * y;
      }
    }
  }
  return 0;
}

const ans1 = answer1(nums);
console.log("Answer1:", ans1);

function answer2(nums: number[]): number {
  for (const x of nums) {
    for (const y of nums) {
      for (const z of nums) {
        if (2020 == x + y + z) {
          return x * y * z;
        }
      }
    }
  }
  return 0;
}

const ans2 = answer2(nums);
console.log("Answer2:", ans2);
