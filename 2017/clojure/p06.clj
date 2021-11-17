(ns advent06
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]
   [clojure.string :as string]))


(def data (into [] (map edn/read-string (string/split (slurp "../input/06.txt") #"\t"))))
;; => (5 1 10 0 1 7 13 14 3 12 8 10 7 12 0 6)

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
  (test/is (= (redistribute [0 2 7 0]) [2 4 1 2])) 
  (test/is (= (redistribute [2 4 1 2]) [3 1 2 3])) 
  (test/is (= (redistribute [3 1 2 3]) [0 2 3 4])) 
  (test/is (= (redistribute [0 2 3 4]) [1 3 4 1])) 
  (test/is (= (redistribute [1 3 4 1]) [2 4 1 2])) 
  (test/is (= (first-repeat [0 2 7 0]) 5)))

(time (def ans1 (first-repeat data)))

(comment 
 (defn exit-2
   "Find the exit of a sequence of instructions."
   ([tape] (exit-2 0 0 (transient tape)))
   ([loc step tape]
    (if-let [val (get tape loc)]
      (let [new-val ^int (if (>= val 3) (dec val) (inc val))]
        (recur (+ loc val) (inc step) (assoc! tape loc new-val)))
      step)))

 (defn exit-2-array
   "Find the exit of a sequence of instructions."
   [tape]
   (let [size (count tape)]
     (loop [tape (int-array tape)
            loc  0
            step 0]
       (if (or (< loc 0) (>= loc size)) step
           (let [val ^int (aget tape loc)
                 new-val ^int (if (>= val 3) (dec val) (inc val))]
             (aset ^ints tape loc new-val)
             (recur
               ^ints tape
               (+ loc val)
               (inc step)))))))

 (test/deftest test-part-2
   (test/is (= (exit-2-array [0 3 0 1 -3]) 10))) 

 (time (def ans2 (exit-2-array data))))


(test/run-tests)

(println)
(println "Answer1:", ans1)
; (println "Answer2:", ans2)


