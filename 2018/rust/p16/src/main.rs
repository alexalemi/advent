use std::fs;

fn main() {
    let contents = fs::read_to_string("../../input/16.txt")
        .expect("Something went wrong reading the file.");

    println!("With text:\n{}", contents);
}
