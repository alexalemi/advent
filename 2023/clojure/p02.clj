;; # 🎄 Advent of Code 2023 - Day 2
(ns p02
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (slurp "../input/02.txt"))

(def test-data-string "Game 1: 3 blue, 4 red; 1 red, 2 green, 6 blue; 2 green
Game 2: 1 blue, 2 green; 3 green, 4 blue, 1 red; 1 green, 1 blue
Game 3: 8 green, 6 blue, 20 red; 5 blue, 4 red, 13 green; 5 green, 1 red
Game 4: 1 green, 3 red, 6 blue; 3 green, 6 red; 3 green, 15 blue, 14 red
Game 5: 6 red, 1 blue, 3 green; 2 blue, 1 red, 2 green")

;; First we need to write some somewhat annoying parsing code
;; to be able to read the input files...

(defn parse-obs [obs]
  (let [[_ n col] (re-matches #"(\d+) (\w+)" obs)]
    {(keyword col) (parse-long n)}))

(defn parse-datum [datum]
  (let [obs (str/split datum #", ")]
    (into {} (map parse-obs obs))))

(defn parse-game-data [game-data]
  (let [data (str/split game-data #"; ")]
    (map parse-datum data)))

(defn parse-game [game]
  (let [[game-id data] (str/split game #": ")
        [_ game-id] (re-matches #"Game (\d+)" game-id)
        game-id (parse-long game-id)]
    {game-id (parse-game-data data)}))

(defn string->data [s]
  (into {} (map parse-game (str/split-lines s))))

;; Now we should be able to generate a data structure
;; This will be a map with the keys being the game-ids
;; and the values being a sequence of games, each stored
;; as a map from the colors to their counts.

(def data (string->data data-string))
(def test-data (string->data test-data-string))

;; We have a target we're interested in and now we need to filter
;; the games to remove those that are impossible given the target.

(def target {:red 12 :green 13 :blue 14})

(defn non-neg? [x]
  (not (neg? x)))

(defn non-neg-vals? [m]
  (every? non-neg? (vals m)))

(defn consistent?
  "Figure out whether a set of observations is consistent with the target"
  [target obs]
  (non-neg-vals? (merge-with - target obs)))

(defn filter-keys [pred? m]
  (reduce-kv (fn [m k v] (if (pred? k) (assoc m k v) m)) {} m))

(defn filter-vals [pred? m]
  (reduce-kv (fn [m k v] (if (pred? v) (assoc m k v) m)) {} m))

(defn all-games-valid? [games]
  (every? (partial consistent? target) games))

(defn part-1 [data]
  (reduce + (keys (filter-vals all-games-valid? data))))

(test/deftest test-part-1
  (test/is (part-1 test-data) 8))

(def ans1 (part-1 data))

;; ## Part 2

(defn power [cubes]
  (reduce * (vals cubes)))

(defn power-for-game [x]
  (power (apply merge-with max x)))

(defn part-2 [data]
  (reduce + (vals (update-vals data power-for-game))))

(test/deftest test-part-2
  (test/is 2286 (part-2 test-data)))

(def ans2 (part-2 data))

(defn -test [_]
  (test/run-tests 'p01))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
