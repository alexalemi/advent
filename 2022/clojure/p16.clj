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

(defn parse [s]
  (into {}
        (for [line (str/split-lines s)]
          (let [[_ valve flow connections] (re-matches #"Valve (\w\w) has flow rate=(\d+); tunnels? leads? to valves? ([A-Z, ]+)" line)]
            [(keyword valve)
             {:flow (parse-long flow)
              :connections (into #{} (map keyword (read-string (str "[" connections "]"))))}]))))

(def data (parse data-string))
(def test-data (parse test-string))

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

(defn neighbors [data state]
  (let [state (update state :time dec)
        {:keys [loc open time]} state
        info (data loc)]
    (when (>= time 0)
      (concat
         ;; We could move to any of the neighboring locations
       (for [neigh (:connections info)]
         (assoc state :loc neigh))
         ;; And if our own valve isn't open, we could open it
       (when (and (not (open loc)) (pos? (:flow info)))
         [(-> state
              (update :open conj loc)
              (update :released + (* time (:flow info))))])))))

;; Having worked out the neighbors from every state, at this point, naively we would
;; simply exhaust all possibilities, however even on the test problem
;; this doesn't run in any reasonable amount of time.
;;
;; We need to cut down the search space.  Let's reason things out a bit.  We need
;; some kind of rules to exclude considerating states that we know are going to lose.
;; One thing I think we know is that there isn't any reason to consider
;; a state at the same location with the same sets of open valves if they have the same
;; `:released` and simply less `:time`.  Similarly if they have the same `:time` but
;; simply has less `:released` its not worth considering.  Similarly, if we have the
;; same `:time` and `:released` but have more `:open` valves, that is also just worse and
;; not worth considering.
;;
;; Overall this seems to be a problem where I'm not sure we have a total ordering on the states
;; but we certainly have some kind of partial ordering or pareto frontier.  There are
;; states or sets of states that dominate over other states.  We should try to exclude the ones
;; that are dominated over.
;;
;; Let's see if we can limp into this by maintaining a set of all previously considered states
;; and then see if we can filter out those states that aren't worth considering.

#_(defn dominates?
    "Say whether state y dominates over state x."
    [{x-loc :loc x-open :open x-released :released x-time :time :as x}
     {y-loc :loc y-open :open y-released :released y-time :time :as y}]
    (and (>= y-time x-time) (>= y-released x-released) (set/subset? y-open x-open)))

#_(defn dominations
    "This function tries to define a notion of domination
  amongst the positions seen so far."
    [data seen]
    (fn find-dominator [state]
      (let [loc (:loc state)]
        (some (partial dominates? state) (seen loc)))))

#_(defn most-pressure
    ([data state] (most-pressure data state ##Inf))
    ([data state max-iter]
     (let [neighbors (partial neighbors data)]
       (loop [frontier [state] ;(list state)
              seen {(:loc state) #{state}}
              best-seen 0
              counter 0]
        ;(println "frontier=" (count frontier) " seen=" (transduce (map count) + 0  (vals seen)) " best-seen=" best-seen " counter=" counter)
         (if (< counter max-iter)
           (if-let [state (peek frontier)]
             (let [find-dominator (dominations data seen)
                   neighs (remove find-dominator (neighbors state))]
              ;(println "frontier=" frontier " seen=" seen " best-seen=" best-seen " counter=" counter " neighs=" neighs)
               (recur
                (into (pop frontier) neighs)
                (reduce (fn [m neigh] (update m (:loc neigh) conj neigh)) seen neighs)
                (if (> (:released state) best-seen)
                  (do
                    (println "new best =" (:released state))
                    (:released state))
                  best-seen)
               ;(max best-seen (:released state)))
                (inc counter)))
             [best-seen counter])
           [best-seen counter])))))

;; So far this approach hasn't really yielded fruit for me.  It doesn't seem to be reducing the search space fast
;; enough to let me solve the real puzzle.
;;
;; At this point to make further progress, I think I need to try to do each clock cycle all at once and
;; only keep the dominating instances. I'm not sure if I could manage to keep only a single trial at each
;; location for each time stamp but it would be nice.
;;
;; So, for each location we'll consider only the best state possible for that step at that location.

#_(defn most-pressure
    ([data] (most-pressure data init
                           (fn [state] [(:loc state) (:open state)])
                           (partial neighbors data)))
    ([data init keyfunc neighbors]
     (let [frontier {(keyfunc init) init}
           time (:time init)]
       (letfn [(state-max [x y] (if (> (:released x) (:released y)) x y))
               (expand [frontier]
                 (println (:time (first (vals frontier))))
                 (apply merge-with state-max
                        (for [state (vals frontier)]
                          (reduce
                           (fn [m s] (assoc m (keyfunc s) s))
                           {}
                           (neighbors state)))))]
         (let [frontier (nth (iterate expand frontier) (:time init))]
           (:released (reduce state-max (vals frontier))))))))

(defn state-max [x y]
  (if (> (:released x) (:released y)) x y))

(defn heuristic [data]
  (let [flows (update-vals data :flow)
        locs (into #{} (keys data))]
    (fn [{:keys [time released open]}]
      (+ released (* time (reduce + (vals (select-keys flows (set/difference locs open)))))))))

(defn simple-most-pressure
  ([data] (simple-most-pressure data init
                                (fn [state] [(:loc state) (:open state)])
                                neighbors))
  ([data init keyfunc neighbors]
   (let [neighbors (partial neighbors data)]
     (loop [frontier {(keyfunc init) init}
            time (:time init)]
       (println "time = " time)
       (if (> time 0)
         (recur
          (apply merge-with state-max
                 (for [state (vals frontier)]
                   (reduce
                    (fn [m s] (assoc m (keyfunc s) s))
                    {}
                    (neighbors state))))
          (dec time))
         (:released (reduce state-max (vals frontier))))))))

(defn most-pressure
  ([data] (most-pressure data init
                         (fn [state] [(:loc state) (:open state)])
                         neighbors))
  ([data init keyfunc neighbors]
   (let [neighbors (partial neighbors data)
         heuristic (heuristic data)] ; (fn [state] (:released state))] ;(heuristic data)]
     (loop [frontier {(keyfunc init) init}
            best-seen {(first (keyfunc init)) (:released init)}
            time (:time init)]
       (println "time = " time)
       (if (> time 0)
         (let [new-frontier
               (apply merge-with state-max
                      (for [state (vals frontier)]
                        (reduce
                         (fn [m s] (assoc m (keyfunc s) s))
                         {}
                         ;(neighbors state)
                         (filter
                          (fn [s] (>= (heuristic s) (get best-seen (first (keyfunc s)) 0)))
                          (neighbors state)))))]
           (recur
            new-frontier
            (reduce
             (fn [m [_ s]] (update m (first (keyfunc s)) (fnil max 0) (:released s)))
             best-seen
             new-frontier)
            (dec time)))
         (:released (reduce state-max (vals frontier))))))))

(test/deftest test-part-1
  (test/is (= 1651 (most-pressure test-data))))

(comment
  (most-pressure test-data)

  (most-pressure data)

  (def ans1 (time (most-pressure data))))
;; ans1 = 1792

;; ## Part 2
;;
;; Now in part two, we have an elephant with us as well.

(def init-2 (-> init
                (assoc :time 26)
                (assoc :elephant :AA)))

(defn swap-loc-elephant [state]
  (-> state
      (assoc :elephant (:loc state))
      (assoc :loc (:elephant state))))

(defn neighbors-2 [data state]
  (let [neighbors (partial neighbors data)]
    (apply concat (for [neigh (neighbors (swap-loc-elephant state))]
                    (neighbors (update (swap-loc-elephant neigh) :time inc))))))

(defn part-2 [data]
  (most-pressure data init-2
                 (fn [state] [(set [(:loc state) (:elephant state)]) (:open state)])
                 neighbors-2))

(test/deftest test-part-2
  (test/is (= 1707 (part-2 test-data))))

(comment
  (time (part-2 test-data)))

(comment
  (def ans2 (time (part-2 data))))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p16))

(defn -main [& _])
  ;(println "Answer1: " ans1))
  ;(println "Answer2: " ans2))

#_(defn heuristic [data]
    (let [flows (update-vals data :flow)
          locs (into #{} (keys data))]
      (fn [{:keys [time released open]}]
        (+ released (* time (reduce + (vals (select-keys flows (set/difference locs open)))))))))

#_(comment
    (let [data test-data
          neighbors (partial neighbors data)
          frontier {[(:loc init) (:open init)] init}]
      (letfn [(state-max [x y] (if (> (:released x) (:released y)) x y))
              (expand [frontier]
                (apply merge-with state-max
                       (for [state (vals frontier)]
                         (reduce
                          (fn [m k] (assoc m [(:loc k) (:open k)] k))
                          {}
                          (neighbors state)))))]
        (let [frontier (nth (iterate expand frontier) 30)]
             ;frontier (expand frontier)
             ;frontier (expand frontier)]
         ;(println "-------")
         ;(pp/pprint frontier)
          (reduce state-max (vals frontier))
         ;(apply merge-with h-max
         ;(println "========")
          #_(pp/pprint (for [state (vals frontier)]
                         [(:loc state)
                          (reduce
                           (fn [m k] (assoc m (:loc k) k))
                           {}
                           (neighbors state))])))))

    (let [data test-data
          neighbors (partial neighbors data)
         ;heuristic (heuristic data)
         ;heuristic (fn [state] (:released state))
          frontier {(:loc init) [init (heuristic init)]}]
      (letfn [(h-max [[s1 h1] [s2 h2]] (if (> h2 h1) [s2 h2] [s1 h1]))
              (g-max [x y] (println "x=" x " y=" y) y)
              (expand [frontier]
                (apply merge-with h-max
                       (for [[state _] (vals frontier)]
                         (reduce
                          (fn [m k] (assoc m (:loc k) [k (heuristic k)]))
                          {}
                          (neighbors state)))))]
        (let [frontier (nth (iterate expand frontier) 30)]
             ;frontier (expand frontier)
             ;frontier (expand frontier)]
          (println "-------")
          (pp/pprint frontier)
          (reduce h-max (vals frontier))
         ;(apply merge-with h-max
         ;(println "========")
          #_(pp/pprint (for [[state _] (vals frontier)]
                         [(:loc state)
                          (reduce
                           (fn [m k] (assoc m (:loc k) [k (heuristic k)]))
                           {}
                           (neighbors state))])))))

    (let [data test-data
          neighbors (partial neighbors data)
          heuristic (heuristic data)
          frontier {(:loc init) (priority-map init (heuristic init))}]
      (letfn [(expand [frontier]
                (apply merge-with into
                       (for [frontier-at-loc (vals frontier)]
                         (apply merge-with into
                                (for [[state _] frontier-at-loc]
                                  (reduce
                                   (fn [m k] (update m (:loc k) (fnil assoc (priority-map)) k (heuristic k)))
                                   {}
                                   (neighbors state)))))))]
        (let [frontier (expand frontier)
              frontier (expand frontier)
              frontier (expand frontier)]
          (println "-----------")
          (pp/pprint (expand frontier))))))

#_(comment
    (println "----------------------")

    (let [[best frontier] (most-pressure test-data init 428)]
      [best (peek frontier)])

    (time (most-pressure test-data init))

    (time (most-pressure data init))

    (time
     (let [data test-data]
       (let [neighbors (partial neighbors data)]
         (letfn [(goal? [state] (= 0 (:time state)))
                 (heuristic [state]
                   (* -1 (:time state) (reduce + (vals (select-keys (update-vals data :flow) (set/difference (into #{} (keys data)) (:open state)))))))
                 (cost [current]
                   (fn [neigh] (- (:released current) (:released neigh))))]
           (last
            (util/a-star
             init
             goal?
             cost
             neighbors
             heuristic))))))

    #_(let [data test-data
            state init
            neighbors (partial neighbors data)]
        (let [frontier [state]
              seen {(:loc state) #{state}}
              best-seen 0
              counter 0]
          (when (< counter 5)
            (if-let [state (peek frontier)]
              (let [find-dominator (dominations data seen)
                    neighs (remove find-dominator (neighbors state))]
                neighs))))))
