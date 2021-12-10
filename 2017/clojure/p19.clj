(ns advent19
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))


(def test-string "     |          
     |  +--+    
     A  |  C    
 F---|----E|--+ 
     |  |  |  D 
     +B-+  +--+
")
(def data-string (slurp "../input/19.txt"))

(defn indexify [coll]
  (zipmap (range) coll))

(defn find-first [pred coll] (first (filter pred coll)))

(defn process [s]
  (let [board (for [[row line] (indexify (str/split-lines s))
                    [col c]    (indexify line)
                    :when (not= c \space)]
                [[row col] c])
        [start _] (find-first (fn [[loc _]] (let [[x _] loc] (zero? x))) board)]
    {:board board :loc start :direction :down :visited []}))

(defn neighbors [loc]
  (let [[x y] loc]
    [[(inc x) y]
     [(dec x) y]
     [x (inc y)]
     [x (dec y)]]))

(defn move [loc direction]
  (let [[x y] loc]
    (case direction
      :up    [x (inc y)]
      :down  [x (dec y)]
      :left  [(inc x) y]
      :right [(dec x) y])))

(def data (process data-string))
(def test-data (process test-string))

