(ns p22
  (:require [clojure.string :as str]
            [util :as util]))

;; # 2016 - Day 22
;; For this puzzle, it looks like we have a big grid of nodes with some disk usage.

(def data-string (slurp "../input/22.txt"))

;; Looking at this data, it looks like there is a header the first two rows, and then things take a certain format

(defn to-int [x] (read-string x))

(defn process-line [line]
  (let [[_ x y size used avail use] (re-matches #"/dev/grid/node-x(\d+)-y(\d+)\s+(\d+)T\s+(\d+)T\s+(\d+)T\s+(\d+)%" line)]
    {:pos [(to-int x) (to-int y)]
     :size (to-int size)
     :used (to-int used)
     :avail (to-int avail)
     :use (to-int use)}))


(def data (mapv process-line (drop 2 (str/split-lines data-string))))


(defn viable? [nodea nodeb]
  (and nodeb
       (pos? (:used nodea))
       (<= (:used nodea) (:avail nodeb))))

(defonce ans1
    (count (filter (fn [[a b]] (viable? a b)) (for [nodea data nodeb data :when (not= nodea nodeb)] [nodea nodeb]))))
(println "Answer1: " ans1)

;; ## Part 2
;; For this part we need to figure out how to get all of the data in the top-right node over to the top-left square.


(def test-string "Filesystem            Size  Used  Avail  Use%
/dev/grid/node-x0-y0   10T    8T     2T   80%
/dev/grid/node-x0-y1   11T    6T     5T   54%
/dev/grid/node-x0-y2   32T   28T     4T   87%
/dev/grid/node-x1-y0    9T    7T     2T   77%
/dev/grid/node-x1-y1    8T    0T     8T    0%
/dev/grid/node-x1-y2   11T    7T     4T   63%
/dev/grid/node-x2-y0   10T    6T     4T   60%
/dev/grid/node-x2-y1    9T    8T     1T   88%
/dev/grid/node-x2-y2    9T    6T     3T   66%")
(def test-data (mapv process-line (drop 1 (str/split-lines test-string))))




(defn neighbor-locs [[x y]]
  [[(inc x) y]
   [(dec x) y]
   [x (inc y)]
   [x (dec y)]])

(defn wanted-key [data]
  [(reduce max (map first (filter #(= (second %) 0) (map :pos data)))) 0])

(defn hole-loc [data]
  (:pos (first (filter #(= (:used %) 0) data))))

(defn to-board [data]
  {:want (wanted-key data)
   :hole (hole-loc data)
   :grid (into {} (map (fn [node] [(:pos node) (select-keys node [:used :avail])]) data))})

(def board (to-board data))
(def test-board (to-board test-data))

(defn move [state frm to]
  (let [{want :want hole :hole} state
        data (:used ((:grid state) frm))]
   (-> state
       (assoc :want (if (= frm want) to want))
       (assoc :hole (if (= to hole) frm hole))
       (assoc-in [:grid frm :used] 0)
       (update-in [:grid frm :avail] + data)
       (update-in [:grid to :used] + data)
       (update-in [:grid to :avail] - data))))

(defn neighbors [board]
  (for [[loc node] (:grid board)
        loc-b (neighbor-locs loc)
        :when (viable? node ((:grid board) loc-b))]
     (move board loc loc-b)))


(defn goal? [board]
  (= (:want board) [0 0]))

(def cost (constantly (constantly 1)))

(defn heuristic [board]
  (let [[x y] (:want board)
        [x0 y0] (:hole board)]
    (+ x y (abs (- x0 x)) (abs (- y0 y)))))


(defn shortest-path-length [board]
  (dec (count (util/a-star
                board
                goal?
                cost
                neighbors
                heuristic))))


(shortest-path-length test-board)


(defn find-walls [data]
  (into #{} (map :pos (filter #(> (:used %) 100) data))))

(defn find-size [data]
  [(reduce max (map (comp first :pos) data))
   (reduce max (map (comp second :pos) data))])

(def compact-board
  (select-keys board [:want :hole]))

(def walls (find-walls data))
(def size (find-size data))


(defn compact-move [board to]
  (let [{hole :hole want :want} board]
   (-> board
       (assoc :want (if (= to want) hole want))
       (assoc :hole to))))

(defn compact-valid? [[x y]]
  (let [[X Y] size]
   (and
    (<= 0 x X)
    (<= 0 y Y)
    ((complement walls) [x y]))))


(defn compact-neighbors [board]
  (let [{hole :hole} board]
    (map (partial compact-move board)
         (filter compact-valid?
              (for [neigh (neighbor-locs hole)]
                 neigh)))))


(defonce ans2 (dec (count
                      (util/a-star
                       compact-board
                       goal?
                       cost
                       compact-neighbors
                       heuristic))))
(println "Answer2:" ans2)


