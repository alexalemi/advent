(use ./util)

(def data-string (slurp "../input/02.txt"))
(def test-string `Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green`)

(defn ->data [s]
  (def file-peg
    ~{:id (* "Game " (number :d+)) 
      :color (/ (<- (+ "green" "blue" "red")) ,keyword)
      :element (/ (* (number :d+) " " :color) ,|[$1 $0])
      :obs (/ (group (some (+ ", " :element))) ,from-pairs)
      :game (group (* :id ": " (group (some (+ "; " :obs)))))
      :main (* (some (+ :game "\n")) -1)})

  (from-pairs (peg/match file-peg s)))

(def data (->data data-string))
(def test-data (->data test-string))

(def target {:red 12 :green 13 :blue 14})

(defn part-1 [data]
  (var total 0)
  (loop [[game-id rounds] :pairs data]
    (when (all true? (values (merge-with <= (merge-with max ;rounds) target)))
        (set total (+ total game-id))))
  total)

(assert (= (part-1 test-data) 8))

(def ans1 (part-1 data))

(assert (= ans1 2541))

# Part 2
# Now we want to know the fewest cubes we can use of each color.

(reduce * 1 [1 2 3])

(defn part-2 [data]
  (var total 0)
  (loop [[game-id rounds] :pairs data]
    (set total (+ total (reduce * 1 (values (merge-with max ;rounds))))))
  total)

(assert (= (part-2 test-data) 2286))

(def ans2 (part-2 data))

(assert (= ans2 66016))

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
    


