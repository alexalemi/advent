ans advent09
  (:require
   [clojure.test :as test])

(def data (slurp "../input/09.txt"))

(def initial-state {:garbage false :bang false :depth 0 :score 0 :garbage-count 0})

(defn step [state c]
  (cond
    (:bang state) (assoc state :bang false)
    (= c \!) (assoc state :bang true)
    (:garbage state) (if (= c \>)
                       (assoc state :garbage false)
                       (update state :garbage-count inc))
    (= c \<) (assoc state :garbage true)
    (= c \{) (update state :depth inc)
    (= c \}) (-> state
                 (update :score + (:depth state))
                 (update :depth dec))
    :else state))

(defn part-1
  [s] (:score (reduce step initial-state s)))

(test/deftest test-part-1
  (test/testing "score function"
    (test/are [x y] (= (part-1 x) y)
      "{}" 1
      "{{{}}}" 6
      "{{},{}}" 5
      "{{{},{},{{}}}}" 16
      "{<a>,<a>,<a>,<a>}" 1
      "{{<ab>},{<ab>},{<ab>},{<ab>}}" 9
      "{{<!!>},{<!!>},{<!!>},{<!!>}}" 9
      "{{<a!>},{<a!>},{<a!>},{<ab>}}" 3)
    (test/is (= (part-1 data) 9662))))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1: " ans1)

(defn part-2
  [s] (:garbage-count (reduce step initial-state s)))

(test/deftest test-part-2
  (test/are [x y] (= (part-2 x) y)
    "<>" 0
    "<random characters>" 17
    "<<<<>" 3
    "<{!>}>" 2
    "<!!>" 0
    "<!!!>>" 0
    "<{o\"i!a,<{i<a>" 10)
  (test/is (= (part-2 data) 4903)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2: " ans2)

(test/run-tests)
