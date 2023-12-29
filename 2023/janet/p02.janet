(use ./util)
(use judge)

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
(test test-data
      @{1 @[@{:blue 3 :red 4}
            @{:blue 6 :green 2 :red 1}
            @{:green 2}]
        2 @[@{:blue 1 :green 2}
            @{:blue 4 :green 3 :red 1}
            @{:blue 1 :green 1}]
        3 @[@{:blue 6 :green 8 :red 20}
            @{:blue 5 :green 13 :red 4}
            @{:green 5 :red 1}]
        4 @[@{:blue 6 :green 1 :red 3}
            @{:green 3 :red 6}
            @{:blue 15 :green 3 :red 14}]
        5 @[@{:blue 1 :green 3 :red 6}
            @{:blue 2 :green 2 :red 1}]})

(def target {:red 12 :green 13 :blue 14})

(defn part-1 [data]
  (sum (seq [[game-id rounds] :pairs data
             :when (every? (merge-with <= (merge-with max ;rounds) target))]
         game-id)))

(test (part-1 test-data) 8)
(def ans1 (part-1 data))
(test ans1 2541)

# Part 2
# Now we want to know the fewest cubes we can use of each color.

(defn part-2 [data]
  (var total 0)
  (loop [[game-id rounds] :pairs data]
    (+= total (product (merge-with max ;rounds))))
  total)

(test (part-2 test-data) 2286)
(def ans2 (part-2 data))
(test ans2 66016)

(defn main [&]
  (print "Answer1:" ans1)
  (print "Answer2:" ans2))
