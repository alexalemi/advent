import { assert } from "./util.ts"

const path = "../input/01.txt";
const file = await Bun.file(path).text();
const data = file.trim();

const testData = `1abc2
pqr3stu8vwx
a1b2c3d4e5f
treb7uchet`

function extractNumber(line: string): int {
	const first_num = line.match(/\d/)
	const last_num = line.split("").reverse().join("").match(/\d/)
	return Number(first_num + last_num)
}

function part1(data: string): int {
	return data.split("\n").map(extractNumber).reduce((acc, val) => acc + val, 0)
}

assert(part1(testData) == 142)

const ans1 = part1(data)
console.log("Answer1:", ans1)

assert(ans1 == 55816)

// Part 2

const testData2 = `two1nine
eightwothree
abcone2threexyz
xtwone3four
4nineeightseven2
zoneight234
7pqrstsixteen`

const lookup = {
	"one": "1",
	"two": "2",
	"three": "3",
	"four": "4",
	"five": "5",
	"six": "6",
	"seven": "7",
	"eight": "8",
	"nine": "9",
};

function extractGeneralNumber(line: string): int {
	const re = /(?=(one|two|three|four|five|six|seven|eight|nine|\d))/g
	let nums = Array.from(line.matchAll(re), x => lookup[x[1]] || x[1])
	return Number(nums[0] + nums[nums.length - 1]);
}

function part2(data: string): int {
	return data.split("\n").map(extractGeneralNumber).reduce((acc, val) => acc + val, 0)
}


assert(part2(testData2) == 281)
const ans2 = part2(data)
console.log("Answer2:", ans2)
assert(ans2 == 54980)

