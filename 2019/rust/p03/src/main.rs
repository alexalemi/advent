use std::collections::HashMap;
use std::fs::read_to_string;

#[derive(Debug)]
enum Direction {
    Right,
    Left,
    Up,
    Down,
}

#[derive(Debug)]
struct Command {
    direction: Direction,
    steps: i32,
}

impl Command {
    fn parse(s: &str) -> Command {
        let steps: i32 = s[1..].parse().expect("failed to find integer.");
        match s.chars().nth(0).expect("First character doesn't match.") {
            'R' => Command {
                direction: Direction::Right,
                steps,
            },
            'U' => Command {
                direction: Direction::Up,
                steps,
            },
            'L' => Command {
                direction: Direction::Left,
                steps,
            },
            'D' => Command {
                direction: Direction::Down,
                steps,
            },
            _ => panic!("First character didn't match expectations."),
        }
    }
}

fn process_wire(data: &Vec<Command>) -> HashMap<(i32, i32), i32> {
    let mut wire: HashMap<(i32, i32), i32> = HashMap::new();
    let mut pos = (0, 0, 0);
    for elem in data.iter() {
        match elem {
            Command {
                direction: Direction::Down,
                steps,
            } => {
                for _i in 0..*steps {
                    pos = (pos.0, pos.1 - 1, pos.2 + 1);
                    wire.insert((pos.0, pos.1), pos.2);
                }
            }
            Command {
                direction: Direction::Up,
                steps,
            } => {
                for _i in 0..*steps {
                    pos = (pos.0, pos.1 + 1, pos.2 + 1);
                    wire.insert((pos.0, pos.1), pos.2);
                }
            }
            Command {
                direction: Direction::Left,
                steps,
            } => {
                for _i in 0..*steps {
                    pos = (pos.0 - 1, pos.1, pos.2 + 1);
                    wire.insert((pos.0, pos.1), pos.2);
                }
            }
            Command {
                direction: Direction::Right,
                steps,
            } => {
                for _i in 0..*steps {
                    pos = (pos.0 + 1, pos.1, pos.2 + 1);
                    wire.insert((pos.0, pos.1), pos.2);
                }
            }
        }
    }
    wire
}

fn manhattan(x: &(i32, i32)) -> i32 {
    x.0.abs() + x.1.abs()
}

fn main() {
    let data = read_to_string("../../input/03.txt").expect("Could not read the file.");
    let data: Vec<Vec<Command>> = data
        .trim()
        .lines()
        .map(|l| l.split(',').map(|s| Command::parse(s)).collect())
        .collect();

    let wire1 = process_wire(&data[0]);
    let wire2 = process_wire(&data[1]);

    let ans1 = wire1
        .keys()
        .filter(|s| wire2.contains_key(s))
        .map(manhattan)
        .min()
        .expect("No minimum found");
    println!("Answer 1: {}", ans1);

    let ans2 = wire1
        .keys()
        .filter(|s| wire2.contains_key(s))
        .map(|s| wire1.get(s).expect("Missing") + wire2.get(s).expect("Missing"))
        .min()
        .expect("No minimum found");
    println!("Answer 2: {}", ans2);
}
