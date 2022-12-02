import { sprintf } from "https://deno.land/std@0.79.0/fmt/printf.ts";
import * as path from "https://deno.land/std@0.79.0/path/mod.ts";

export const ROOT = path.posix.dirname(import.meta.url);
// const token = await Deno.readTextFile(path.join(ROOT, "token.txt"));
const tokenPath = path.join(ROOT, "token.txt");
const token = await Deno.readTextFile(new URL(tokenPath)).catch((err) => {
  console.error("Error loading " + tokenPath + ":" + err.message);
});

function dayfmt(day: number): string {
  return sprintf("%02d", day);
}

function currentYear(): number {
  return (new Date()).getFullYear();
}

function download(day: number, year?: number) {
  year = year || currentYear();
  const url = `https://adventofcode.com/${year}/day/${day}/input`;
  console.log("Fetching " + url);
  const result = fetch(url, { headers: { "Cookie": `session=${token}` } });
  const outPath = path.join(ROOT, `${year}/input/${dayfmt(day)}.txt`);
  return result.then((response) => {
    return response.text();
  }).then((body) => {
    console.log("Writing file to " + outPath);
    return Deno.writeTextFile(new URL(outPath), body).then(() => {
      return body;
    }).catch(console.error);
  }).catch(console.error);
}

/**
 * Returns data for day and year.
 * @param {number} day
 * @param {number=} year - or current
 * @returns {Promise} Promise of data.
 */
export function getData(day: number, year?: number) {
  year = year || currentYear();
  const inPath = path.join(ROOT, `${year}/input/${dayfmt(day)}.txt`);
  console.log("Loading data from " + inPath);
  return Deno.readTextFile(new URL(inPath)).catch((err) => {
    console.log("Data missing! attempting to download...");
    return download(day, year);
  });
}
