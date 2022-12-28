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
;; to figure out all of the connections in the map.
;;
;; The eventual goal will be to build up a datastructure representing all of the
;; doors on the map. That will be a set of sets, the set of all doors where each door
;; will itself be represented by a set of the two locations it services.
;;
;; Locations themselves will be `[x y]` tuples.

(defn neighbor [[x y] direction]
  (case direction
    \W [(dec x) y]
    \E [(inc x) y]
    \N [x (inc y)]
    \S [x (dec y)]))

#_(defn forks
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

#_(defn peek! [tvec] (get tvec (dec (count tvec))))

(defn assemble-connections
  "Given a regex, assemble all of the observed connections."
  [input-regex]
  (assert (str/starts-with? input-regex "^"))
  (assert (str/ends-with? input-regex "$"))
  (loop [connections #{}
         frontier []
         loc nil
         regex nil
         queue []
         jobs [{:loc [0 0]
                :regex (butlast (rest input-regex))
                :frontier []
                :queue []}]]
    ;(println "Total connections = " (count connections) " loc =" loc " frontier size=" (count frontier) " strlength = " (count regex) " count jobs = " (count jobs))
    (if-let [next (first regex)]
      ;; If we have a frontier to process
      (case next
        (\N \S \E \W)  ;; the usual case, just add a connection and continue
        (let [nextloc (neighbor loc next)]
          (recur
           (conj connections #{loc nextloc})
           frontier
           nextloc
           (rest regex)
           queue
           jobs))
        \( ;; We've hit a fork, enqueue the current loc and continue
        (recur
         connections
         (conj frontier #{})
         loc
         (rest regex)
         (conj queue loc)
         jobs)
        \| ;; We've hit a branch, grab an enqueued loc
        (recur
         connections
         (conj (pop frontier) (conj (peek frontier) loc))
         (peek queue)
         (rest regex)
         queue
         jobs)
        \) ;; We've finished a branch, pop from the queue)
        (recur
         connections
         (pop frontier)
         loc
         (rest regex)
         (pop queue)
         (into jobs
               (for [branch (disj (peek frontier) loc)]
                 [{:loc branch
                   :regex (rest regex)
                   :queue (pop queue)
                   :frontier (pop frontier)}]))))
      ;; Otherwise we're finished
      (if-let [job (first jobs)]
        (let [{:keys [loc regex queue frontier]} job]
          ;(println "Popping a job, loc = " loc " regex = " regex)
          (recur
           connections
           frontier
           loc
           regex
           queue
           (rest jobs)))
        connections))))

(defn raw-neighbors [[x y]]
  [[x (inc y)]
   [(dec x) y]
   [(inc x) y]
   [x (dec y)]])

(def QUEUE clojure.lang.PersistentQueue/EMPTY)

(defn queue
  ([] (QUEUE))
  ([coll] (reduce conj QUEUE coll)))

(defn furthest-distance [connections]
  (loop [max-dist 0
         seen #{}
         frontier (queue [[0 [0 0]]])]
    ;(println "Total seen = " (count seen) " max-dist = " max-dist " frontier count = " (count frontier))
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

(def data-rooms (assemble-connections data-string))
(def ans1 (time (furthest-distance data-rooms)))

;; ## Part 2

(defn at-least-a-distance [cutoff connections]
  (loop [seen {}
         frontier (queue [[0 [0 0]]])]
    ;(println "Total seen = " (count seen) " max-dist = " max-dist " frontier count = " (count frontier))
    (if-let [[dist loc] (peek frontier)]
      (let [neighs (sequence
                    (comp
                     (remove seen)
                     (filter (fn [x] (connections #{loc x}))))
                    (raw-neighbors loc))]
        (recur (into seen (for [neigh neighs] [neigh (inc dist)]))
               (into (pop frontier) (map vector (repeat (inc dist)) neighs))))
      ; else
      (count (filter (fn [[_ dist]] (>= dist cutoff)) seen)))))

(def ans2 (at-least-a-distance 1000 data-rooms))

;; ## Main

(defn -test [_]
  (test/run-tests))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
