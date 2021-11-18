(ns advent06
  (:require
   [clojure.edn :as edn]
   [clojure.string :as string]
   [clojure.test :as test]))

(def data (into [] (map edn/read-string (string/split (slurp "../input/06.txt") #"\t"))))
;; => [5 1 10 0 1 7 13 14 3 12 8 10 7 12 0 6]

(defn maximum
  "Return the index of the maximum element"
  [banks]
  (apply max-key second (reverse (map-indexed vector banks))))

(defn redistribute
  "Do a single round of redistribution.

  Takes the elements in the biggest bin and starts
  dealing them out to the other slots."
  ([banks]
   (let [[index blocks] (maximum banks)
         new-banks (assoc banks index 0)
         safe-index (mod (inc index) (count banks))]
     (redistribute new-banks safe-index blocks)))
  ([banks index blocks]
   (if (> blocks 0)
     ; redistribute
     (recur (assoc banks index (inc (get banks index))) (mod (inc index) (count banks)) (dec blocks))
     banks)))

(defn first-repeat
  "Figure out how many redistributions until a repeat"
  ([banks] (first-repeat banks #{} 0))
  ([banks seen counter]
   (let [new-banks (redistribute banks)]
     (if (contains? seen new-banks) (inc counter)
         (recur new-banks (conj seen new-banks) (inc counter))))))

(test/deftest test-part-1
  (test/are [x y] (= (redistribute x) y)
    [0 2 7 0] [2 4 1 2]
    [2 4 1 2] [3 1 2 3]
    [3 1 2 3] [0 2 3 4]
    [0 2 3 4] [1 3 4 1]
    [1 3 4 1] [2 4 1 2]))

(test/deftest test-part-1-2
  (test/is (= (first-repeat [0 2 7 0]) 5)))

(time (def ans1 (first-repeat data)))

(comment "for part two we are supposed to figure out
the length of the cycle.")

(defn cycle-length
  "Figure out how many redistributions until a repeat"
  ([banks] (cycle-length banks {} 0))
  ([banks seen counter]
   (let [new-banks (redistribute banks)]
     (if (contains? seen new-banks) (- counter (get seen new-banks))
         (recur new-banks (assoc seen new-banks counter) (inc counter))))))

(test/deftest test-part-2
  (test/is (= (cycle-length [0 2 7 0]) 4)))

(time (def ans2 (cycle-length data)))

(test/run-tests)

(println)
(println "Answer1:", ans1)
(println "Answer2:", ans2)


