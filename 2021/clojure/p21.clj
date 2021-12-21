(ns advent21
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/21.txt"))
(def test-string "Player 1 starting position: 4\nPlayer 2 starting position: 8\n")

(defn process-line [line]
  (let [[_ pos] (re-matches #"Player \d starting position: (\d+)" line)]
    (read-string pos)))

(defn process [s]
  (let [[a b] (map process-line (str/split-lines s))]
    {:positions [a b]
     :rolls 0
     :scores [0 0]}))

(def data (process data-string))
(def test-data (process test-string))

(def DIE-SIDES 100)
(defn roll-die [s]
  (if (= s DIE-SIDES) 1 (inc s)))

(def STATES 10)
(defn project [n] (inc (mod (dec n) STATES)))

(def MAX-SCORE 1000)
(defn step [state roll]
  (let [{:keys [positions scores]} state
        [a b] positions
        [sa sb] scores
        loc (project (+ a roll))
        score (+ sa loc)
        new-state (-> state
                      (update :rolls + 3)
                      (assoc :positions [b loc])
                      (assoc :scores [sb score]))]
    (if (>= score MAX-SCORE)
      (reduced (assoc new-state :finished true))
      new-state)))

(def rolls (map #(reduce + %) (partition 3 (iterate roll-die 1))))

(defn part-1 [data]
  (let [final (reduce step data rolls)
        {:keys [rolls scores]} final
        lower  (first scores)]
    (* lower rolls)))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 739785)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(comment
  "Now I need to figure out the number of universes in which
  each player wins. I'll do this with a recursive memoized
  function of a sort.")

(def MAX-SCORE-2 21)
(defn step-2 [state roll]
  (let [{:keys [positions scores]} state
        [a b] positions
        [sa sb] scores
        loc (project (+ a roll))
        score (+ sa loc)
        new-state (-> state
                      (assoc :positions [b loc])
                      (assoc :scores [sb score]))]
    (if (>= score MAX-SCORE-2)
      (assoc new-state :finished true)
      new-state)))

(defn vec-* [a v]
  (mapv * (repeat a) v))

(def universes
  (memoize
   (fn [state]
     (if (state :finished) [0 1]
         (apply mapv +
                (for [[times roll] [[1 3] [3 4] [6 5] [7 6] [6 7] [3 8] [1 9]]]
                  (vec-* times (reverse (universes (step-2 state roll))))))))))

(defn part-2 [data]
  (first (universes (dissoc data :rolls))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 444356092776315)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)






