use itertools::Itertools;
use std::fs;

fn max_min(row: String) -> u32 {
    let numbers: Vec<u32> = row
        .split_ascii_whitespace()
        .map(|x| x.parse::<u32>().unwrap())
        .collect();
    let max = numbers.iter().max().unwrap();
    let min = numbers.iter().min().unwrap();
    max - min
}

fn answer1(data: &String) -> u32 {
    data.trim().lines().map(|x| max_min(x.to_string())).sum()
}

fn divisors(row: String) -> u32 {
    let numbers: Vec<u32> = row
        .split_ascii_whitespace()
        .map(|x| x.parse::<u32>().unwrap())
        .collect();
    let pair = numbers
        .iter()
        .combinations(2)
        .filter(|x| (x[1] != x[0]) && (x[1] % x[0] == 0 || x[0] % x[1] == 0))
        .next()
        .expect("didn't work");
    let a = *pair.iter().min().unwrap();
    let b = *pair.iter().max().unwrap();
    b / a
}

fn answer2(data: &String) -> u32 {
    data.trim().lines().map(|x| divisors(x.to_string())).sum()
}

fn main() {
    let data = fs::read_to_string("../../input/02.txt").expect("Error reading the input.");
    let ans1 = answer1(&data);
    println!("Part1 = {}", ans1);
    let ans2 = answer2(&data);
    println!("Part2 = {}", ans2);
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn part1() {
        assert_eq!(
            answer1(
                &"5 9 2 8
        9 4 7 3
        3 8 6 5"
                    .to_string()
            ),
            18
        );
    }

    #[test]
    fn part2() {
        assert_eq!(
            answer2(
                &"
                5 9 2 8
9 4 7 3
3 8 6 5"
                    .to_string()
            ),
            9
        );
    }
}
