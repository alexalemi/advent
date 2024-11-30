(ns p01
  [:require [clojure.test :as test]])
            

(defonce data-string (slurp "../input/01.txt"))

(defn find-floor [commands]
  (reduce 
    (fn [floor chr] 
      (if (= chr \() 
          (inc floor) (dec floor))) 
    0 commands))


(test/deftest part-1
  (test/are [x y] (= (find-floor x) y)
    "()()" 0
    "(())" 0
    "(((" 3
    "(()(()(" 3
    "))(((((" 3
    "())" -1
    "))(" -1
    ")))" -3
    ")())())" -3))

(def ans1 (find-floor data-string)) 

(defn find-first-basement [commands]
  (->> commands
       (reductions (fn [floor chr] (if (= chr \() (inc floor) (dec floor))) 0)
       (keep-indexed (fn [i x] (when (= x -1) i)))
       (first)))

(test/deftest part-2
  (test/are [x y] (= (find-first-basement x) y)
    ")" 1
    "()())" 5))
       
(def ans2 (find-first-basement data-string))

(comment
  (test/run-tests))

(defn -main [& args]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

