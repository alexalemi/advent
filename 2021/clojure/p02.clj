(ns advent.day02
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]
   [clojure.string :as str]))


(defn process-line [line]
  {:direction (keyword (edn/read-string line))
   :amount (edn/read-string (last (str/split line #" ")))})

(defn process [inp]
  (->> (str/split-lines inp)
       (map str/trim)
       (map process-line)
       (into [])))

(def data (process (slurp "../input/02b.txt")))

(defn move
  "Do a single command of the sub"
  [position command]
  (let [{:keys [direction amount]} command]
    (case direction
      :forward (update position :horizontal + amount)
      :down (update position :depth + amount)
      :up (update position :depth - amount))))

(defn part-1 [data]
  (let [final-position (reduce move {:horizontal 0 :depth 0} data)]
    (* (:horizontal final-position) (:depth final-position))))

(time (def ans1 (part-1 data)))
(println)
(println "Part1:" ans1)

(def test-string "forward 5
  down 5
  forward 8
  up 3
  down 8
  forward 2")

(def test-data (process test-string))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 150)))

(defn move-2
  "Do a single command of the sub"
  [position command]
  (let [{:keys [direction amount]} command]
    (case direction
      ; :forward (update (update position :horizontal + amount) :depth + (* aim amount))
      :forward (update (update position :horizontal + amount) :depth + (* (:aim position) amount))
      :down (update position :aim + amount)
      :up (update position :aim - amount))))


(defn part-2 [data]
  (let [final-position (reduce move-2 {:horizontal 0 :depth 0 :aim 0} data)]
    (* (:horizontal final-position) (:depth final-position))))

(time (def ans2 (part-2 data)))
(println)
(println "Part2:" ans2)

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 900)))

(test/run-tests)
