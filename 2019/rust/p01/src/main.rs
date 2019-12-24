use std::fs::read_to_string;

fn parse(line: &str) -> u32 {
    line.parse().expect("Line was not an integer")
}

fn mass(module: &u32) -> u32 {
    (module / 3) - 2
}

fn main() {
    let data = read_to_string("../../input/01.txt")
        .expect("Something went wrong reading the file");
    let modules: Vec<u32> = data.lines().map(parse).collect();
    let total:u32 = modules.iter().map(mass).sum();

    println!("Answer1: {}", total);
}
