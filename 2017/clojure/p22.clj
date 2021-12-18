(ns advent22
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def data-string (slurp "../input/22.txt"))
(def test-string "..#
#..
...")

(defn enumerate [coll]
  (zipmap (range) coll))

(defn process [s]
  (let [n (quot (count (str/split-lines s)) 2)
        infected
        (set (for [[y line] (enumerate (str/split-lines s))
                   [x c] (enumerate line)
                   :when (= c \#)]
               [x y]))]
    {:infected infected
     :infections 0
     :position [n n]
     :direction :north}))

(def data (process data-string))
(def test-data (process test-string))

(def turn-right
  {:north :east
   :east :south
   :south :west
   :west :north})

(def turn-left
  {:north :west
   :west :south
   :south :east
   :east :north})

(defn toggle [coll val]
  (if (coll val) (disj coll val) (conj coll val)))

(defn move [position direction]
  (let [[x y] position]
    (case direction
      :north [x (dec y)]
      :west  [(dec x) y]
      :east  [(inc x) y]
      :south [x (inc y)])))

(defn step [state]
  (let [{:keys [infected direction position]} state
        is-infected (infected position)
        new-direction ((if is-infected turn-right turn-left) direction)]
    (-> state
        (assoc :direction new-direction)
        (update :infections (if is-infected identity inc))
        (update :infected toggle position)
        (update :position move new-direction))))

(defn part-1 [data n]
  (:infections (nth (iterate step data) n)))

(defn step! [state]
  (let [{:keys [infected direction position infections]} state
        is-infected (infected position)
        new-direction ((if is-infected turn-right turn-left) direction)]
    (-> state
        (assoc! :direction new-direction)
        (assoc! :infections ((if is-infected identity inc) infections))
        (assoc! :infected (toggle infected position))
        (assoc! :position (move position new-direction)))))

(defn part-1! [data n]
  (:infections (nth (iterate step! (transient data)) n)))

(test/deftest test-part-1
  (test/are [t n] (= (part-1! test-data t) n)
    70 41
    10000 5587))

(time (def ans1 (part-1! data 10000)))
(println)
(println "Answer 1:" ans1)

(def evolve
  {:clean :weakened
   :weakened :infected
   :infected :flagged
   :flagged :clean})

(defn process-2 [data]
  (-> data
      (assoc  :status (zipmap (:infected data) (repeat :infected)))
      (dissoc :infected)))

(def data-2 (process-2 data))
(def test-data-2 (process-2 test-data))

(def turn-around
  {:north :south
   :south :north
   :west :east
   :east :west})

(def direction-change-from-status
  {:clean turn-left
   :weakened identity
   :infected turn-right
   :flagged turn-around})

(defn step-2 [state]
  (let [{:keys [status direction position]} state
        current-status (get status position :clean)
        new-direction ((direction-change-from-status current-status) direction)]
    (-> state
        (assoc :direction new-direction)
        (update-in [:status position] (fnil evolve :clean))
        (update :infections (if (= current-status :weakened) inc identity))
        (update :position move new-direction))))

(defn part-2 [data]
  (:infections (nth (iterate step-2 data) 10000000)))

(defn step-2! [state]
  (let [{:keys [status direction position infections]} state
        current-status (get status position :clean)
        new-direction ((direction-change-from-status current-status) direction)]
    (-> state
        (assoc! :direction new-direction)
        (assoc! :status (assoc status position ((fnil evolve :clean) current-status)))
        (assoc! :infections ((if (= current-status :weakened) inc identity) infections))
        (assoc! :position (move position new-direction)))))

(defn part-2! [data]
  (:infections (nth (iterate step-2! (transient data)) 10000000)))

(time (def ans2 (part-2! data-2)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
