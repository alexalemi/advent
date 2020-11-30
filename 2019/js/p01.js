import { getData } from "../../advent.ts";
import * as R from "https://x.nest.land/ramda@0.27.0/source/index.js";

const data = await getData(1, 2019).catch(console.error) || "";

let fuel = R.compose(R.subtract(R.__, 2), Math.floor, R.divide(R.__, 3));
let sol1 = R.map(R.compose(fuel, parseFloat));
let ans1 = R.sum(sol1(data.trim().split("\n")));
console.log("Answer1:", ans1);
