(ns advent05
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]
   [clojure.string :as string]))


(def data
  (into [] (map edn/read-string
            (string/split-lines
             (slurp "../input/05.txt")))))
;; => (2 1 1 0 ...)

(defn exit
  "Find the exit of a sequence of instructions."
  ([tape] (exit 0 0 (transient tape)))
  ([loc step tape]
   (if-let [val (get tape loc)]
     (recur (+ loc val) (inc step) (assoc! tape loc (inc val)))
     step)))

(test/deftest test-part-1
  (test/is (= (exit [0 3 0 1 -3]) 5))) 

(time (def ans1 (exit data)))

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

(time (def ans2 (exit-2-array data)))

(test/run-tests)

(println)
(println "Answer1:", ans1)
(println "Answer2:", ans2)
