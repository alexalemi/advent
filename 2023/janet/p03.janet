# Advent of Code - Day 3

(use ./util)
(use judge)

(def data-string (slurp "../input/03.txt"))
(def test-string `467..114..
...*......
..35..633.
......#...
617*......
.....+.58.
..592.....
......755.
...$.*....
.664.598..`)

(def board-peg
  ~{:number (group (* (constant :num) (/ (group (* (line) (column))) ,tuple/slice) (number :d+)))
    :blank (+ "." "\n")
    :symbol (group (* (constant :sym) (/ (group (* (line) (column))) ,tuple/slice) (<- :W)))
    :main (some (+ :blank :number :symbol))})

(defn ->data [s]
  (let [x (peg/match board-peg s)]
     {:nums (from-pairs (map rest (filter |(= (first $0) :num) x)))
      :syms (from-pairs (map rest (filter |(= (first $0) :sym) x)))}))

(def data (->data data-string))
(def test-data (->data test-string))
# (test test-data)

(defn neighbors [[x y] l]
  (array/concat
    #(catseq [dx :in [-1 1] dy :range-to [-1 l]] [(+ x dx) (+ y dy)])
    (seq [i :range [-1 (inc l)]] [(dec x) (+ y i)])
    (seq [i :range [-1 (inc l)]] [(inc x) (+ y i)])
    [[x (dec y)] [x (+ y l)]]))

(defn part-1 [data]
  (defn part-number? [loc s]
    (any? (map (data :syms) (neighbors loc (length (string s))))))
  (sum (seq [[loc num] :pairs (data :nums) :when (part-number? loc num)] num)))

(test (part-1 test-data) 4361)
(def ans1 (part-1 data))
(test ans1 559667)


# Part 2
# Now we need to find the stars that are touching exactly two numbers.

(defn contains? [arr x]
  (var result false)
  (each elem arr 
    (when (= x elem)
      (set result true)
      (break)))
  result)


(defn part-2 [data]
  (let [max-num-length (max ;(map |(length (string $0)) (values (data :nums))))
        star-locs (seq [[loc char] :pairs (data :syms) :when (= char "*")] loc)]
     (defn search-points [[x y]]
        (array/concat
          (seq [i :range [(- max-num-length) 2]] [(dec x) (+ y i)])
          (seq [i :range [(- max-num-length) 2]] [(inc x) (+ y i)])
          (seq [i :range [(- max-num-length) 2]] [x (+ y i)])))
     (defn touching-nums [loc]
       (let [cands (filter (data :nums) (search-points loc))]
         (defn touches? [num-loc]
           (contains? (neighbors num-loc (length (string ((data :nums) num-loc)))) loc))
         (filter touches? cands)))
     (def gear-word-locs (filter |(= (length $0) 2) (map touching-nums star-locs)))
     (defn gear-ratio [[loc1 loc2]]
       (* ((data :nums) loc1) ((data :nums) loc2)))
     (sum (map gear-ratio gear-word-locs))))

(test (part-2 test-data) 467835)
(def ans2 (part-2 data))
(test ans2 86841457)


(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
    

