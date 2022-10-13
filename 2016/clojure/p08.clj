(ns p08
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/08.txt"))
(def size [50 6])

(def test-string "rect 3x2
rotate column x=1 by 1
rotate row y=0 by 4
rotate column x=1 by 1")

(comment
  (let [line (first (str/split-lines test-string))]
     (let [[_ rect-width rect-height row-num rotate-row col-num rotate-col] (re-matches #"(?:(?:rect (\d+)x(\d+))|(?:rotate row y=(\d+) by (\d+))|(?:rotate column x=(\d+) by (\d+)))" line)]
       [_ rect-width rect-height row-num rotate-row col-num rotate-col])))
