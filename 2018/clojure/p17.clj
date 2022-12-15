;; # 🎄 Advent of Code 2018 - Day 17
;;
;; [[link to puzzle]](https://adventofcode.com/2018/day/17)
;;
;; In this challenge we are dropping water
;; into a bunch of clay buckets and have to figure
;; out how the water flows.

(ns p17
  (:require [clojure.string :as str]
            [nextjournal.clerk :as clerk]
            [clojure.set :as set])
  (:import [java.awt.image BufferedImage]))

;; ## Input Processing
;; We'll start by trying to turn the input into some consumable form.

(defn s->ints [s]
  (mapv read-string (re-seq #"\d+" s)))

(defn process-one [s]
  (let [[one many] (str/split s #", ")]
    {(keyword (str (first one)))
     (first (s->ints one))
     (keyword (str (first many)))
     (s->ints many)}))

(defn process [s]
  (->> s
       (str/split-lines)
       (map process-one)))

(def data (process (slurp "../input/17.txt")))

(def test-data (process "x=495, y=2..7
y=7, x=495..501
x=501, y=3..7
x=498, y=2..4
x=506, y=1..2
x=498, y=10..13
x=504, y=10..13
y=13, x=498..504"))

(defn horizontal-line
  "All of the coordinates between two horizontal points."
  [[x1 y1] [x2 y2]]
  (assert (= y1 y2) "Can only form a line on between points on the same height.")
  (for [x (range x1 (inc x2))] [x y1]))

(defn vertical-line
  "All of the coordinates between two vertical points."
  [[x1 y1] [x2 y2]]
  (assert (= x1 x2) "Can only form a line on between points at the same x.")
  (for [y (range y1 (inc y2))] [x1 y]))

(defn line [{x :x y :y}]
  (if (coll? x)
    (horizontal-line [(first x) y] [(second x) y])
    (vertical-line [x (first y)] [x (second y)])))

;; Let's think a bit about how we should best represent our world. We clearly
;; need some way to specify all of the clay locations, so we'll store a set in
;; `:clays` with those coordinates.  We also need access to the bounds,
;; which could be a function as its drived by

(defn data->state [data]
  (let [clay (reduce into #{} (map line data))]
    {:clay clay
     :still #{}
     :flowing #{}
     :sources #{[500 0]}
     :bounds
     [[(dec (reduce min (map first clay)))
       (inc (reduce max (map first clay)))]
      [(reduce min (map second clay))
       (reduce max (map second clay))]]}))

(def test-Ω (data->state test-data))
(def Ω (data->state data))

;; ## Visualization
;; Let's build a nice visualizer in terms of `div` elements.
;; This will be too expensive to use for the full size puzzle,
;; but we can use it for the test case.

(defn in-bounds?
  "Test whether a point is within the bounds of the puzzle."
  [Ω [_ y]]
  (let [[_  [ylo yhi]] (:bounds Ω)]
    (<= ylo y yhi)))

(defn total-wet
  "Count the total number of water squares."
  [Ω]
  (count (filter (partial in-bounds? Ω)
                 (set/union (:still Ω) (:flowing Ω)))))

(defn render-cell
  "Render a single square."
  [Ω loc]
  (let [{:keys [clay still flowing sources spawn blocked]} Ω]
    [:div.inline-block
     {:style {:width 16 :height 16
              :background-color (cond
                                  (= spawn loc) "yellow"
                                  (= blocked loc) "green"
                                  (sources loc) "blue"
                                  (clay loc) "brown"
                                  (still loc) "darkblue"
                                  (flowing loc) "steelblue"
                                  :else "white")
              :border "solid 1px black"}}]))

(defn render
  "Custom clerk rendering function for a board."
  [Ω]
  (letfn [(box [val] [:div.flex.flex-col [:div {:style {:width 16 :height 16 :font-size "0.5em"}} val]])]
    (let [[[xlo xhi] [ylo yhi]] (:bounds Ω)]
      (clerk/html
       [:div
        (into [:div.flex.inline-flex
               (into [:div.flex.flex-col (box "") (for [y (range ylo (inc yhi))] (box y))])]
              (for [x (range xlo (inc xhi))]
                (into [:div.flex.flex-col (box x)]
                      (for [y (range ylo (inc yhi))]
                        (render-cell Ω [x y])))))
        [:div "wet: " (total-wet Ω)]]))))

(render test-Ω)

;; This looks nice, but it isn't going to work for the full size puzzle,
;; so for that, let's directly generate an Image.

(defn render-image
  "Render the puzzle as a raw image."
  ([Ω] (render-image Ω (:bounds Ω) 1))
  ([Ω zoom] (render-image Ω (:bounds Ω) zoom))
  ([Ω bounds zoom]
   (let [{:keys [clay still flowing sources spawn blocked]} Ω
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
                    (= spawn loc) (.getRGB (java.awt.Color. 255 0 0))
                    (= blocked loc) (.getRGB java.awt.Color/GREEN)
                    (sources loc) (.getRGB (java.awt.Color. 120 190 33))
                    (clay loc) (.getRGB (java.awt.Color. 165 42 42))
                    (still loc) (.getRGB (java.awt.Color. 0 0 139))
                    (flowing loc) (.getRGB (java.awt.Color. 70 130 180))
                    :else (.getRGB java.awt.Color/WHITE)))))
     img)))

;; ## Core Logic
;; Let's start to plan out how we'll actually solve the problem now that we've done
;; some minimal processing of the input.

;; Let's say there are some different types of water,
;; there are `:flowing` squares where water has moved through
;; and there are `:still` squares that behave as though they are clay
;; There is obviously the `:clay` squares.
;;
;; Now in terms of states, of the actual propogation of the water,
;; we'll separate it into two phases.  In the `fall` stage, we will
;; take a `:source` square and let the water fall down until it either
;; hits something or runs off the edge of the puzzle.  If it hits something
;; this will start a `spawn` process that will fill up the buckets.
;;
;; In the `spawn` process, we'll move to the left and right of our
;; spawn point and see if we run into things at both ends, if so,
;; we'll mark that whole line as `:still` and start a new `spawn`
;; one square up from where we started, recursively.

(defn solid?
  "Is a square something we can't fall through?"
  [Ω]
  (some-fn (:clay Ω) (:still Ω)))
(defn not-solid? [Ω] (complement (solid? Ω)))

;; We'll hand code motion on our grid.
(defn left [loc] (update loc 0 dec))
(defn right [loc] (update loc 0 inc))
(defn below [loc] (update loc 1 inc))
(defn above [loc] (update loc 1 dec))

(defn mark-spawn
  "Mark a square as a new spawn point."
  [Ω spawn]
  (-> Ω
      (assoc :spawn spawn)
      (update :flowing conj spawn)))

(defn spread
  "Take the spawn and move whichways until we hit clay or there is nothing below us."
  [which Ω start]
  (loop [Ω (update Ω :flowing conj start)
         spawn start]
    (let [Ω (update Ω :flowing conj spawn)]
      (if
       ;; If empty, so nothing
       (nil? spawn) Ω

       (let [newspawn (which spawn)]
         (if
          ;; If there is something solid beneath us
          ((solid? Ω) (below spawn))
          ;; If there is nothing in our way,
           (if ((not-solid? Ω) newspawn)
           ;; Then continue in that direction
             (recur Ω newspawn)
           ;; Otherwise mark as blocked
             (assoc Ω :blocked spawn))
          ;; If there isn't anything below us mark as source
          ;; we must have gone off an edge.
           (update Ω :sources conj spawn)))))))

(defn mark-still
  "Mark a square as now a still square."
  [Ω loc]
  (-> Ω
      (update :still conj loc)
      (update :sources disj loc)
      (update :flowing disj loc)))

(defn above-bottom?
  "Are we above the bottom of the world's end?"
  [Ω [_ y]]
  (let [[_ [_ yhi]] (:bounds Ω)]
    (<= 0 y yhi)))

(defn fall
  "Pop a source and let it fall."
  [Ω]
  (if-let [loc (first (:sources Ω))]
    (loop [Ω (update Ω :sources disj loc)
           loc loc]
      (let [newloc (below loc)]
        (cond
           ;; If there is something solid below us, create a spawn.
          ((solid? Ω) newloc)
          (mark-spawn Ω loc)

           ;; If we are within the bounds of the image, continue falling.
          (above-bottom? Ω newloc)
          (recur (update Ω :flowing conj newloc) newloc)

           ;; Otherwise just pop the source (we must have hit bottom)
          :else Ω)))
    (assoc Ω :finished true)))

(defn merge-guys
  "Helper for how we'll merge the two spreads."
  [m1 m2]
  (-> m1
      (update :flowing set/union (:flowing m2))
      (update :sources set/union (:sources m2))))

(defn spawn
  "Launch a spawn left and right and resolve."
  [Ω]
  (loop [start (:spawn Ω)
         Ω (dissoc Ω :spawn)]
    (if
      ;; Short circuit if nil
     (nil? start) Ω

     (let [lefty (spread left Ω start)
           righty (spread right Ω start)
           l (:blocked lefty)
           r (:blocked righty)
           combined (merge-guys (dissoc lefty :blocked) (dissoc righty :blocked))]
       (if
          ;; If both directions got blocked, then make those values still
          ;; and add the original parent as another source.
        (and l r)
         (recur
          (above start)
          (reduce
           (fn [m x] (mark-still m x))
           combined
           (horizontal-line l r)))
            ;; Otherwise, just return the combined thing.
         combined)))))

(defn fill
  "The main filling logic."
  [Ω]
  ;; To perform the fill, we will start off by spreading
  ;; left and right from every :split, then
  ;; combine the result, finally we'll initiate a new
  ;; :fall
  (if (:finished Ω)
    Ω
    (fall (spawn Ω))))

(defn fill-completely
  "Keep filling until completely finished."
  [Ω]
  (first (drop-while (complement :finished) (iterate fill Ω))))

;; We can test all of the game logic on the test input
;; and render it with our custom viewer.

(let [Ω (fill-completely test-Ω)]
  (assert (= 57 (total-wet Ω)))
  (render Ω))

;; ## Part 1
;; That looks good, so we compute the actual answer.

(def finished-Ω (time (fill-completely Ω)))

(def ans1 (total-wet finished-Ω))
(render-image finished-Ω 2)

;; ## Part 2
;; For Part 2, we need to let all of the `:flowing` squares
;; drain out.

(def ans2 (count (:still finished-Ω)))
(render-image (assoc finished-Ω :flowing #{}) 2)

#_(let [Ω (time (nth (iterate fill Ω) 300))]
    (render-image Ω 2))

(defn -main [& _]
  (println "Answer 1:" ans1)
  (println "Answer 2:" ans2))
