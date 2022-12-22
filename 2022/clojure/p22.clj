;; 🎄 Advent of Code 2022 - Day 22 - Monkey Map
;; Looks like today is moving around on a strange map
(ns p22
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.test :as test]))

;; ## Data Processing

(def data-string (slurp "../input/22.txt"))
(def test-string "        ...#
        .#..
        #...
        ....
...#.......#
........#...
..#....#....
..........#.
        ...#....
        .....#..
        .#......
        ......#.

10R5L5R10L4R5L5")

(defn indexed [xs] (map-indexed vector xs))

(defn process [s]
  (let [[board inst] (str/split s #"\n\n")]
    (assoc
     (reduce
      (fn [m [[x y] c]]
        (if (= c \.) (update m :open conj [x y])
            (update m :wall conj [x y])))
      {:open #{} :wall #{}}
      (for [[y line] (indexed (str/split-lines board))
            [x c] (indexed line)
            :when (not= c \ )]
        [[x y] c]))
     :inst
     (drop-last
      (interleave
       (map parse-long (str/split (str/trim inst) #"R|L"))
       (cycle (map keyword (drop 1 (str/split inst #"\d+")))))))))

(def data (process data-string))
(def test-data (process test-string))

;; ## Logic
;; We need a mechanism to wrap around the map, so let's compute the bounds for each row and column

(defn row-bounds [data row]
  (transduce
   (filter (fn [[_ y]] (= y row)))
   (completing (fn [[xlo xhi] [x _]] [(min xlo x) (max xhi x)]))
   [##Inf ##-Inf]
   (set/union (:wall data) (:open data))))

(defn col-bounds [data col]
  (transduce
   (filter (fn [[x _]] (= x col)))
   (completing (fn [[ylo yhi] [_ y]] [(min ylo y) (max yhi y)]))
   [##Inf ##-Inf]
   (set/union (:wall data) (:open data))))

;; Directions

(defn up [[x y]] [x (dec y)])
(defn down [[x y]] [x (inc y)])
(defn left [[x y]] [(dec x) y])
(defn right [[x y]] [(inc x) y])

(def direction-fs
  {:up up
   :down down
   :left left
   :right right})

(def turn-right
  {:up :right
   :right :down
   :down :left
   :left :up})

(def turn-left
  {:up :left
   :left :down
   :down :right
   :right :up})

(defn init [board]
  {:loc [(first (row-bounds board 0)) 0]
   :dir :right})

(defn step
  "Perform a step on the map, wrapping around if we go off the edge."
  [{wall :wall open :open :as data} {loc :loc dir :dir :as state}]
  (let [[x y] loc
        board? (set/union wall open)
        dir-fn (direction-fs dir)
        new (dir-fn loc)]
    (cond
      (board? new) (assoc state :loc new)
      (= dir :right) (assoc state :loc [(first (row-bounds data y)) y])
      (= dir :left) (assoc state :loc [(second (row-bounds data y)) y])
      (= dir :down) (assoc state :loc [x (first (col-bounds data x))])
      (= dir :up) (assoc state :loc [x (second (col-bounds data x))]))))

(def ^:dynamic stepper step)

(defn walk
  "Advance the token the given number of steps forward, stopping if we hit a wall."
  [{wall? :wall :as board} amount state]
  (let [on-board? (set/union wall? (:open board))]
    (loop [state state
           amount amount]
      (let [new (stepper board state)]
        (assert (on-board? (:loc state)) (str "We've walked off the board at: " state))
        (if (or (zero? amount) (wall? (:loc new)))
          state
          (recur new (dec amount)))))))

(defn turn [state which]
  (case which
    :R (update state :dir turn-right)
    :L (update state :dir turn-left)))

(defn advance [board state inst]
  (if (number? inst) (walk board inst state)
      (turn state inst)))

(defn password [{[x y] :loc dir :dir}]
  (+
   (* 1000 (inc y))
   (* 4 (inc x))
   ({:right 0 :down 1 :left 2 :up 3} dir)))

;; ## Part 1

(defn part-1 [data]
  (let [advance (partial advance data)
        state (init data)
        inst (:inst data)]
    (password (reduce advance state inst))))

(test/deftest test-part-1
  (test/is (= 6032 (part-1 test-data))))

(defonce ans1 (part-1 data))

;; ## Part 2
;; Now we have to think about the map as being the surface of a cube, this is going to only really effect my `step` method from above,
;; now I need to have the `step` method take a step on a cube.

(defn linear-map [[x1 y1] [x2 y2]]
  (let [slope (/ (- y2 y1) (- x2 x1))]
    (assert (= 1 (abs slope)) "Not slope = 1!")
    (fn [x] (+ y1 (* slope (- x x1))))))

(defn test-cube-step
  "Perform a step on the cube."
  [{wall :wall open :open :as data} {loc :loc dir :dir :as state}]
  (let [S (first (col-bounds data 0))
        board? (set/union wall open)
        new ((direction-fs dir) loc)
        [x y] new]
    (if (board? new) (assoc state :loc new)
        (let [?S-1 (dec S)
              ?2S (* 2 S)
              ?2S-1 (dec ?2S)
              ?3S (* 3 S)
              ?3S-1 (dec ?3S)
              ?4S (* 4 S)
              ?4S-1 (dec ?4S)
              [loc dir] (cond
                          ;A
                          (= y -1) [[((linear-map [?3S-1 0] [?2S ?S-1]) x)
                                     S]
                                    :down]
                          ;B
                          (= x -1) [[((linear-map [S ?4S-1] [?2S-1 ?3S]) y)
                                     ?3S-1]
                                    :up]

                          ;C
                          (= x ?4S) [[?3S-1
                                      ((linear-map [?2S-1 ?S-1] [?3S-1 0]) y)]
                                     :left]
                          ;D
                          (= y ?2S-1) [[?3S-1
                                        ((linear-map [?3S ?2S-1] [?4S-1 S]) x)]
                                       :left]

                          ;E
                          (and (= y ?S-1)
                               (< x S)) [[((linear-map [0 ?3S-1] [?S-1 ?2S]) x)
                                          0]
                                         :down]
                          ;F
                          (and (= y ?S-1)
                               (>= x S)) [[?2S
                                           ((linear-map [S 0] [?2S-1 ?S-1]) x)]
                                          :right]

                          ;G
                          (and (= y ?2S)
                               (< x S)) [[((linear-map [0 ?3S-1] [?S-1 ?2S]) x)
                                          ?3S-1]
                                         :up]
                          ;H
                          (and (= y ?2S)
                               (>= x S)) [[?2S
                                           ((linear-map [S ?3S-1] [?2S-1 ?2S]) x)]
                                          :right]

                          ;I
                          (and (= x ?3S)
                               (< y S)) [[?4S-1
                                          ((linear-map [0 ?3S-1] [?S-1 ?2S]) y)]
                                         :left]
                          ;J
                          (and (= x ?3S)
                               (>= x S)) [[((linear-map [S ?4S-1] [?2S-1 ?3S]) y)
                                           ?2S]
                                          :down]

                          ;K
                          (and (= y ?3S)
                               (< x ?3S)) [[((linear-map [?2S ?S-1] [?3S-1 0]) x)
                                            ?2S-1]
                                           :up]
                          ;L
                          (and (= y ?3S)
                               (>= x ?3S)) [[0
                                             ((linear-map [?3S ?2S-1] [?4S-1 S]) x)]
                                            :right]

                          ;M
                          (and (= x ?2S-1)
                               (< y S)) [[((linear-map [0 S] [?S-1 ?2S-1]) y)
                                          S]
                                         :down]

                          ;N
                          (and (= x ?2S-1)
                               (>= y ?2S)) [[((linear-map [?2S ?2S-1] [?3S-1 S]) y)
                                             ?2S-1]
                                            :up]

                          :else (throw (AssertionError. (str "Shouldn't get here! with x=" x " y=" y))))]
          (-> state
              (assoc :loc loc)
              (assoc :dir dir))))))

(defn cube-step
  "Perform a step on the cube."
  [{wall :wall open :open :as data} {loc :loc dir :dir :as state}]
  (let [S (first (row-bounds data 0))
        board? (set/union wall open)
        new ((direction-fs dir) loc)
        [x y] new]
    (if (board? new) (assoc state :loc new)
        (let [?S-1 (dec S)
              ?2S (* 2 S)
              ?2S-1 (dec ?2S)
              ?3S (* 3 S)
              ?3S-1 (dec ?3S)
              ?4S (* 4 S)
              ?4S-1 (dec ?4S)
              [loc dir] (cond
                          ;A
                          (and
                           (= y ?2S-1)
                           (= dir :up)) [[S
                                          ((linear-map [0 S] [?S-1 ?2S-1]) x)]
                                         :right]
                          ;B
                          (and
                           (= dir :down)
                           (= y ?3S)) [[?S-1
                                        ((linear-map [S ?3S] [?2S-1 ?4S-1]) x)]
                                       :left]

                          ;C
                          (and (= dir :down)
                               (= y ?4S)) [[((linear-map [0 ?2S] [?S-1 ?3S-1]) x)
                                            0]
                                           :down]
                          ;D
                          (and (= dir :right)
                               (= x S)) [[((linear-map [?3S S] [?4S-1 ?2S-1]) y)
                                          ?3S-1]
                                         :up]

                          ;E
                          (and (= dir :down)
                               (= y S)) [[?2S-1
                                          ((linear-map [?2S S] [?3S-1 ?2S-1]) x)]
                                         :left]
                          ;F
                          (and (= dir :right)
                               (= x ?3S)) [[?2S-1
                                            ((linear-map [0 ?3S-1] [?S-1 ?2S]) y)]
                                           :left]

                          ;G
                          (and
                           (= dir :left)
                           (= x -1)
                           (< y ?3S)) [[S
                                        ((linear-map [?2S ?S-1] [?3S-1 0]) y)]
                                       :right]
                          ;H
                          (and
                           (= dir :left)
                           (= x -1)
                           (>= y ?3S)) [[((linear-map [?3S S] [?4S-1 ?2S-1]) y)
                                         0]
                                        :down]

                          ;I
                          (and
                           (= dir :left)
                           (= x ?S-1)
                           (< y S)) [[0
                                      ((linear-map [0 ?3S-1] [?S-1 ?2S]) y)]
                                     :right]
                          ;J
                          (and
                           (= dir :left)
                           (= x ?S-1)
                           (>= y S)) [[((linear-map [S 0] [?2S-1 ?S-1]) y)
                                       ?2S]
                                      :down]

                          ;K
                          (and
                           (= dir :up)
                           (= y -1)
                           (>= x ?2S)) [[((linear-map [?2S 0] [?3S-1 ?S-1]) x)
                                         ?4S-1]
                                        :up]
                          ;L
                          (and
                           (= dir :up)
                           (= y -1)
                           (< x ?2S)) [[0
                                        ((linear-map [S ?3S] [?2S-1 ?4S-1]) x)]
                                       :right]

                          ;M
                          (and
                           (= dir :right)
                           (= x ?2S)
                           (< y ?2S)) [[((linear-map [S ?2S] [?2S-1 ?3S-1]) y)
                                        ?S-1]
                                       :up]

                          ;N
                          (and
                           (= dir :right)
                           (= x ?2S)
                           (>= y ?2S)) [[?3S-1
                                         ((linear-map [?2S ?S-1] [?3S-1 0]) y)]
                                        :left]

                          :else (throw (AssertionError. (str "Shouldn't get here! with x=" x " y=" y " S=" S))))]
          (-> state
              (assoc :loc loc)
              (assoc :dir dir))))))

(defn test-part-2 [data]
  (binding [stepper test-cube-step]
    (part-1 data)))

(defn part-2 [data]
  (binding [stepper cube-step]
    (part-1 data)))

(test/deftest test-for-part-2
  (test/is (= 5031 (test-part-2 test-data))))

(def ans2 (part-2 data))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p22))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
