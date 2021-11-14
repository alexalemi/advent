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
  ([tape] (exit 0 0 tape))
  ([loc step tape]
   (if (or (< loc 0) (>= loc (count tape))) step
       (let [val (get tape loc)]
        (recur
          (+ loc val)
          (inc step)
          (assoc tape loc (inc val)))))))

(test/deftest test-part-1
  (test/is (= (exit [0 3 0 1 -3]) 5))) 

(def ans1 (exit data))

(defn exit-2
  "Find the exit of a sequence of instructions."
  ([tape] (exit-2 0 0 tape))
  ([loc step tape]
   (if (or (< loc 0) (>= loc (count tape))) step
       (let [val (get tape loc)
             new-val (if (>= val 3) (dec val) (inc val))]
         (recur
          (+ loc val)
          (inc step)
          (assoc tape loc new-val))))))

(test/deftest test-part-2
  (test/is (= (exit-2 [0 3 0 1 -3]) 10))) 

(def ans2 (exit-2 data))

(test/run-tests)

(println)
(println "Answer1:", ans1)
(println "Answer2:", ans2)
