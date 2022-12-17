;; # 🎄 Advent of Code 2022 - Day 16 - Proboscidea Volcanium
(ns p16
  (:require [clojure.string :as str]
            [clojure.test :as test]
            [clojure.pprint :as pp]
            [clojure.set :as set]
            [util :as util]
            [clojure.data.priority-map :refer [priority-map]]))

;; ## Data Processing

(def data-string (slurp "../input/16.txt"))

(def test-string "Valve AA has flow rate=0; tunnels lead to valves DD, II, BB
Valve BB has flow rate=13; tunnels lead to valves CC, AA
Valve CC has flow rate=2; tunnels lead to valves DD, BB
Valve DD has flow rate=20; tunnels lead to valves CC, AA, EE
Valve EE has flow rate=3; tunnels lead to valves FF, DD
Valve FF has flow rate=0; tunnels lead to valves EE, GG
Valve GG has flow rate=0; tunnels lead to valves FF, HH
Valve HH has flow rate=22; tunnel leads to valve GG
Valve II has flow rate=0; tunnels lead to valves AA, JJ
Valve JJ has flow rate=21; tunnel leads to valve II")

(defn set->map
  ([s] (set->map s 1))
  ([s default]
   (into {} (map (juxt identity (constantly default))) s)))

(defn parse [s]
  (let [data (into {}
                   (for [line (str/split-lines s)]
                     (let [[_ valve flow connections] (re-matches #"Valve (\w\w) has flow rate=(\d+); tunnels? leads? to valves? ([A-Z, ]+)" line)]
                       [(keyword valve)
                        {:flow (parse-long flow)
                         :connections (into #{} (map keyword (read-string (str "[" connections "]"))))}])))
        flows (update-vals data :flow)]
    {:connections (update-vals data :connections)
     :flows flows
     :zero-flows (reduce (fn [s [k v]] (if (zero? v) (conj s k) s)) #{} flows)}))

(def raw-data (parse data-string))
(def raw-test-data (parse test-string))

;; ### Compression
;;
;; Let's try to compress the map by removing all of the zero nodes.

(defn replace-connections
  "Given connections at site `who`, and some `downstream` connections at site `key`, update."
  [connections who key downstream]
  (if (connections key)
    (merge-with min
                (dissoc connections key)
                (dissoc (update-vals downstream (fn [x] (+ x (connections key)))) who))
    connections))

(defn remove-node [connections loc]
  (reduce-kv
   (fn [m k v]
     (assoc m k (replace-connections v k loc (connections loc))))
   {}
   connections))

(defn compress [data]
  (let [{:keys [connections zero-flows]} data
        connections (update-vals connections set->map)]
    (-> data
        (assoc :connections
               (loop [connections connections
                      queue zero-flows]
                 (if-let [goner (first queue)]
                   (recur
                    (remove-node connections goner)
                    (rest queue))
                   (reduce dissoc connections (disj zero-flows :AA))))))))

(def compressed-data (compress raw-data))
(def compressed-test-data (compress raw-test-data))

;; ## Fully-connected
;; Let's further simplify the input by letting every node connect to every other node

(defn join-connections
  "Given connections at site `who`, and some `downstream` connections at site `key`, update."
  [connections who key downstream]
  (if (connections key)
    (merge-with min
                connections
                (dissoc (update-vals downstream (fn [x] (+ x (connections key)))) who))
    connections))

(defn augment-node [connections loc]
  (reduce-kv
   (fn [m k v]
     (assoc m k (join-connections v k loc (connections loc))))
   {}
   connections))

(defn augment [data]
  (let [{:keys [connections]} data]
    (-> data
        (assoc :connections
               (loop [connections connections
                      queue (keys connections)]
                 (if-let [goner (first queue)]
                   (recur
                    (augment-node connections goner)
                    (rest queue))
                   connections))))))

(def data (augment compressed-data))
(def test-data (augment compressed-test-data))

(reduce-kv
 (fn [m k v] (if (pos? v) (assoc m k v) m))
 {}
 (:flows data))

;; ## Logic
;; For the core puzzle today, we are tasked with starting in room `AA` and then every minute we can
;; move along a tunnel or we can open a valve.  The goal is to figure out
;; a strategy that will allow the greatest release in pressure.  Once a valve is opened
;; it stays open and we can continue.
;;
;; To track our state, we'll need to know where we are, the time, which valves are open and
;; how much pressure we have released so far.

(def init {:loc :AA :open #{} :released 0 :time 30})

;; Let's start by working out the neighboring states.

(defn filter-keys [m pred]
  (reduce-kv (fn [m k v] (if (pred k) (assoc m k v) m)) {} m))

(defn neighbors [data state]
  (let [{:keys [loc open time]} state
        {:keys [connections flows]} data
        connections (connections loc)
        neighs (filter (fn [s] (>= (:time s) 0))
                       (concat
                        ;; We could move to any of the neighboring locations
                        (for [[neigh steps] (filter-keys connections (complement open))]
                          (-> state
                              (assoc :loc neigh)
                              (update :time - steps 1)
                              (update :released + (* (- time steps 1) (flows neigh)))
                              (update :open conj neigh)))))]
    (if (empty? neighs)
      (list (assoc state :time 0))
      neighs)))

(defn heuristic [data state]
  (let [{:keys [released]} state]
    (transduce
     (map (fn [x] (- (:released x) released)))
     +
     0
     (neighbors data state))))

(defn solve [data state neighbors heuristic goal?]
  (let [neighbors (partial neighbors data)
        heuristic (comp - (partial heuristic data))
        cost (fn [start] (fn [end] (- (:released start) (:released end))))]
    (:released
     (last
      (util/a-star
       state
       goal?
       cost
       neighbors
       heuristic)))))

(defn goal? [state]
  (= 0 (:time state)))

(defn part-1 [data]
  (solve data init neighbors heuristic goal?))

(test/deftest test-part-1
  (test/is (= 1651 (part-1 test-data))))

(def ans1 (time (part-1 data)))

;; ## Part 2
;; Now in part 2 we have an elephant helping us out.  We need to modify our neighbor function and try again.

(def init-2 (-> init
                (assoc :loc [:AA :AA])
                (assoc :time [26 26])))

(defn distinct-by [f coll]
  (let [groups (group-by f coll)]
    (map #(first (groups %)) (distinct (map f coll)))))

(defn neighbors-2 [data state]
  (let [{[loc1 loc2] :loc [time1 time2] :time o-released :released} state]
    (distinct-by (fn [x] (set (zipmap (:loc x) (:time x))))
                 (for [neigh (neighbors data (-> state (assoc :loc loc1) (assoc :time time1)))
                       neigh2 (remove (fn [x] (= (:loc x) (:loc neigh))) (neighbors data (-> state (assoc :loc loc2) (assoc :time time2))))]
                   (let [{loc :loc time :time} neigh
                         {loc2 :loc time2 :time open2 :open released2 :released} neigh2]
                     (-> neigh
                         (assoc :loc [loc loc2])
                         (assoc :time [time time2])
                         (update :open into open2)
                         (update :released + released2 (- o-released))))))))

;; We also need a new heuristic that somehow combines scores from both the player and the elephant.

(defn heuristic-2 [data state]
  (let [{[loc1 loc2] :loc [time1 time2] :time o-released :released} state]
    (->> (concat (neighbors data (-> state (assoc :loc loc1) (assoc :time time1)))
                 (neighbors data (-> state (assoc :loc loc2) (assoc :time time2))))
         (group-by :loc)
         (map (fn [[k v]] (transduce (map (fn [x] (- (:released x) o-released))) max 0 v)))
         (reduce +))))

(defn goal?-2 [state]
  (= [0 0] (:time state)))

(defn part-2 [data]
  (solve data init-2 neighbors-2 heuristic-2 goal?-2))

(test/deftest test-part-2
  (test/is (= 1707 (part-2 test-data))))

(def ans2 (time (part-2 data)))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p16))

(defn -main [& _]
  (println "Answer1: " ans1)
  (println "Answer2: " ans2))
