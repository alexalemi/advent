(ns clojure.util
  (:require [clojure.string :as str]
            [clojure.math.combinatorics :as combo]
            [clojure.data.priority-map :as p-map]))

(def QUEUE clojure.lang.PersistentQueue/EMPTY)

(defn a-star
  "A simple generic a-star implementation."
  [start goal cost neighbors heuristic])
