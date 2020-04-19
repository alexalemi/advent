use std::fs::read_to_string;

type Op = usize;
type Program = Vec<Op>;

const EXP_OUTPUT: Op = 19690720;

fn load_program(progstring: &str) -> Program {
    fn parse(line: &str) -> Op {
        line.parse().expect("Line was not an integer")
    }
    progstring.trim().split(',').map(parse).collect()
}

fn run(prog: &mut Program) {
    let mut loc = 0;

    loop {
        match prog[loc] {
            99 => break,
            1 => {
                let (a, b, c) = (prog[loc + 1], prog[loc + 2], prog[loc + 3]);
                prog[c] = prog[a] + prog[b];
                loc += 4;
            }
            2 => {
                let (a, b, c) = (prog[loc + 1], prog[loc + 2], prog[loc + 3]);
                prog[c] = prog[a] * prog[b];
                loc += 4;
            }
            _ => panic!("Hit an opcode we don't understand {}", prog[loc]),
        }
    }
}

fn main() {
    let mut program = load_program(
        &read_to_string("../../input/02.txt").expect("Something went wrong reading the file"),
    );

    let original_program = program.clone();

    program[1] = 12;
    program[2] = 2;
    run(&mut program);
    println!("Answer1: {}", program[0]);

    for i in 0..99 {
        for j in 0..99 {
            program = original_program.clone();
            program[1] = i;
            program[2] = j;
            run(&mut program);
            if program[0] == EXP_OUTPUT {
                println!("Answer2: {}", 100 * i + j);
                return;
            }
        }
    };
}

#[cfg(test)]
mod tests {
    // Note this useful idiom: importing names from outer (for mod tests) scope.
    use super::*;

    #[test]
    fn test_p1() {
        let mut program = load_program("1,9,10,3,2,3,11,0,99,30,40,50");
        run(&mut program);
        assert_eq!(program, [3500, 9, 10, 70, 2, 3, 11, 0, 99, 30, 40, 50]);
    }

    #[test]
    fn test_p2() {
        let mut program = load_program("1,0,0,0,99");
        run(&mut program);
        assert_eq!(program, [2, 0, 0, 0, 99]);
    }

    #[test]
    fn test_p3() {
        let mut program = load_program("2,3,0,3,99");
        run(&mut program);
        assert_eq!(program, [2, 3, 0, 6, 99]);
    }

    #[test]
    fn test_p4() {
        let mut program = load_program("2,4,4,5,99,0");
        run(&mut program);
        assert_eq!(program, [2, 4, 4, 5, 99, 9801]);
    }

    #[test]
    fn test_p5() {
        let mut program = load_program("1,1,1,4,99,5,6,0,99");
        run(&mut program);
        assert_eq!(program, [30, 1, 1, 4, 2, 5, 6, 0, 99]);
    }
}
