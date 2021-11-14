(ns advent02
  (:require
   [clojure.edn :as edn]
   [clojure.test :as test]
   [clojure.string :as string]))


(def data (edn/read-string (string/trim (slurp "../input/03.txt"))))


; Part 1
(def directions
  "Infinite sequence of spiral directions."
  (let [dirs (cycle [[:right :up] [:left :down]])
        amount (map inc (range))]
    (mapcat (fn [[d1 d2] amount]
                (concat (repeat amount d1)
                        (repeat amount d2)))
            dirs
            amount))) 

(defn next-tile
  "Calculates the next tile from the current tile and direction."
  [{x :x y :y} direction]
  (case direction
    :right {:x (inc x) :y y}
    :left  {:x (dec x) :y y}
    :up    {:x x :y (inc y)}
    :down  {:x x :y (dec y)}))


(defn tile-distance
  "Compute the manhattan distance of a tile."
  [tile]
  (+ (Math/abs (:x tile)) (Math/abs (:y tile))))


(defn tile-at
  "Get the nth tile."
  [n]
  (reduce next-tile {:x 0 :y 0} (take (dec n) directions)))


(defn spiral-distance
  "Figure out the manhattan distance of a point on a spiral"
  [n]
  (tile-distance (tile-at n)))
 

(def ans1 (spiral-distance data))


(test/deftest test-part-1
  (test/are [x y] (= (spiral-distance x) y)
       1 0
       12 3
       23 2
       1024 31)) 


(comment "I need to figure out the value of each tile, so I need to store all of the tiles by their location and store their value in there.")

(def neighbor-directions [[:up] [:up :right] [:up :left] [:left] [:down :left] [:down] [:down :right] [:right]])

(defn neighbors
  "Get all of the neighbors of a tile."
  [tile]
  (map #(reduce next-tile tile %) neighbor-directions))


(defn neighbor-total
  "Add up the total of all of the existing tiles."
  [tile tiles]
  (reduce + (map #(get tiles % 0) (neighbors tile))))

(defn first-tile-greater
  "Find the first tile with a value greater than that given."
  ([n] (first-tile-greater n {:x 0 :y 0} 1 {{:x 0 :y 0} 1} directions))
  ([n tile val tiles directions]
   (if (> val n) val
       (let [dir (first directions)
             next (next-tile tile dir)
             next-val (neighbor-total next tiles)]
         (recur n next next-val (assoc tiles next next-val) (rest directions))))))


(test/deftest test-part-2
  (test/are [x y] (= (first-tile-greater x) y)
       0 1 
       1 2
       3 4
       4 5
       5 10
       10 11
       11 23))


(def ans2 (first-tile-greater data))


(test/run-tests)

(println)
(println "Answer1:", ans1)
(println "Answer2:", ans2)

