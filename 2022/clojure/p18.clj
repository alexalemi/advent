;; ## 🎄 Advent of Code 2022 - Day 18 - Boiling Boulders
;;  Looks like today we get to do some geometry, we need to figure out
;;  how many surfaces are visible externally given a bunch of
;;  voxel coordinates.
(ns p18
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.test :as test]))

;; ## Data
;; Data processing today is easy, let's turn each source into
;; a set of `[x y z]` coordinates.

(defn parse [s]
  (->> (str/split-lines s)
       (map (fn [x] (read-string (str "[" x "]"))))
       (into #{})))

(def data (parse (slurp "../input/18.txt")))

(def test-data (parse "2,2,2
1,2,2
3,2,2
2,1,2
2,3,2
2,2,1
2,2,3
2,2,4
2,2,6
1,2,5
3,2,5
2,1,5
2,3,5"))

;; ## Part 1
;; For the first part we are supposed to simply count which sides are not
;; connected to another cube, so let's just do this naively and for every voxel
;; in our list, let's try each of its six neighbors to see if that spot is
;; occupied.

(defn neighbors [[x y z]]
  [[(dec x) y z]
   [(inc x) y z]
   [x (dec y) z]
   [x (inc y) z]
   [x y (dec z)]
   [x y (inc z)]])

(defn unconnected-sides [data]
  (reduce + (for [spot data]
              (count (remove data (neighbors spot))))))

(test/deftest test-part-1
  (test/is (= 64 (unconnected-sides test-data)))
  (test/is (= 10 (unconnected-sides #{[1 1 1] [2 1 1]}))))

(def ans1 (unconnected-sides data))

;; ## Part 2
;; Now for part 2 we need to only consider exterior sides
;; One way I could think to do this is start with a single exterior side and some enclosing box and simply
;; do some kind of depth first search and see which of the exterior sides we can hit.
;;
;; Another option would be to try to start from each of the candidate spots and
;; try to cast a ray outwards and see if we always intersect the surface, for an interior point we would have to always hit
;; the outside, only problem there is there might be some false positives as there could be some kind of
;; cave like structure internally that we wouldn't `see` with this approach.
;;
;; This is pushing me in the direction of the exhaustive walk, let's do that.
;;
;; First we need some kind of bounding volume.

(defn bounds [spots]
  (reduce
   (fn [[[xlo xhi] [ylo yhi] [zlo zhi]] [x y z]]
     [[(min xlo x) (max xhi x)]
      [(min ylo y) (max yhi y)]
      [(min zlo z) (max zhi z)]])
   [[##Inf ##-Inf]
    [##Inf ##-Inf]
    [##Inf ##-Inf]]
   spots))

(defn expand [[[xlo xhi] [ylo yhi] [zlo zhi]]]
  [[(dec xlo) (inc xhi)] [(dec ylo) (inc yhi)] [(dec zlo) (inc zhi)]])

(defn in-bounds [[[xlo xhi] [ylo yhi] [zlo zhi]] [x y z]]
  (and (<= xlo x xhi) (<= ylo y yhi) (<= zlo z zhi)))

(defn find-exterior-sides [data]
  (let [bounds (expand (bounds data))
        [[x0 _] [y0 _] [z0 _]] bounds
        in-bounds? (partial in-bounds bounds)]
    (loop [frontier [[x0 y0 z0]]
           seen #{[x0 y0 z0]}
           sides 0]
      (if-let [loc (peek frontier)]
        (let [neighs (filter in-bounds? (neighbors loc))]
          (recur (into (pop frontier) (remove (some-fn data seen) neighs))
                 (into seen neighs)
                 (+ sides (count (filter data neighs)))))
        sides))))

(test/deftest test-part-2
  (test/is (= 58 (find-exterior-sides test-data))))

(def ans2 (find-exterior-sides data))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p18))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
