use std::fs;

fn count(x: &String) -> u32 {
    // Find the sum of all of the digits that match the following digit
    // with periodic boundary conditions.
    let x = x.trim();
    let mut total: u32 = 0;
    for (i, c) in x.char_indices() {
        if c == x.chars().nth((i + 1) % x.len()).expect("Unknown") {
            total += c.to_string().parse::<u32>().expect("Failed to parse!");
        }
    }
    return total;
}

fn count2(x: &String) -> u32 {
    // Find the sum of all of the digits that match the following digit
    // with periodic boundary conditions.
    let x = x.trim();
    let mut total: u32 = 0;
    let length = x.len();
    for (i, c) in x.char_indices() {
        if c == x.chars().nth((i + length / 2) % length).expect("Unknown") {
            total += c.to_string().parse::<u32>().expect("Failed to parse!");
        }
    }
    return total;
}

fn main() {
    let data = fs::read_to_string("../../input/01.txt").expect("Error reading the input.");
    let answer1 = count(&data);
    println!("Part1 = {}", answer1);
    let answer2 = count2(&data);
    println!("Part2 = {}", answer2);
}

#[cfg(test)]
mod tests {
    use super::*;

    #[test]
    fn part1() {
        assert_eq!(count(&"1122".to_string()), 3);
        assert_eq!(count(&"1111".to_string()), 4);
        assert_eq!(count(&"1234".to_string()), 0);
        assert_eq!(count(&"91212129".to_string()), 9);
    }

    #[test]
    fn part2() {
        assert_eq!(count2(&"1212".to_string()), 6);
        assert_eq!(count2(&"1221".to_string()), 0);
        assert_eq!(count2(&"123425".to_string()), 4);
        assert_eq!(count2(&"123123".to_string()), 12);
        assert_eq!(count2(&"12131415".to_string()), 4);
    }
}
