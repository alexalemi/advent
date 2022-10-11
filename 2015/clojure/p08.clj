(ns p08
  (:require [clojure.string :as str]))

(defonce data-string (str/trim (slurp "../input/08.txt")))

(def test-data
  "\"\"\n\"abc\"\n\"aaa\\\"aaa\"\n\"\\x27\"")

(defn frest [x] (first (rest x)))
(defn rrest [x] (rest (rest x)))
(defn rrrrest [x] (rest (rest (rest (rest x)))))

(defn process 
  ([s] (process (seq s) {:code 0 :chars 0 :lines 1}))
  ([s data]
   (if-let [x (first s)]
     (case x 
       \" (recur (rest s) 
                 (if (:in-string data)
                   (-> data (update :code inc) (assoc :in-string false))
                   (-> data (update :code inc) (assoc :in-string true))))
       \\ (let [y (frest s)]
            (case y
              \" (recur (rrest s) (-> data (update :code + 2) (update :chars inc)))
              \\ (recur (rrest s) (-> data (update :code + 2) (update :chars inc)))
              \x (recur (rrrrest s) (-> data (update :code + 4) (update :chars inc)))))
       \newline (recur (rest s) (update data :lines inc))
       (recur (rest s) (-> data (update :code inc) (update :chars inc))))
     data)))
          
  

(comment
  (process data-string)

  (process "\"\"")
  (process "\"abc\"")
  (process "\"aaa\\\"aaa\""))

(defn part1 [s]
  (let [{code :code chrs :chars} (process s)]
     (- code chrs)))

(defonce ans1 (part1 data-string))
(println "Answer1:" ans1)

(defn escape
  ([s] (escape s nil))
  ([s out]
   (if-let [x (first s)]
     (case x
       \" (recur (rest s) (conj out \\ \"))
       \\ (recur (rest s) (conj out \\ \\))
       (recur (rest s) (conj out x)))
     (apply str (reverse out)))))

(defn part2 [s]
  (let [{code1 :code} (process s)
        n (escape s)
        {code2 :code lines2 :lines} (process n)]
    (- (+ code2 (* 2 lines2)) code1)))


(def ans2 (part2 data-string))
(println "Answer2:" ans2)

(comment
  (map process (str/split-lines test-data))
  (apply str [\a \b])
  (let [x "\"\""
        y (escape x)]
    [(process x)])
  (escape "\"abc\"")
  (escape "\"abc\"")
  (+ 4 7 14 9)
  (map process (map escape (str/split-lines test-data)))
  (process (escape test-data))
  (+ 34 (* 2 4))
 
  (part2 test-data))
