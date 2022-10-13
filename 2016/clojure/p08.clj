(ns p08
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/08.txt"))
(def size [50 6])

(def test-string "rect 3x2
rotate column x=1 by 1
rotate row y=0 by 4
rotate column x=1 by 1")

(defn to-num [x] (read-string x))

(defn parse-line [line]
     (let [[_ rect-width rect-height row-num rotate-row col-num rotate-col] (re-matches #"(?:(?:rect (\d+)x(\d+))|(?:rotate row y=(\d+) by (\d+))|(?:rotate column x=(\d+) by (\d+)))" line)]
       (cond
         (and rect-width rect-height) {:type :rect :width (to-num rect-width) :height (to-num rect-height)}
         (and row-num rotate-row) {:type :row :row (to-num row-num) :amount (to-num rotate-row)}
         (and col-num rotate-col) {:type :col :col (to-num col-num) :amount (to-num rotate-col)})))


(def data {:width (first size)
           :height (second size)
           :board #{}
           :instructions (map parse-line (str/split-lines data-string))})


(def test-data {:width 7
                :height 3
                :board #{}
                :instructions (map parse-line (str/split-lines test-string))})
;; => {:width 7, :height 3,
;;     :instructions ({:type :rect, :width 3, :height 2} {:type :col, :col 1, :amount 1} {:type :row, :row 0, :amount 4} {:type :col, :col 1, :amount 1})};


(defn fill-rect [board {width :width height :height}]
  (into board (for [x (range width) y (range height)] [x y])))


(defn in-row [row [x y]] (= y row))
(defn in-col [col [x y]] (= x col))

(defn cycle-row [amount width [x y]] [(mod (+ x amount) width) y])
(defn cycle-col [amount height [x y]] [x (mod (+ y amount) height)])

(comment
  (let [board #{[0 0] [1 0] [2 0] [0 1] [1 1] [2 1]} {row :row amount :amount} {:type :rotate-row :row 1 :amount 3} {height :height width :width} {:height 2 :width 7}]))

(defn rotate-row [board {row :row amount :amount} {width :width}]
    (let [row (filter (partial in-row row) board)
          left (apply disj board row)]
      (into left (map (partial cycle-row amount width) row))))

(defn rotate-col [board {col :col amount :amount} {height :height}]
    (let [col (filter (partial in-col col) board)
          left (apply disj board col)]
      (into left (map (partial cycle-col amount height) col))))

(defn step [data move]
   (let [{board :board} data]
     (assoc data :board
       (case (:type move)
         :rect (fill-rect board move)
         :row (rotate-row board move data)
         :col (rotate-col board move data)))))

(defn round [data]
  (update
   (reduce step data (:instructions data))
   :time (fnil inc 0)))


(comment
   (-> (nth (iterate round test-data) 1)
       :board
       count))

(def ans1 (count (:board (round data))))
(println "Answer1" ans1)
