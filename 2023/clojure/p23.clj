;; # 🎄 Advent of Code 2023 - Day 22 - Sand Slabs
(ns p23
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/23.txt"))
(def test-string "#.#####################
#.......#########...###
#######.#########.#.###
###.....#.>.>.###.#.###
###v#####.#v#.###.#.###
###.>...#.#.#.....#...#
###v###.#.#.#########.#
###...#.#.#.......#...#
#####.#.#.#######.#.###
#.....#.#.#.......#...#
#.#####.#.#.#########v#
#.#...#...#...###...>.#
#.#.#v#######v###.###v#
#...#.>.#...>.>.#.###.#
#####v#.#.###v#.#.###.#
#.....#...#...#.#.#...#
#.#########.###.#.#.###
#...###...#...#...#.###
###.###.#.###v#####v###
#...#...#.#.>.>.#.>.###
#.###.###.#.###.#.#v###
#.....###...###...#...#
#####################.#")

(defn enumerate [col]
  (map list (range) col))


(defn extent [col]
  [(transduce (map first) max 0 col)
   (transduce (map second) max 0 col)])

(defn ->data [s]
  (loop [s s
         forests #{}
         slides {}
         line 0
         col 0]
    (if-let [c (first s)]
      (case c
        \# (recur (rest s)
                  (conj forests [line col])
                  slides
                  line
                  (inc col))
        \. (recur (rest s)
                  forests
                  slides
                  line
                  (inc col))
        \newline (recur (rest s)
                        forests
                        slides
                        (inc line)
                        0)
        (\> \< \^ \v) (recur (rest s)
                             forests
                             (assoc slides [line col] c)
                             line
                             (inc col)))
      (let [[Y X] (extent forests)]
        {:forests forests :slides slides :extent [Y X]
         :start (first (remove forests (for [x (range X)] [0 x])))
         :end (first (remove forests (for [x (range X)] [Y x])))}))))

(def data (->data data-string))
(def test-data (->data test-string))

(defn down [[y x]] [(inc y) x])
(defn up [[y x]] [(dec y) x])
(defn right [[y x]] [y (inc x)])
(defn left [[y x]] [y (dec x)])
(def slide->move {\> right \< left \v down \^ up})
(defn raw-neighbors [loc] [(down loc) (up loc) (right loc) (left loc)])

(defn all-path-lengths [data]
  (let [{:keys [start end forests slides]} data]
    (loop [paths [[(down start) 1 #{start}]]
           lengths []]
      (if-let [[loc length seen] (peek paths)]
        (cond
          ;; reached the end
          (= loc end) (recur (pop paths) (conj lengths length))
          ;; at a slide
          (slides loc)
          (let [slid ((slide->move (slides loc)) loc)]
            (if (not (seen slid))
              (recur (conj (pop paths) [slid (inc length) (conj seen loc)]) lengths)
              (recur (pop paths) lengths)))
          :else
          (let [neighs (eduction (comp (remove forests) (remove seen)) (raw-neighbors loc))]
            (recur
              (into (pop paths)
                 (for [neigh neighs]
                    [neigh (inc length) (conj seen loc)]))
              lengths)))
        ;; otherwise return lengths
        lengths))))

(defn part-1 [data]
  (apply max (all-path-lengths data)))

(assert (= 94 (part-1 test-data)))
(defonce ans1 (part-1 data))
(assert (= ans1 2114))

;; # Part 2

(defn compress [data]
  (let [{:keys [start end forests]} data]
    (loop [paths [[(down start) 1 start start]]
           graph {}]
      (if-let [[loc length prev origin] (peek paths)]
        (if
          ;; reached the end
          (= loc end)
          (recur (pop paths)
                 (-> graph
                     (assoc-in [loc origin] length)
                     (assoc-in [origin loc] length)))
          ;; not an end
          (let [neighs (into [] (comp (remove forests) (remove #(= % prev)) (remove #(= % start))) (raw-neighbors loc))]
            (if
                (= (count neighs) 1)
                (recur (conj (pop paths) [(first neighs) (inc length) loc origin]) graph)
                (if ((get graph origin {}) loc)
                  ;; don't go back this way
                  (recur (pop paths) graph)
                  ;; otherwise add to graph and extend
                  (recur
                    (into paths (for [neigh neighs :when (not ((get graph neigh {}) origin))] [neigh 1 loc loc]))
                    (-> graph
                        (assoc-in [loc origin] length)
                        (assoc-in [origin loc] length)))))))
        ;; otherwise return lengths
        graph))))


(defn all-path-lengths-compressed [data]
  (let [{:keys [start end]} data
        compressed-data (compress data)]
    (loop [paths [[start 0 #{start}]]
           lengths []]
      (if-let [[loc length seen] (peek paths)]
        (if
          ;; reached the end
          (= loc end) (recur (pop paths) (conj lengths length))
          (let [neighs-and-costs (eduction (remove (comp seen first)) (compressed-data loc))]
            (recur
              (into (pop paths)
                 (for [[neigh cost] neighs-and-costs]
                    [neigh (+ length cost) (conj seen loc)]))
              lengths)))
        ;; otherwise return lengths
        lengths))))

(defn part-2 [data]
  (apply max (all-path-lengths-compressed data)))

(assert (= 154 (part-2 test-data)))
(defonce ans2 (part-2 data))
(assert (= ans2 6322))

(defn -main []
  (println "Answer 1:" ans1)
  (println "Answer 1:" ans2))
