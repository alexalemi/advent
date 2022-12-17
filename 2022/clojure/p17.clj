;; # 🎄 Advent of Code 2022 - Day 17 - Pyroclastic Flow
;;  For today's puzzle, we are playing tetris.
(ns p17
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [clojure.test :as test])
  (:import [java.awt.image BufferedImage]))

;; ## Data Processing

(def data-string (str/trim (slurp "../input/17.txt")))

(def test-string ">>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>")

(def rocks
  [#{[2 4] [3 4] [4 4] [5 4]}
   #{[2 5] [3 4] [3 5] [3 6] [4 5]}
   #{[2 4] [3 4] [4 4] [4 5] [4 6]}
   #{[2 4] [2 5] [2 6] [2 7]}
   #{[2 4] [3 4] [2 5] [3 5]}])

(def WIDTH 7)

;; We'll represent the state of our world
;; with the following little object.  Our `rock` is a set
;; of coordinates that we will manipulate, our `fixed` are all
;; of the squares that have already frozen in place.
;; We'll keep track of the set of all `rocks` that we will
;; cycle through, indexed by `rock-num`, as well as the
;; `move-str` of all of the moves we'll cycle through, our
;; current location being `move-num`.  `time` will denote
;; how many blocks have fallen, and `top` is the heighest
;; `y` coordinate in `fixed.`

(def state
  {:fixed (into #{} (for [x (range WIDTH)] [x 0]))
   :rock #{}
   :rocks rocks
   :rock-num 0
   :top 0
   :move-str data-string
   :move-num 0
   :time 0})

(def test-state
  (assoc state :move-str test-string))

;; ## Visualization
;; We'll build a nice image renderer so that we can see the game board.

(defn x-bounds [vals]
  (reduce
   (fn [[xlo xhi] [x _]]
     [(min xlo x) (max xhi x)])
   [##Inf ##-Inf]
   vals))

(defn y-bounds [vals]
  (reduce
   (fn [[ylo yhi] [_ y]]
     [(min ylo y) (max yhi y)])
   [##Inf ##-Inf]
   vals))
(defn bounds [state]
  (let [{:keys [rock fixed]} state
        all (into fixed rock)]
    [(x-bounds all) (y-bounds all)]))

(defn render-image
  "Render the puzzle as a raw image."
  ([Ω] (render-image Ω (bounds Ω) 1))
  ([Ω zoom] (render-image Ω (bounds Ω) zoom))
  ([Ω bounds zoom]
   (let [{:keys [fixed rock hidden]} Ω
         [[xlo xhi] [ylo yhi]] bounds
         width (* zoom (- (inc (inc xhi)) (dec xlo)))
         height (* zoom (- (inc yhi) ylo))
         img (BufferedImage. width height BufferedImage/TYPE_3BYTE_BGR)]
     (doseq [xp (range width)
             yp (range height)]
       (let [x (+ (quot xp zoom) (dec xlo))
             ;y (- yhi (+ (quot yp zoom) ylo))
             y (+ (quot yp zoom) ylo)
             loc [x y]]
         (.setRGB img xp (- (dec height) yp)
                  (cond
                    (and (= y 0) (nil? hidden)) (.getRGB (java.awt.Color. 142 142 142))
                    (= x -1) (.getRGB (java.awt.Color. 142 142 142))
                    (= x WIDTH) (.getRGB (java.awt.Color. 142 142 142))
                    (rock loc) (.getRGB (java.awt.Color. 194 42 42))
                    (fixed loc) (.getRGB (java.awt.Color. 42 42 42))
                    :else (.getRGB java.awt.Color/WHITE)))))
     img)))

;; ## Logic
;; To start we'll implement the basic components we need to get some blocks moving.

(defn in-bounds? [rocks]
  (let [[xlo xhi] (x-bounds rocks)]
    (<= 0 xlo xhi (dec WIDTH))))

(defn move-right [rock]
  (into #{} (map (fn [[x y]] [(inc x) y])) rock))

(defn move-left [rock]
  (into #{} (map (fn [[x y]] [(dec x) y])) rock))

(defn move-down [rock]
  (into #{} (map (fn [[x y]] [x (dec y)])) rock))

(defn push [move rock]
  (let [new-rock (move rock)]
    (if (in-bounds? new-rock) new-rock rock)))

(def push-left (partial push move-left))
(def push-right (partial push move-right))

(defn offset [rock dy]
  (into #{} (map (fn [[x y]] [x (+ y dy)])) rock))
(defn wrapped-inc [x cap] (mod (inc x) cap))

(defn step
  "Core logic, does a single update to our state."
  [state]
  (let [{:keys [fixed rock rocks rock-num move-num move-str top]} state]
    (if (empty? rock)
      ;; If we don't currently have a rock, load the next one at the appropriate height.
      (-> state
          (assoc :rock (offset (get rocks rock-num) top))
          (update :rock-num wrapped-inc (count rocks)))
      ;; Otherwise handle a move
      (let [move (get move-str move-num)
            new-rock (push (case move \> move-right \< move-left) rock)
            rock (if (empty? (set/intersection new-rock fixed)) new-rock rock)
            down-rock (move-down rock)]
        (if (empty? (set/intersection down-rock fixed))
          ;; Just an ordinary move, no collisions
          (-> state
              (assoc :rock down-rock)
              (update :move-num wrapped-inc (count move-str)))
          ;; We have a landing, freeze the rock, add it to fixed, clear the rock, inc the time
          (-> state
              (assoc :rock #{})
              (update :fixed into rock)
              (update :move-num wrapped-inc (count move-str))
              (update :time inc)
              (assoc :top (max top (second (y-bounds rock))))))))))

;; ## Part 1
;;  We already have what we need in place to solve the first part.

(defn seek
  "Returns the first time from coll for which (pred item) returns true.
   Returns nil if no such item is present or the not-found value if supplied."
  ([pred coll] (seek pred coll nil))
  ([pred coll not-found]
   (reduce (fn [_ x] (if (pred x) (reduced x) not-found)) not-found coll)))

(defn drop-block
  "Drop the next block onto the configuration."
  [state]
  (seek (fn [x] (not= (:time x) (:time state))) (iterate step state)))

(defn run-until [state stop]
  (seek (fn [x] (= (:time x) stop)) (iterate drop-block state)))

(defn part-1 [state]
  (:top (run-until state 2022)))

(test/deftest test-part-1
  (test/is (= 3068 (part-1 test-state))))

(def ans1 (part-1 state))

(let [dropped 10
      state (first (drop-while (fn [x] (< (:time x) dropped)) (iterate step test-state)))
      state (step state)]
      ;state (nth (iterate step test-state) 5)]
  (render-image state 10))

;; ## Part-2
;;  If we have any hope of doing a huge number of steps, we are going to have to prune our fixed set to
;;  only have a single spot at each height.  We only need to keep those blocks that are 'visible' from the
;;  very top, at which point we can sort of shift our whole state down again to be nestled at the origin,
;;  after doing that we'll store a `hidden` parameter that says how much height we've just eaten up.

(defn neighbors [[x y]]
  [[(dec x) y]
   [x (dec y)]
   [x (inc y)]
   [(inc x) y]])

(defn prune [fixed top]
  (let [top (inc top)
        valid? (fn [[x y]] (and (<= 0 x (dec WIDTH)) (<= 0 y top)))]
    (loop [frontier [[0 top]]
           seen #{[0 top]}
           edges #{}]
      (if-let [loc (peek frontier)]
        (let [neighs (into #{} (comp (filter valid?) (remove seen)) (neighbors loc))]
          (recur
           (into (pop frontier) (filter (complement fixed) neighs))
           (into seen neighs)
           (into edges (filter fixed neighs))))
        edges))))

(let [dropped 2022
      state (first (drop-while (fn [x] (< (:time x) dropped)) (iterate step test-state)))
      state (step state)
      new-fixed (prune (:fixed state) (:top state))]
  (render-image (assoc state :fixed new-fixed) 10))

(defn clean-up
  "Prune the boundary after dropping a block"
  [state]
  (let [{:keys [fixed top]} state
        pruned-fixed (prune fixed top)
        bottom (first (y-bounds pruned-fixed))
        new-top (- top bottom)
        new-fixed (into #{} (map (fn [[x y]] [x (- y bottom)])) pruned-fixed)]
    (-> state
        (assoc :fixed new-fixed)
        (update :hidden (fnil + 0) bottom)
        (assoc :top new-top))))

;; By normalizing our states like this, we should be able to easily detect
;; if there is a cycle in the states.

(defn find-repeat
  "Run the game until we find a cycle."
  [state]
  (let [signature (juxt :move-num :rock-num :fixed)]
    (loop [state state
           seen {}]
      (let [new-state (clean-up (drop-block state))
            sig (signature new-state)]
        (if-let [prev (seen sig)]
          ;; we've hit a loop)))))
          (-> new-state
              (assoc :old-time (first prev))
              (assoc :old-hidden (second prev)))
          (recur
           new-state
           (assoc seen sig [(:time new-state) (:hidden new-state)])))))))

;; If we detect a cycle we can do a fast warp into the future to get near to
;; our eventual goal.

(defn warp [state stop]
  (let [{:keys [time hidden old-time old-hidden]} state
        period (- time old-time)
        hidden-inc (- hidden old-hidden)
        periods (quot (- stop time) period)
        new-hidden (+ hidden (* periods hidden-inc))
        new-time (+ time (* periods period))]
    (-> state
        (assoc :time new-time)
        (assoc :hidden new-hidden))))

;; We'll then just run ordinarily until we hit it, in case there are residual steps needed.

(defn part-2 [state stop]
  (let [state state
        state (find-repeat state)
        state (warp state stop)
        state (run-until state stop)]
    (+ (:hidden state) (:top state))))

(test/deftest test-part-1-fast
  (test/is (= 3068 (part-2 test-state 2022))))

(def STOP 1000000000000)

(test/deftest test-part-2
  (test/is (= 1514285714288 (part-2 test-state STOP))))

(def ans2 (part-2 state STOP))

(let [state state
      stop STOP
      state (find-repeat state)
      state (warp state stop)
      state (run-until state stop)
      state (clean-up state)]
  (render-image state 10))

;; ## Main

(defn -test [& _]
  (test/run-tests 'p17))

(defn -main [& _]
  (println "Answer1: " ans1)
  (println "Answer2: " ans2))
