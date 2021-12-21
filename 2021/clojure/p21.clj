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
     :turn 0
     :rolls 0
     :dice 1
     :scores [0 0]}))

(def data (process data-string))
(def test-data (process test-string))

(def DIE-SIDES 100)
(defn roll-die [s]
  (if (= s DIE-SIDES) 1 (inc s)))

(def STATES 10)
(defn new-pos [loc plus]
  (inc (mod (+ (dec loc) plus) STATES)))

(defn step [state]
  (let [{:keys [positions turn dice scores]} state
        max-score (apply max scores)
        die (iterate roll-die dice)
        [roll, die] (split-at 3 die)
        face (first die)
        who (mod turn 2)
        new-loc (new-pos (positions who) (reduce + roll))]
    (if (>= max-score 1000)
      (assoc state :finished true)
      (-> state
          (assoc :dice face)
          (update :turn inc)
          (update :rolls + 3)
          (assoc-in [:positions who] new-loc)
          (update-in [:scores who] + new-loc)))))

(defn part-1 [data]
  (let [final (last (take-while (complement :finished) (iterate step data)))
        {:keys [rolls scores]} final
        lower-score (apply min scores)]
    (* lower-score rolls)))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 739785)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn refine [data]
  {:positions (:positions data)
   :turn 0
   :scores [0 0]
   :rolls []})

(def data-2 (refine data))
(def test-data-2 (refine test-data))

(comment
  "Now I need to figure out the number of universes in which
  each player wins. I'll do this with a recursive memoized
  function of a sort.")

(defn get-winner [scores]
  (let [[a b] scores]
    (cond
      (>= a 21) 0
      (>= b 21) 1
      :else nil)))

(defn project [n]
  (inc (mod (dec n) 10)))

(defn step-2 [state roll]
  (let [{:keys [positions turn scores rolls]} state
        who (mod turn 2)]
    (cond
      (= (count rolls) 2)
      (let [new-loc (project (apply + (positions who) roll rolls))
            new-scores (update scores who + new-loc)
            winner (get-winner new-scores)]
        (-> state
            (update :turn inc)
            (assoc :rolls [])
            (assoc-in [:positions who] new-loc)
            (update-in [:scores who] + new-loc)
            (assoc :winner winner)))

      :else
      (-> state
          (update :rolls conj roll)))))

(def universes
  (memoize
   (fn [state]
     (let [{:keys [winner]} state]
       (if winner
         (if (= winner 0) [1 0] [0 1])
         (mapv +
               (universes (step-2 state 1))
               (universes (step-2 state 2))
               (universes (step-2 state 3))))))))

(defn part-2 [data]
  (apply max (universes data)))

(test/deftest test-part-2
  (test/is (= (part-2 test-data-2) 444356092776315)))

(time (def ans2 (part-2 data-2)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)






