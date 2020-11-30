import { sprintf } from "https://deno.land/std@0.79.0/fmt/printf.ts";
import { format } from "https://deno.land/std@0.79.0/datetime/mod.ts";

export { getData };

const token = await Deno.readTextFile("token.txt");

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
  const path = `${year}/input/${dayfmt(day)}.txt`;
  return result.then((response) => {
    return response.text();
  }).then((body) => {
    console.log("Writing file to " + path);
    return Deno.writeTextFile(path, body).then(() => {return body;}).catch(console.error);
  }).catch(console.error);
}

/**
 * Returns data for day and year.
 * @param {number} day
 * @param {number=} year - or current
 * @returns {Promise} Promise of data.
 */
function getData(day: number, year?: number) {
  year = year || currentYear();
  const path = `${year}/input/${dayfmt(day)}.txt`;
  console.log("Loading data from " + path);
  return Deno.readTextFile(path).catch((err) => {
    console.log("Data missing! attempting to download...");
    return download(day, year);
  });
}

