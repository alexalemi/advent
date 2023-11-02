# 🎄 Advent of Code: 2022 Day 1

# We have a bunch of calories eaten by each elf.
(def test-string `1000
2000
3000

4000

5000
6000

7000
8000
9000

10000`)
(def data-string (slurp "../input/01.txt"))


## We want to process the data so that we have the sums of the numbers)
## for each of the elves.
(def data (->> data-string
              (string/trim)
              (string/split "\n\n")
              (map (fn [x] (reduce + 0 (map scan-number (string/split "\n" x)))))))

## For part1 we just need the max.
(def ans1 (reduce max 0 data))

(comment
  (sorted data >))
  
  

# For part 2 we need the top 3.)
(def ans2 (reduce + 0 (take 3 (sorted data >))))

# You can run from main
(defn main [& args]
 (print "Answer1:" ans1)
 (print "Answer2:" ans2))

