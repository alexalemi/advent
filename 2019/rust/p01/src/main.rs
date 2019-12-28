use std::fs::read_to_string;

type Module = i32;

fn parse(line: &str) -> Module {
    line.parse().expect("Line was not an integer")
}

fn mass(module: &Module) -> Module {
    let ans = (module / 3) - 2;
    if ans > 0 { ans } else { 0 }
}

fn allmass(module: &Module) -> Module {
    let mut value:Module = *module;
    let mut total:Module = 0;
    while value >= 0 {
        value = value / 3 - 2;
        total += value;
    }
    total
}

fn day1(data: &str) -> Module {
    let modules: Vec<Module> = data.lines().map(parse).collect();
    modules.iter().map(mass).sum()
}

fn day2(data: &str) -> Module {
    let modules: Vec<Module> = data.lines().map(parse).collect();
    modules.iter().map(allmass).sum()
}


fn main() {
    let data = read_to_string("../../input/01.txt")
        .expect("Something went wrong reading the file");

    println!("Answer1: {}", day1(&data));
    println!("Answer1: {}", day2(&data));
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

    #[test]
    fn test_day2() {
        assert_eq!(allmass(&14), 2);
        assert_eq!(mass(&1969), 966);
        assert_eq!(mass(&100756), 50346);
    }
}
