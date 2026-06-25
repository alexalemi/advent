;; # Advent of Code 2025 - Day 1
(ns p01
  [:require [clojure.test :as test]
            [clojure.string :as str]])

(def data-string (str/trim (slurp "../input/01.txt")))
(def test-string "L68
L30
R48
L5
R60
L55
L1
L99
R14
L82")

(defn process [s]
  (map
    (fn [row] [(keyword (subs row 0 1))
               (Integer/parseInt (subs row 1))])
    (str/split s #"\n")))

(def data (process data-string))
(def test-data (process test-string))

(defn step [loc [dir amt]]
  (mod ((if (= dir :L) - +) loc amt) 100))

(defn part-1 [data]
  (count (filter zero? (reductions step 50 data))))

(test/deftest test-part-1
  (test/is (= 3 (part-1 test-data))))

(def ans-1 (part-1 data))


;; ## Part 2

(defn step-with-zeros [[loc zeros] [dir amt]]
  (let [op (if (= dir :L) - +)]
   (loop [zeros zeros
          loc loc
          amt amt]
     (if (< amt 0) [loc zeros]
       (recur (if (zero? loc) (inc zeros) zeros)
              (mod (op loc 1) 100)
              (dec amt))))))


(defn part-2 [data]
  (second (reduce step-with-zeros [50 0] data)))

(test/deftest test-part-1
  (test/is (= 6 (part-2 test-data))))

(def ans-2 (part-2 data))

(defn -main []
  (println "Answer 1:" ans-1)
  (println "Answer 2:" ans-2))
   

