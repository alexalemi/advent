(ns advent23
  (:require
   [clojure.data.priority-map :refer [priority-map]]
   [clojure.set :as set]))

;(def data-string (slurp "../input/23.txt"))
(def data-string "#############
#...........#
###D#A#A#D###
  #C#C#B#B#
  #########")
(def test-string "#############
#...........#
###B#C#B#D###
  #A#D#C#A#
  #########")

(defn enumerate [coll]
  (zipmap (range) coll))

(defn raw-neighbors [loc]
  (let [[x y] loc]
    [[(inc x) y]
     [(dec x) y]
     [x (inc y)]
     [x (dec y)]]))

(def data
  {:board #{[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [6 0] [7 0] [8 0] [9 0] [10 0]}
   :rooms {:A #{[2 1] [2 2]}
           :B #{[4 1] [4 2]}
           :C #{[6 1] [6 2]}
           :D #{[8 1] [8 2]}}
   :door #{[2 0] [4 0] [6 0] [8 0]}
   :energies {:A 1 :B 10 :C 100 :D 1000}})

(def test-state {:A #{[2 2] [8 2]}
                 :B #{[2 1] [6 1]}
                 :C #{[4 1] [6 2]}
                 :D #{[4 2] [8 1]}})

(def state {:A #{[4 1] [6 1]}
            :B #{[6 2] [8 2]}
            :C #{[2 2] [4 2]}
            :D #{[2 1] [8 1]}})

(def state-b {:A #{[4 1] [8 1]}
              :B #{[6 2] [8 2]}
              :C #{[4 2] [6 1]}
              :D #{[2 1] [2 2]}})

(defn filter-keys [pred m]
  (reduce-kv (fn [m k v] (if (pred k) (assoc m k v) m)) {} m))

(defn reachable-moves
  [valid-space loc]
  (loop [frontier [[loc 0]]
         seen #{}
         neighs {}]
    (let [[node cost] (first frontier)]
      (if (empty? frontier) (dissoc neighs loc)
          (recur (into
                  (rest frontier)
                  (zipmap
                   (filter (every-pred (complement seen) valid-space)
                           (raw-neighbors node))
                   (repeat (inc cost))))
                 (conj seen node)
                 (conj neighs [node cost]))))))

(defn map-vals [f m]
  (reduce-kv (fn [m k v] (assoc m k (f v))) {} m))

(defn get-moves
  "Given a state, who is moving, and their current location
  return all of the valid places to move.

  I want to consider moves of multiple length as I'd like to try
  to treat the whole operation as one move."
  [data state who loc]
  (let [energy (get-in data [:energies who])
        unoccupied (set/difference
                    (apply set/union (data :board) (vals (data :rooms)))
                    (reduce set/union (vals state)))
        reachable (map-vals #(* energy %) (reachable-moves unoccupied loc))
        board (data :board)
        my-room (get-in data [:rooms who])
        others-locs (apply set/union (vals (dissoc state who)))
        can-dock (empty? (set/intersection my-room others-locs))]
    (if (contains? board loc) ; means we are in the hallway
       ;we only need to consider our room, provided no other color is in there.
      (if can-dock (filter-keys my-room reachable) ())
      (if (contains? (state :-unmoved) loc)
        (filter-keys (every-pred (complement (data :door)) board) reachable)
        ()))))

(defn neighbors
  "Given a state, return all neighboring, valid states along with their
  incremental cost."
  [data state]
  (into {}
        (for [who (remove #{:-unmoved} (keys state))
              loc (state who)
              [to cost] (get-moves data state who loc)]
          [(-> state
               (update who disj loc)
               (update who conj to)
               (update :-unmoved disj loc))
           cost])))

(comment
  "Overall this thing is a lot like day 15, we're gonna wanna do
  something like Djikstra's algorithm.  For that we need a
  'neighbor' function that gives us all of the possible moves
  at each turn, as well as a way to tell if we've reached
  the goal."

  "To represent the state, I probably minimally just need to know
 the locations of the 8 amphipods. That could be a list of four sets.
 me the overall board, probably a set of locations."

  "The tricky bit is the neighbor function, we'd need to consider each amphipod
and all of the locations they could stop on.  That's the core logic here.")

(defn map-val
  "Map a function over the vals of a map."
  [m f & args]
  (reduce-kv (fn [m k v] (assoc m k (apply f v args))) {} m))

(defn remove-keys
  "Remove some of the keys in a map."
  [pred m]
  (reduce-kv (fn [m k v] (if (pred k) m (assoc m k v))) {} m))

(defn shortest-cost
  "Recursive dijstra's algorithm.

  General form.
    start - any data structure.
    neighbors is a function from a state -> cost for the move.
    goal is a state."
  [neighbors start goal]
  (loop [frontier (priority-map start 0)  ; the nodes we have to expand
         min-cost {start 0}]  ; the nodes we already know the optimal cost of.
    (let [[current cost] (peek frontier)
         ; for all of the neighbors of the current node, see if we've just
         ; discovered a shorter path.
          dists (map-val (remove-keys min-cost (neighbors current)) + cost)]
      (if (= (dissoc current :-unmoved) goal) cost
          (recur (merge-with min (pop frontier) dists)
                 (assoc min-cost current cost))))))


(defn part-1 [state]
  (let [initial-pieces (reduce set/union (vals state))]
    (shortest-cost (partial neighbors data)
                   (assoc state :-unmoved initial-pieces)
                   (data :rooms))))

(time (def ans-1 (part-1 state)))
(println)
(println "Answer 1:" ans-1)

(def data-2
  {:board #{[0 0] [1 0] [2 0] [3 0] [4 0] [5 0] [6 0] [7 0] [8 0] [9 0] [10 0]}
   :rooms {:A #{[2 1] [2 2] [2 3] [2 4]}
           :B #{[4 1] [4 2] [4 3] [4 4]}
           :C #{[6 1] [6 2] [6 3] [6 4]}
           :D #{[8 1] [8 2] [8 3] [8 4]}}
   :door #{[2 0] [4 0] [6 0] [8 0]}
   :energies {:A 1 :B 10 :C 100 :D 1000}})

(def state-2 {:A #{[4 1] [6 1] [8 2] [6 3]}
              :B #{[6 4] [8 4] [6 2] [4 3]}
              :C #{[2 4] [4 4] [4 2] [8 3]}
              :D #{[2 1] [8 1] [2 2] [2 3]}})

(def state-2-b {:A #{[4 1] [8 1] [8 2] [6 3]}
                :B #{[6 4] [8 4] [6 2] [4 3]}
                :C #{[4 4] [6 1] [4 2] [8 3]}
                :D #{[2 1] [2 4] [2 2] [2 3]}})

(defn part-2 [state]
  (let [initial-pieces (reduce set/union (vals state))]
    (shortest-cost (partial neighbors data-2)
                   (assoc state :-unmoved initial-pieces)
                   (data-2 :rooms))))

(time (def ans-2 (part-2 state-2)))
(println)
(println "Answer 2:" ans-2)

