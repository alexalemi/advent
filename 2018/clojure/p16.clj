;; # 2018 Day 16
(ns p16
  (:require [clojure.string :as str]))


(defn process-observation [lines]
  (let [[_ & args] (re-matches #"Before: \[(\d+), (\d+), (\d+), (\d+)\]\n(\d+) (\d+) (\d+) (\d+)\nAfter:  \[(\d+), (\d+), (\d+), (\d+)\]" lines)
          args (mapv read-string args)]
       {:before (subvec args 0 4)
        :command (subvec args 4 8)
        :after (subvec args 8)}))


(def data
  (let [[observations examples] (str/split (slurp "../../2018/input/16.txt") #"\n\n\n\n")
        observations (str/split observations #"\n\n")
        observations (mapv process-observation observations)
        examples (str/split-lines examples)]
    ;(re-find #"Before: \[(\d+), (\d+), (\d+), (\d+)\]\n(\d+) (\d+) (\d+) (\d+)\nAfter: \[(\d+), (\d+), (\d+), (\d+)\]" (first observations))
    examples))
