(ns p18
  (:require [clojure.string :as str]))

(def data-string (str/trim (slurp "../input/18.txt")))
(def test-string "..^^.")

(def RULES
  #{[\^ \^ \.]
    [\. \^ \^]
    [\^ \. \.]
    [\. \. \^]})


(defn advance [state]
  (->> (concat [\.] state [\.])
       (partition 3 1)
       (map #(if (RULES %) \^ \.))
       (apply str)))

(defn to-state [s]
  {:row 0 :safe 0 :tiles s})

(defn step [state]
  (-> state
      (update :tiles advance)
      (update :safe + (count (filter #(not= \^ %) (:tiles state))))
      (update :row inc)))


(defn total-safe [n starting-tiles]
  (:safe (nth (iterate step (to-state starting-tiles)) n)))

(total-safe 10 ".^^.^.^^^^")

(defonce ans1 (total-safe 40 data-string))
(println "Answer1:" ans1)

(defonce ans2 (total-safe 400000 data-string))
(println "Answer2:" ans2)
