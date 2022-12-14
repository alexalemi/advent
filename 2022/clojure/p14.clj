;; # 🎄 Advent of Code 2022 - Day 14 - Regolith Reservoir
(ns p14
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]
            [clojure.set :as set]
            [clojure.edn :as edn])
  (:import [java.awt.image BufferedImage]))

;; ## Data processing
;; The input comes in the form is a string of coordinates that paints
;; the walls of the board.

(def data-string (slurp "../input/14.txt"))

(def test-string "498,4 -> 498,6 -> 496,6
503,4 -> 502,4 -> 502,9 -> 494,9")

(defn horizontal-line
  "All of the coordinates between two horizontal points."
  [[x1 y1] [x2 y2]]
  (assert (= y1 y2) "Can only form a line on between points on the same height.")
  (let [xlo (min x1 x2)
        xhi (max x1 x2)]
    (for [x (range xlo (inc xhi))] [x y1])))

(defn vertical-line
  "All of the coordinates between two vertical points."
  [[x1 y1] [x2 y2]]
  (assert (= x1 x2) "Can only form a line on between points at the same x.")
  (let [ylo (min y1 y2)
        yhi (max y1 y2)]
    (for [y (range ylo (inc yhi))] [x1 y])))

(defn line [[x1 y1] [x2 y2]]
  (if
   (= x1 x2) (vertical-line [x1 y1] [x2 y2])
   (horizontal-line [x1 y1] [x2 y2])))

(defn trace [[start & spots]]
  (loop [rock #{}
         start start
         spots spots]
    (if-let [end (first spots)]
      (recur
       (into rock (line start end))
       end
       (rest spots))
      rock)))

(defn bounds [rock]
  [[(dec (reduce min (map first rock)))
    (inc (reduce max (map first rock)))]
   [0
    (reduce max (map second rock))]])

(defn process [s]
  (let [rock (apply set/union
                    (for [line (str/split-lines s)]
                      (trace (into []
                                   (for [pair (str/split line #" -> ")]
                                     (edn/read-string (str "[" pair "]")))))))]

    {:source [500 0]
     :sand #{}
     :rock rock
     :bounds (bounds rock)}))

(def test-data (process test-string))
(def data (process data-string))

;; ## Visualization

;; We'll build a clerk RGB image visualizater for our state.

(defn render-image
  "Render the puzzle as a raw image."
  ([Ω] (render-image Ω (:bounds Ω) 1))
  ([Ω zoom] (render-image Ω (:bounds Ω) zoom))
  ([Ω bounds zoom]
   (let [{:keys [rock sand source]} Ω
         [[xlo xhi] [ylo yhi]] bounds
         width (* zoom (- (inc xhi) (dec xlo)))
         height (* zoom (- (inc yhi) (dec ylo)))
         img (BufferedImage. width height BufferedImage/TYPE_3BYTE_BGR)]
     (doseq [xp (range width)
             yp (range height)]
       (let [x (+ (quot xp zoom) (dec xlo))
             y (+ (quot yp zoom) (dec ylo))
             loc [x y]]
         (.setRGB img xp yp
                  (cond
                    (rock loc) (.getRGB (java.awt.Color. 42 42 42))
                    (= source loc) (.getRGB (java.awt.Color. 255 0 0))
                    (sand loc) (.getRGB (java.awt.Color. 194 178 128))
                    :else (.getRGB java.awt.Color/WHITE)))))
     img)))

(render-image (update test-data :sand conj [500 8]) 10)

;; ## Core Logic
;;
;; Now we have to code up the core logic of the puzzle.  We start dropping sand, where the physics of sand
;; is that it falls straight down if able, otherwise it tries to go down and left and finally it tries to go
;; down and right.

(defn below-bottom?
  "Are we above the bottom of the world's end?"
  [Ω [_ y]]
  (let [[_ [_ yhi]] (:bounds Ω)]
    (> y yhi)))

(defn down [[x y]] [x (inc y)])
(defn down-left [[x y]] [(dec x) (inc y)])
(defn down-right [[x y]] [(inc x) (inc y)])

(defn solid? [Ω loc]
  (or ((:rock Ω) loc) ((:sand Ω) loc)))

(defn drop-sand [Ω]
  (let [solid? (partial solid? Ω)
        free? (complement solid?)
        source (:source Ω)]
    (loop [loc source]
      (cond
        (below-bottom? Ω loc) (assoc Ω :finished true)
        (free? (down loc)) (recur (down loc))
        (free? (down-left loc)) (recur (down-left loc))
        (free? (down-right loc)) (recur (down-right loc))
        :else
        (if (= loc source)
          (-> Ω
              (assoc :finished true)
              (update :sand conj loc))
          (update Ω :sand conj loc))))))

(defn fill-with-sand [Ω]
  (first (drop-while (complement :finished) (iterate drop-sand Ω))))

(let [Ω test-data]
  (let [finished (fill-with-sand Ω)]
    (render-image finished 10)))

(let [Ω data]
  (let [finished (fill-with-sand Ω)]
    (def ans1 (count (:sand finished)))
    (render-image finished 5)))

ans1 ;; = 745

;; ## Part 2
;; Now there is a big floor at the bottom that sand can land on and we have to keep filling up
;; with sand until we block the source.

(defn add-floor [Ω]
  (let [floor-y (+ 2 (second (second (:bounds Ω))))
        [xs ys] (:source Ω)
        rock (into (:rock Ω) (line [(- xs (inc floor-y)) floor-y] [(+ xs (inc floor-y)) floor-y]))
        new-bounds (bounds rock)]
    (-> Ω
        (assoc :bounds new-bounds)
        (assoc :rock rock))))

(let [Ω (add-floor test-data)]
  (let [finished (fill-with-sand Ω)]
    [(count (:sand finished))
     (render-image finished 10)]))

(let [Ω (add-floor data)]
  (let [finished (fill-with-sand Ω)]
    (def ans2 (count (:sand finished)))
    (render-image finished 4)))

ans2 ;; = 27551

;; ## Main

(defn -main [& _]
  (println "Answer1: " ans1)
  (println "Answer2: " ans2))
