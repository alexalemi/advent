(declare-project
  :name "advent2023"
  :description ```Advent of Code 2023 ```
  :version "0.0.0"
  :dependencies ["https://github.com/ianthehenry/jimmy"
                 "https://github.com/andrewchambers/janet-big"
                 "https://github.com/ianthehenry/judge"])

(task "test" [] (shell "jpm_tree/bin/judge"))
