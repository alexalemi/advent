use std::fs::read_to_string;

fn parse(line: &str) -> u32 {
    line.parse().expect("Line was not an integer")
}

fn mass(module: &u32) -> u32 {
    (module / 3) - 2
}

fn day1(data: &str) -> u32 {
    let modules: Vec<u32> = data.lines().map(parse).collect();
    modules.iter().map(mass).sum()
}


fn main() {
    let data = read_to_string("../../input/01.txt")
        .expect("Something went wrong reading the file");

    println!("Answer1: {}", day1(&data));
}

#[cfg(test)]
mod tests {
    // Note this useful idiom: importing names from outer (for mod tests) scope.
    use super::*;

    #[test]
    fn test_day1() {
        assert_eq!(mass(&12), 2);
        assert_eq!(mass(&14), 2);
        assert_eq!(mass(&1969), 654);
        assert_eq!(mass(&100756), 33583);

    }
}
