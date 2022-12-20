;; # 🎄 Advent of Code 2022 - Day 19 - Not Enough Minerals
;; Today's puzzle reminds me of [cookie clicker](https://orteil.dashnet.org/cookieclicker/).  Looks like
;; we have to optimize our yield given a small menu of options.
(ns p19
  (:require [clojure.string :as str]
            [clojure.test :as test]))

(def data-string (slurp "../input/19.txt"))

(def test-string "Blueprint 1: Each ore robot costs 4 ore. Each clay robot costs 2 ore. Each obsidian robot costs 3 ore and 14 clay. Each geode robot costs 2 ore and 7 obsidian.
Blueprint 2: Each ore robot costs 2 ore. Each clay robot costs 3 ore. Each obsidian robot costs 3 ore and 8 clay. Each geode robot costs 3 ore and 12 obsidian.")

(defn str->ints [s]
  (map parse-long (re-seq #"\d+" s)))

(defn parse [s]
  (into {}
        (for [blueprint (str/split-lines s)]
          (let [[id ore-robot-cost-ore clay-robot-cost-ore obsidian-robot-cost-ore obsidian-robot-cost-clay geode-robot-cost-ore geode-robot-cost-obsidian] (str->ints blueprint)]
            [id
             {:ore {:ore ore-robot-cost-ore}
              :clay {:ore clay-robot-cost-ore}
              :obsidian {:ore obsidian-robot-cost-ore :clay obsidian-robot-cost-clay}
              :geode {:ore geode-robot-cost-ore :obsidian geode-robot-cost-obsidian}}]))))

(def data (parse data-string))
(def test-data (parse test-string))

;; ## Logic

(def init
  {:time 24
   :stuff {:ore 0 :clay 0 :obsidian 0 :geode 0}
   :robot {:ore 1 :clay 0 :obsidian 0 :geode 0}})

(def non-neg? (complement neg?))

(defn pay
  "Remove the cost of a robot from the bank, or nil"
  [bank cost]
  (let [balance (merge-with - bank cost)]
    (when (every? non-neg? (vals balance))
      balance)))

(defn eliminate-excess [plan state]
  (let [{:keys [robot stuff time]} state
        most-needed (apply merge-with max (vals plan))]
    (into {:geode (stuff :geode)}
          (for [element #{:ore :clay :obsidian}
                :let [has (stuff element)
                      needs (most-needed element)
                      bots (robot element)]]
            [element (min has
                          (+ needs (* time (- needs bots))))]
            #_(if (= (robot element) (most-needed element))
                [element (min (stuff element) (most-needed element))]
                [element (stuff element)])))))

;; Doing the naive thing and considering all possibilities already seems to be too complicated to handle.
;;
;; In order to cut down the search space, there are a couple things to keep in mind, first you should not consider
;; making more robots than the max you could spend of any item, and second we can throw away any money we'll never
;; be able to spend.

(defn fast-forward [state type requirements]
  (loop [state state]
    (let [new-state (-> state
                        (update :time dec)
                        (assoc :stuff (merge-with + (:stuff state) (:robot state))))]
      (if-let [stuff (pay (:stuff state) requirements)]
        (-> state
            (update :time dec)
            (assoc :stuff (merge-with + stuff (:robot state)))
            (update-in [:robot type] inc))
        (if (non-neg? (:time new-state))
          (recur new-state)
          state)))))

(defn targetted-neighbors
  "Target a specific build and run forward until that is built."
  [plan state]
  (let [most-needed (assoc (apply merge-with max (vals plan)) :geode ##Inf)]
    (into #{}
          (comp
           (filter some?)
           (filter (comp non-neg? :time))
           (map (fn [state] (assoc state :stuff (eliminate-excess plan state)))))
          (for [type [:ore :clay :obsidian :geode]
                :when (< ((:robot state) type) (most-needed type))]
            (fast-forward state type (plan type))))))

(defn most-geodes
  "Figure out the most geodes we can create from a set of plans."
  [state plan]
  (let [neighbors (partial targetted-neighbors plan)]
    (loop [frontier [state]
           most-seen 0
           seen #{state}]
      ;(println "most-seen=" most-seen " frontier=" (count frontier))
      (if-let [x (peek frontier)]
        (let [neighs (remove seen (neighbors x))]
          (recur (into (pop frontier) neighs)
                 (transduce (map (comp :geode :stuff)) max most-seen neighs)
                 (into seen neighs)))
        most-seen))))

;; ## Part 1

(defn part-1 [data]
  (reduce
   +
   0
   (pmap
    (fn [[id plan]]
      (let [geodes (time (most-geodes init plan))]
        (println "Geodes for plan " id " = " geodes)
        (* id geodes)))
    data)))

(test/deftest test-part-1
  (test/is (= 33 (part-1 test-data))))

(def ans1 (part-1 data))

;; ## Part 2

(defn part-2 [data]
  (let [data (select-keys data [1 2 3])]
    (reduce
     *
     1
     (pmap
      (fn [[id plan]]
        (let [geodes (time (most-geodes (assoc init :time 32) plan))]
          (println "Geodes for plan " id " = " geodes)
          geodes))
      data))))

(def ans2 (part-2 data))

(test/deftest test-part-2
  (test/is (= (* 56 62) (part-2 test-data))))

;; ## Main
(defn -test [& _]
  (test/run-tests 'p19))

(defn -main [& _]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
