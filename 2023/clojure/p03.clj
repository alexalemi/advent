;; # 🎄 Advent of Code 2023 - Day 3
(ns p03)

(def data-string (slurp "../input/03.txt"))

(def test-string "467..114..
...*......
..35..633.
......#...
617*......
.....+.58.
..592.....
......755.
...$.*....
.664.598..")

(def digits {\1 1 \2 2 \3 3 \4 4 \5 5 \6 6 \7 7 \8 8 \9 9 \0 0})

(defn ->data
  "Parse the data recursively"
  [data-string]
  (loop [loc [0 0]
         board {:syms {} :nums {}}
         s data-string]
    (let [[row col] loc
          [next & rest] s]
      (cond
        ; done
        (= next nil) board
        ; on newline, increment row
        (= next \newline) (recur [(inc row) 0] board rest)
        ; on . increment col
        (= next \.) (recur [row (inc col)] board rest)
        ; if we hit a digit, consume it and store at its loc
        (digits next) (let [digits (re-find #"\d+" (apply str s))
                            n (count digits)]
                        (recur [row (+ col n)]
                               (assoc-in board [:nums loc] (parse-long digits))
                               (drop n s)))
        ; otherwise we must have a symbol
        :else (recur [row (inc col)] (assoc-in board [:syms loc] next) rest)))))

(def data (->data data-string))
(def test-data (->data test-string))

(defn neighbors
  "Compute all of the squares that neighbor a number"
  [[row col] num]
  (let [n (count (str num))]
    (into #{}
          (concat
           ; the row above
           (for [i (range -1 (inc n))] [(dec row) (+ col i)])
           ; the row below
           (for [i (range -1 (inc n))] [(inc row) (+ col i)])
           ; left and right
           [[row (dec col)] [row (+ col n)]]))))

(defn part-1 [{nums :nums syms :syms}]
  (letfn [(touching-symbol? [[loc num]]
            (some syms (neighbors loc num)))]
    (transduce 
      (comp 
        (filter touching-symbol?) 
        (map second)) 
      + nums)))

(assert (= (part-1 test-data) 4361))

(def ans1 (part-1 data))

(assert ans1 559667)

;; ## Part 2
;;
;; Now we need to identify gears, which are *s that are touching exactly two numbers.

(defn prod [xs] (reduce * xs))

(defn part-2 [data]
  (let [{nums :nums syms :syms} data]
    (letfn [(num-neighbors [star]
              (let [[row col] star
                    ; find the numbers that are within shooting distance of the star
                    possible-nums (select-keys nums (for [c (range -3 2) r (range -1 2)] [(+ row r) (+ col c)]))]
                (into []
                      (comp
                       ; only keep those that actually touch the star
                       (filter (fn [[loc num]] ((neighbors loc num) star)))
                       ; get the number itself
                       (map second))
                      possible-nums)))]
      (transduce
       (comp
        ; find the stars
        (filter (fn [x] (= (second x) \*)))
        ; grab their locs
        (map first)
        ; find the lists of their number neighbors
        (map num-neighbors)
        ; gears need to have two neighbors
        (filter #(= (count %) 2))
        ; compute the gear-ratio
        (map prod))
       + syms))))

(assert (= (part-2 test-data) 467835))

(def ans2 (part-2 data))

(assert (= ans2 86841457))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
