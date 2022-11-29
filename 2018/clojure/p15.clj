(ns p15
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]
            [criterium.core :as crit]))

;; # Day 15
;; For this day we essentially have to implement a game.


;; ## Data processing
(def data-string (slurp "../input/15.txt"))

(defn third [x] (nth x 3))

(def ATTACK 3)
(def HP 200)

(defn process [s]
  (let [pixels (into {} (for [[y line] (map-indexed vector (str/split-lines s))
                              [x c] (map-indexed vector line)]
                           [[x y] c]))]
   (-> (group-by pixels (keys pixels))
       (dissoc \.)
       (update-vals #(into #{} %))
       (update-keys {\# :walls \G :goblins \E :elves})
       (update :goblins #(into {} (for [loc %] [loc HP])))
       (update :elves #(into {} (for [loc %] [loc HP])))
       (assoc :round 0))))

(def data (process data-string))


;; Now we've managed to turn the ascii input into a map of sets of the locations
;; that are populated.

;; ## Visualization
;; Let's see if we can build a nice vizualization thing.

(defn maximum [vals] (reduce max vals))

(defn max-size [data]
    (->> (:walls data)
        ((juxt #(maximum (map first %))
            #(maximum (map second %))))))

(defn life [x]
  (cond
    (= x 200) 1.0
    (= x 0) 0.0
    :else
    (+ 0.3 (* 0.3 (/ x 200)))))

(defn render-spot [data [x y]]
  (let [{:keys [walls goblins elves]} data]
    [:div.inline-block
     {:style {:width 16 :height 16
              :font-size "5pt"
              :background-color (cond
                                  (walls [x y]) "#222"
                                  (elves [x y]) (str "rgba(0, 255, 0," (life (elves [x y])) ")")
                                  (goblins [x y]) (str "rgba(255, 0, 0," (life (goblins [x y])) ")"))
              :border "solid 1px black"}}
     (cond
        (elves [x y]) (elves [x y])
        (goblins [x y]) (goblins [x y])
        :else nil)]))

(declare score)

(defn render
  "Custom clerk rendering function for a board."
  [data]
  (letfn [(box [val] [:div.flex.flex-col [:div {:style {:width 16 :height 16 :font-size "0.5em"}} val]])]
   (let [[X Y] (max-size data)]
     (clerk/html
      [:div
       (into [:div.flex.inline-flex
              (into [:div.flex.flex-col (box "") (for [y (range (inc Y))] (box y))])]
           (for [x (range (inc X))]
             (into [:div.flex.flex-col (box x)]
              (for [y (range (inc Y))]
               (render-spot data [x y])))))
       [:div "rounds: " (:round data) (when (:finished data) (str " score:" (score data)))]]))))

(render data)

;; ## Game Logic
;;
;; Now we have to implement the actual game logic, I'm going to do it here as if we already have what we want.


(defn reading-order
  "Comparator for reading-order."
  [[^int x1 ^int y1] [^int x2 ^int y2]]
  (or (< y1 y2)
      (and (= y1 y2) (< x1 x2))))

(defn assemble-turns
  "Assemble all of the turns in reading order from the data."
  [data]
  (sort-by
   first
   reading-order
   (concat
    (map vector (keys (:goblins data)) (repeat :goblins))
    (map vector (keys (:elves data)) (repeat :elves)))))

(defn identify-targets
  "Find all of the valid targets"
  [data kind]
  (case kind
    :goblins (:elves data)
    :elves (:goblins data)))

(defn neighbors
  "Get neighbors in reading-order."
  [[^int x ^int y]]
  [[x (dec y)]
   [(dec x) y]
   [(inc x) y]
   [x (inc y)]])

(def QUEUE clojure.lang.PersistentQueue/EMPTY)

(defn first-move
  "Knowing the parent of each node, find the first step."
  [came-from current start]
  (loop [x current]
    (let [prev (came-from x)]
      (if (and prev (not= prev start))
        (recur prev)
        x))))

(comment
  (let [data data]
    ((juxt (comp keys :goblins) (comp keys :elves)) data)))

(defn best-move
  "Determine the best first move."
  [data loc targets]
  (let [invalid? (into (:walls data) (concat (keys (:goblins data)) (keys (:elves data))))
        valid? (complement invalid?)
        goal? (into #{} (comp (mapcat (comp neighbors first)) (filter valid?)) targets)]
    (if (empty? goal?) loc ;; short-circuit if there are no places to go.
     (loop [frontier (conj QUEUE loc)
            seen (transient #{loc})
            came-from (transient {})
            buffer nil]
       (if-let [x (peek frontier)]
         (if
           ;; If we reached a goal, quit out
           (goal? x) (first-move (persistent! came-from) x loc)
           ;; Otherwise, we need to append to the frontier.
           (let [neighs (filter (every-pred valid? (complement seen)) (neighbors x))]
             (recur
               (pop frontier)
               (reduce conj! seen neighs)
               (reduce (fn [m k] (if (m k) m (assoc! m k x)))
                       came-from neighs)
               (into buffer neighs))))
         ;; Our frontier is empty, check the buffer
         (if (empty? buffer)
           ;; If we can't find a path, don't move
           loc
           (recur
            (into frontier (sort reading-order buffer))
            seen came-from nil)))))))


(defn manhattan [[^int x1 ^int y1] [^int x2 ^int y2]]
  (+ (abs (- x2 x1))
     (abs (- y2 y1))))

(defn adjacent? [a b]
  (= (manhattan a b) 1))

(defn move
  "Enact a move for the specified unit."
  [[data [loc which]]]
  ;; First check to make sure there are targets
  (let [targets (identify-targets data which)]
    (cond
      ;; If no targets found, do nothing and mark as done.
      (empty? targets) [(assoc data :finished true) nil]
      ;; is-adjacent-already? Don't move just attack
      (some #(adjacent? loc %) (keys targets))
      [data [loc which targets]]
        ;; otherwise, move before attacking.
      :else (let [newloc (best-move data loc targets)
                  health (get-in data [which loc])]
               [(-> data
                 (update which dissoc loc)
                 (update which assoc newloc health))
                [newloc which targets]]))))


(defn filter-keys [pred map]
  (reduce-kv (fn [m k v] (if (pred k) (assoc m k v) m)) nil map))

(defn weakest-reading-order
  "Find the weakest target, if tied, go by reading order"
  [[loc0 health0] [loc1 health1]]
  (or (< health0 health1)
      (and (= health0 health1)
           (reading-order loc0 loc1))))

(defn attack
  "Institute an attack for the specified unit."
  [[data [loc which targets]]]
  (if (:finished data) data
   (let [neighs (into #{} (neighbors loc))]
     (if-let [in-range (filter-keys neighs targets)]
        (let [[target health] (first (sort weakest-reading-order in-range))
              kind (if (= which :goblins) :elves :goblins)
              newhealth (- health ATTACK)]
          (if (pos? newhealth)
            (assoc-in data [kind target] newhealth)
            (update data kind dissoc target)))
        data))))

(defn turn
  "Enact a single turn."
  [data [loc which]]
  ;; Check to make sure the person is still alive
  ;; and we aren't finished already
  (if (and (get-in data [which loc])
           (not (:finished data)))
      (attack (move [data [loc which]]))
      data))

(defn round
  "Run a single round."
  ([data] (round data (assemble-turns data)))
  ([data turns]
   (loop [data data turns turns]
     (if-let [t (first turns)]
       (recur (turn data t) (rest turns))
       ;; Don't increment the turn if we've finished.
       (if (:finished data)
          data
          (update data :round inc))))))


(defn complete [data]
  (first (drop-while (complement :finished) (iterate round data))))

(defn score [data]
  (* (:round data)
     (reduce + (concat
                (vals (:elves data))
                (vals (:goblins data))))))



;; ## Some Tests
;; Let's try out the examples in the problem statement
;;
;; ### Movement test 1
(def test-movement (process "#######
#.E...#
#.....#
#...G.#
#######"))

(let [start test-movement
      end (round test-movement)]
  (assert ((:elves end) [3 1]))
  [(render start)
   (render end)])


;; ### Movement test 2
(def test-movement-2 (process "#########
#G..G..G#
#.......#
#.......#
#G..E..G#
#.......#
#.......#
#G..G..G#
#########"))

(map render (take 4 (iterate round test-movement-2)))

;; ### Sample combat 1

(def test-combat (process "#######
#.G...#
#...EG#
#.#.#G#
#..G#E#
#.....#
#######"))

(let [start test-combat
      end (complete start)]
  (assert (= (score end) 27730))
  [(render start)
   (render end)])


;; ### Other test combats

(let [result (complete (process "#######
#G..#E#
#E#E.E#
#G.##.#
#...#E#
#...E.#
#######"))]
 (assert (= (score result) 36334))
 (render result))

(let [result (complete (process "#######
#E..EG#
#.#G.E#
#E.##E#
#G..#.#
#..E#.#
#######"))]
 (assert (= (score result) 39514))
 (render result))

(let [result (complete (process "#######
#E.G#.#
#.#G..#
#G.#.G#
#G..#.#
#...E.#
#######"))]
 (assert (= (score result) 27755))
 (render result))

(let [result (complete (process "#######
#.E...#
#.#..G#
#.###.#
#E#G#G#
#...#G#
#######"))]
 (assert (= (score result) 28944))
 (render result))

(let [result (complete (process "#########
#G......#
#.E.#...#
#..##..G#
#...##..#
#...#...#
#.G...G.#
#.....G.#
#########"))]
 (assert (= (score result) 18740))
 (render result))

(comment
  (crit/quick-bench
    (score (complete (process "#########
#G......#
#.E.#...#
#..##..G#
#...##..#
#...#...#
#.G...G.#
#.....G.#
#########")))))

;; ## Part 1

(def final-state (time (complete data)))
(render final-state)
(def ans1 (score final-state))
(println "Answer1:" ans1)
; 194028 and 192410 are wrong, too high.
; 190604 is too low


(def movie (take-while (complement :finished) (iterate round data)))
(map render movie)

;; Right now I have an issue, If there are multiple adjacent target squares
;; that are the same distance apart, I take the first step which is in reading order
;; to get there. But I need to instead take the first step towards
;; the one that comes first in reading order.
;;
;; I think I fixed that problem, now I have the problem that if there
;; are multiple shortest paths to get to a desired square, I need
;; to take the one that is best in terms of reading-order.

#_(let [data (nth movie 8)
        turns (assemble-turns data)
        [loc which] (first turns)
        targets (identify-targets data which)
        invalid? (into (:walls data) (concat (keys (:goblins data)) (keys (:elves data))))
        valid? (complement invalid?)
        goal? (into #{} (comp (mapcat (comp neighbors first)) (filter valid?)) targets)]
    [(if (empty? goal?) loc ;; short-circuit if there are no places to go.
      (loop [frontier (conj QUEUE loc)
             seen (transient #{loc})
             came-from (transient {})
             buffer nil]
        (if-let [x (peek frontier)]
          (if
            ;; If we reached a goal, quit out
            (goal? x) (first-move (persistent! came-from) x loc)
            ;; Otherwise, we need to append to the frontier.
            (let [neighs (filter (every-pred valid? (complement seen)) (neighbors x))]
              (recur
                (pop frontier)
                (reduce conj! seen neighs)
                (reduce (fn [m k] (if (m k) m (assoc! m k x)))
                        came-from neighs)
                (into buffer neighs))))
          ;; Our frontier is empty, check the buffer
          (if (empty? buffer)
            ;; If we can't find a path, don't move
            loc
            (recur
             (into frontier (sort reading-order buffer))
             seen came-from nil)))))
     (best-move data loc targets)
     (render data)
     (render (round data))])
