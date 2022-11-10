(ns p15
 (:require [clojure.string :as str]))

(def data (slurp "../input/15.txt"))
(def test-data "Disc #1 has 5 positions; at time=0, it is at position 4.
Disc #2 has 2 positions; at time=0, it is at position 1.")

(defn to-int [x] (read-string x))

(defn parse-line [line]
  (let [[_ id size pos] (re-matches #"Disc #(\d+) has (\d+) positions; at time=0, it is at position (\d+)." line)]
    {:id (to-int id) 
     :size (to-int size) 
     :pos (to-int pos)}))
  
