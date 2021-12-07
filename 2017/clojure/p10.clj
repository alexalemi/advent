(ns advent10
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]
   [clojure.string :as str]))

(def test-string "3, 4, 1, 5")
(def data-string (str/trim (slurp "../input/10.txt")))

(defn read-vec [s]
  (edn/read-string (str "[" s "]")))

(def test-data [5 (read-vec test-string)])
(def data [256 (read-vec data-string)])

(defn initial-state [n]
  {:position 0 :skip-size 0 :vals (range n)})

(defn step [state length]
  (let [{:keys [position skip-size vals]} state
        n (count vals)
        x (cycle vals)
        x (drop position x)
        [cut x] (split-at length x)
        rest (take (- n length) x)
        joined (cycle (concat (reverse cut) rest))]
    (-> state
        (assoc :vals (take n (drop (- n position) joined)))
        (update :position + (+ skip-size length))
        (update :position mod n)
        (update :skip-size inc)
        (update :skip-size mod n))))

(defn part-1 [n data]
  (apply * (take 2 (:vals (reduce step (initial-state n) data)))))

(test/deftest test-part-1
  (test/is (= (apply part-1 test-data) 12))
  (test/is (= (apply part-1 data) 29240)))

(time (def ans1 (apply part-1 data)))
(println)
(println "Answer1: " ans1)

(defn expand-input [inp]
  (as-> inp s
    (map int s)
    (concat s [17 31 73 47 23])
    (repeat 64 s)
    (flatten s)))

(defn hexify [vs]
  (apply str (map #(format "%02x" %) vs)))

(defn knot-hash [s]
  (->> s
       expand-input
       (reduce step (initial-state 256))
       :vals
       (partition 16)
       (map (partial reduce bit-xor))
       hexify))

(knot-hash "1,2,3")

(test/deftest test-part-2
  (test/are [s h] (= (knot-hash s) h)
    "" "a2582a3a0e66e6e86e3812dcb672a272"
    "AoC 2017" "33efeb34ea91902bb2f59c9920caa6cd"
    "1,2,3" "3efbe78a8d82f29979031a4aa0b16a9d"
    "1,2,4" "63960835bcdc130f0b66d7ff4f6a5a8e")
  (test/is (= (knot-hash data-string) "4db3799145278dc9f73dcdbc680bd53d")))

(time (def ans2 (knot-hash data-string)))
(println)
(println "Answer 2:", ans2)

(test/run-tests)
