;; # 🎄 Advent of Code 2018 - Day 20 - A Regular Map
;; For this puzzle, we are given a `regex`-like string that
;; tells us a sequence of paths that are possible in some maze
;; and from that we need to reconstruct the map and discern how
;; far away the farthest room is.
(ns p20
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (str/trim (slurp "../input/20.txt")))

(def test-string "^ENWWW(NEEE|SSE(EE|N))$")

;; The first thing we're going to do is try to parse this string
;; to figure out all of the connections in the map.  My plan is
;; to do this by simply creating a queue wherein we keep track
;; of the current location we are at as well as the tail end
;; of the rest of the regex we need to parse.  If there is a
;; single letter we'll just add the corresponding connection (as a two
;; element set in a set of connections) and if its an opening
;; parenthesis then we'll add children, one for each of the segments of the
;; regex.

(def QUEUE clojure.lang.PersistentQueue/EMPTY)

(defn queue
  ([] (QUEUE))
  ([coll] (reduce conj QUEUE coll)))

(defn neighbor [[x y] direction]
  (case direction
    \W [(dec x) y]
    \E [(inc x) y]
    \N [x (inc y)]
    \S [x (dec y)]))

(defn forks
  "Find all of the forking continuations of a given regex."
  [regex]
  (loop [depth 0
         forks []
         path []
         regex regex]
    (let [[next & remaining] regex]
      (case next
        \| (if (= depth 0)
             (recur depth (conj forks path) [] remaining)
             (recur depth forks (conj path next) remaining))
        \( (recur (inc depth) forks (conj path next) remaining)
        \) (if (= depth 0)
             (for [fork (conj forks path)]
               (concat fork remaining))
             (recur (dec depth) forks (conj path next) remaining))
        (recur depth forks (conj path next) remaining)))))

(defn peek! [tvec] (get tvec (dec (count tvec))))

(defn assemble-connections
  "Given a regex, assemble all of the observed connections."
  [regex]
  (assert (str/starts-with? regex "^"))
  (assert (str/ends-with? regex "$"))
  (loop [connections (transient #{})
         frontier (transient [[[0 0] (butlast (rest regex))]])]
    ; (println "Total connections = " (count connections) " frontier size=" (count frontier) " strlength = " (count (second (peek frontier))))
    (if-let [[loc [next & remaining]] (peek! frontier)]
      (case next
        \( (recur connections (reduce conj! (pop! frontier) (map vector (repeat loc) (forks remaining))))
        nil (recur connections (pop! frontier))
        (\N \S \E \W) (let [nextloc (neighbor loc next)]
                        (recur (conj! connections #{loc nextloc}) (conj! (pop! frontier) [nextloc remaining])))
        (recur connections (conj! (pop! frontier) [loc remaining])))
      (persistent! connections))))

(defn raw-neighbors [[x y]]
  [[x (inc y)]
   [(dec x) y]
   [(inc x) y]
   [x (dec y)]])

(defn furthest-distance [connections]
  (loop [max-dist 0
         seen #{}
         frontier (queue [[0 [0 0]]])]
    (println "Total seen = " (count seen) " max-dist = " max-dist " frontier count = " (count frontier))
    (if-let [[dist loc] (peek frontier)]
      (let [neighs (sequence
                    (comp
                     (remove seen)
                     (filter (fn [x] (connections #{loc x}))))
                    (raw-neighbors loc))]
        (recur (max max-dist dist)
               (into seen neighs)
               (into (pop frontier) (map vector (repeat (inc dist)) neighs))))
      ; else
      max-dist)))

(test/deftest test-part-1
  (test/are [n r] (= n (furthest-distance (assemble-connections r)))
    3 "^WNE$"
    10 "^ENWWW(NEEE|SSE(EE|N))$"
    18 "^ENNWSWW(NEWS|)SSSEEN(WNSE|)EE(SWEN|)NNN$"
    23 "^ESSWWN(E|NNENN(EESS(WNSE|)SSS|WWWSSSSE(SW|NNNE)))$"
    31 "^WSSEESWWWNW(S|NENNEEEENN(ESSSSW(NWSW|SSEN)|WSWWN(E|WWS(E|SS))))$"))

(def ans1 (time (furthest-distance (assemble-connections data-string))))

;; ## Part 2

(def ans2 :undefined)

;; ## Main

(defn -test [_]
  (test/run-tests))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
