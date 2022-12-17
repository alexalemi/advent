
(ns p17
  (:require [clojure.string :as str]
            [clojure.set :as set]
            [nextjournal.clerk :as clerk])
  (:import [java.awt.image BufferedImage]))

(def data-string (str/trim (slurp "../input/17.txt")))

(def test-string ">>><<><>><<<>><>>><<<>>><<<><<<>><>><<>>")

(def rocks
  [#{[2 4] [3 4] [4 4] [5 4]}
   #{[2 5] [3 4] [3 5] [3 6] [4 5]}
   #{[2 4] [3 4] [4 4] [4 5] [4 6]}
   #{[2 4] [2 5] [2 6] [2 7]}
   #{[2 4] [3 4] [2 5] [3 5]}])

(def WIDTH 7)

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

(def state
  {:fixed (into #{} (for [x (range WIDTH)] [x 0]))
   :rock #{}
   :rocks (cycle rocks)
   :top 0
   :moves (cycle data-string)
   :time 0})

(def test-state
  (assoc state :moves (cycle test-string)))

(empty? #{})

(defn step [state]
  (let [{:keys [fixed rock rocks moves time top]} state]
    (if (empty? rock)
      (-> state
          (assoc :rock (offset (first rocks) top))
          (update :rocks rest))
      (let [move (first moves)
            new-rock (push (case move \> move-right \< move-left) rock)
            rock (if (empty? (set/intersection new-rock fixed)) new-rock rock)
            down-rock (move-down rock)]
        (if (empty? (set/intersection down-rock fixed))
          ;; Otherwise just a move
          (-> state
              (assoc :rock down-rock)
              (update :moves rest))
          ;; We have a landing
          (-> state
              (assoc :rock #{})
              (update :fixed into rock)
              (update :moves rest)
              (update :time inc)
              (assoc :top (max top (second (y-bounds rock))))))))))

(defn bounds [state]
  (let [{:keys [rock fixed]} state
        all (into fixed rock)]
    [(x-bounds all) (y-bounds all)]))

(defn render-image
  "Render the puzzle as a raw image."
  ([Ω] (render-image Ω (bounds Ω) 1))
  ([Ω zoom] (render-image Ω (bounds Ω) zoom))
  ([Ω bounds zoom]
   (let [{:keys [fixed rock]} Ω
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
                    (= y 0) (.getRGB (java.awt.Color. 142 142 142))
                    (= x -1) (.getRGB (java.awt.Color. 142 142 142))
                    (= x WIDTH) (.getRGB (java.awt.Color. 142 142 142))
                    (rock loc) (.getRGB (java.awt.Color. 194 42 42))
                    (fixed loc) (.getRGB (java.awt.Color. 42 42 42))
                    :else (.getRGB java.awt.Color/WHITE)))))
     img)))

(defn part-1 [state]
  (:top (first (drop-while (fn [state] (< (:time state) 2022)) (iterate step state)))))

(part-1 test-state)

(def ans1 (part-1 state))
(println "Answer1:" ans1)

(let [final (first (drop-while (fn [state] (< (:time state) 2022)) (iterate step test-state)))]
  (:top final))

(let [dropped 10
      state (first (drop-while (fn [x] (< (:time x) dropped)) (iterate step test-state)))
      state (step state)]
      ;state (nth (iterate step test-state) 5)]
  (render-image state 10))

;; ## Part-2
;;  If we have any hope of doing a huge number of steps, we are going to have to prune our fixed set to
;;  only have a single spot at each height.

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

(defn find-repeat [state move-str stop]
  (let [{:keys [fixed rock time top]} state
        inc-move (fn [x] (mod (inc x) (count move-str)))
        inc-rocks (fn [x] (mod (inc x) (count rocks)))]
    (loop [fixed fixed
           rock rock
           rock-num 0
           move-num 0
           time time
           top top
           hidden 0
           seen {}]
      (if (= time stop) (+ top hidden)
          (if (empty? rock)
            (let [pruned-fixed (prune fixed top)
                  bottom (first (y-bounds pruned-fixed))
                  new-top (- top bottom)
                  new-fixed (into #{} (map (fn [[x y]] [x (- y bottom)])) pruned-fixed)
                  signature [move-num rock-num new-fixed]]
              (if (seen signature)
                ;; we've hit a loop
                {:fixed new-fixed :rock-num rock-num :move-num move-num :top new-top :hidden (+ hidden bottom) :time time
                 :old-time (first (seen signature))
                 :old-hidden (second (seen signature))}
                (recur new-fixed (offset (get rocks rock-num) new-top) (inc-rocks rock-num) move-num time new-top (+ hidden bottom)
                       (assoc seen signature [time (+ hidden bottom)]))))
            (let [move (get move-str move-num)
                  new-rock (push (case move \> move-right \< move-left) rock)
                  rock (if (empty? (set/intersection new-rock fixed)) new-rock rock)
                  down-rock (move-down rock)]
              (if (empty? (set/intersection down-rock fixed))
                ;; Otherwise just a move
                (recur fixed down-rock rock-num (inc-move move-num) time top hidden seen)
                ;; We have a landing
                (do
                  (when (= (mod time 1000) 0) (println "time=" time " score=" (+ top hidden)))
                  (recur (into fixed rock) #{} rock-num (inc-move move-num) (inc time)
                         (max top (second (y-bounds rock)))
                         hidden seen)))))))))

(defn warp [state stop]
  (let [{:keys [fixed rock rock-num move-num time top hidden old-time old-hidden]} state
        period (- time old-time)
        hidden-inc (- hidden old-hidden)
        periods (quot (- stop time) period)
        new-hidden (+ hidden (* periods hidden-inc))
        new-time (+ time (* periods period))]
    (-> state
        (assoc :time new-time)
        (assoc :hidden new-hidden))))

(defn continue [state move-str stop]
  (let [{:keys [fixed rock rock-num move-num time top hidden]} state
        inc-move (fn [x] (mod (inc x) (count move-str)))
        inc-rocks (fn [x] (mod (inc x) (count rocks)))]
    (loop [fixed fixed
           rock rock
           rock-num rock-num
           move-num move-num
           time time
           top top
           hidden hidden]
      (if (= time stop) (+ top hidden)
          (if (empty? rock)
            (let [pruned-fixed (prune fixed top)
                  bottom (first (y-bounds pruned-fixed))
                  new-top (- top bottom)
                  new-fixed (into #{} (map (fn [[x y]] [x (- y bottom)])) pruned-fixed)]
              (recur new-fixed (offset (get rocks rock-num) new-top) (inc-rocks rock-num) move-num time new-top (+ hidden bottom)))
            (let [move (get move-str move-num)
                  new-rock (push (case move \> move-right \< move-left) rock)
                  rock (if (empty? (set/intersection new-rock fixed)) new-rock rock)
                  down-rock (move-down rock)]
              (if (empty? (set/intersection down-rock fixed))
                ;; Otherwise just a move
                (recur fixed down-rock rock-num (inc-move move-num) time top hidden)
                ;; We have a landing
                (do
                  (when (= (mod time 1000) 0) (println "time=" time " score=" (+ top hidden)))
                  (recur (into fixed rock) #{} rock-num (inc-move move-num) (inc time)
                         (max top (second (y-bounds rock)))
                         hidden)))))))))

(defn part-2 [state move-str stop]
  (let [state (find-repeat state move-str stop)
        state (warp state stop)]
    (continue state test-string stop)))

(part-2
 (dissoc (dissoc test-state :rocks) :moves)
 test-string
 2022)

(part-2
 (dissoc (dissoc state :rocks) :moves)
 data-string
 2022)

(let [state (dissoc (dissoc state :rocks) :moves)
      move-str data-string
      stop 2022]
  (let [state (find-repeat state move-str stop)]
        ;state (warp state stop)]
    state))
    ;(continue state test-string stop)))

;(find-repeat (dissoc state :rocks) data-string 1000000)

#_(part-2 test-state 1000000000000)
