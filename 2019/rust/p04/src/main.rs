const RANGE: std::ops::Range<i32> = 178416..676461;

/// Tests whether two adjacent numbers are the same
fn two_adjacent(x: &i32) -> bool {
    let s = x.to_string();
    s.chars()
        .enumerate()
        .any(|(i, c)| c == s.chars().nth(i + 1).unwrap_or(' '))
}

/// Tests whether digits only increase
fn never_decrease(x: &i32) -> bool {
    let mut prev = 0;
    for c in x.to_string().chars() {
        let now: i32 = c.to_string().parse().unwrap_or(10);
        if now < prev {
            return false;
        }
        prev = now;
    }
    true
}

fn only_two_adjacent(x: &i32) -> bool {
    let s = x.to_string();
    let mut run = 1;
    let mut prev = ' ';
    for c in s.chars() {
        if c == prev {
            run = run + 1;
        } else {
            if run == 2 {
                return true;
            } else {
                run = 1;
            }
        }
        prev = c;
    }
    run == 2
}

fn valid(x: &i32) -> bool {
    never_decrease(x) && two_adjacent(x)
}

fn valid2(x: &i32) -> bool {
    never_decrease(x) && only_two_adjacent(x)
}

fn main() {
    let valid = RANGE
        .filter(valid)
        .fold(0, |acc, _elem| acc + 1);
    println!("Answer 1: {}", valid);

    let valid2 = RANGE
        .filter(valid2)
        .fold(0, |acc, _elem| acc + 1);
    println!("Answer 2: {}", valid2);
}

#[cfg(test)]
mod tests {
    // Note this useful idiom: importing names from outer (for mod tests) scope.
    use super::*;

    #[test]
    fn test1() {
        assert!(valid(&111111))
    }

    #[test]
    fn test2() {
        assert!(!valid(&223450))
    }

    #[test]
    fn test3() {
        assert!(!valid(&123789))
    }

    #[test]
    fn test4() {
        assert!(valid2(&112233))
    }

    #[test]
    fn test5() {
        assert!(!valid2(&123444))
    }

    #[test]
    fn test6() {
        assert!(valid2(&111122))
    }
}
