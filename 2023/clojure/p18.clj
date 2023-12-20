;; # Advent of Code - Day 18

(ns p18
  (:require [clojure.string :as str]))


(def data-string (slurp "../input/18.txt"))
(def test-string "R 6 (#70c710)
D 5 (#0dc571)
L 2 (#5713f0)
D 2 (#d2c081)
R 2 (#59c680)
D 2 (#411b91)
L 5 (#8ceee2)
U 2 (#caa173)
L 1 (#1b58a2)
U 2 (#caa171)
R 2 (#7807d2)
U 3 (#a77fa3)
L 2 (#015232)
U 2 (#7a21e3)")


(def data-re #"(U|D|L|R) (\d+) \((#[0-9a-f]{6})\)")

(defn process-line [line]
    (let [[_ dir amount color] (re-matches data-re line)]
      {:col color :dir (keyword dir) :amt (parse-long amount)}))

(defn ->data [s]
  (map process-line (str/split-lines s)))

(def data (->data data-string))
(def test-data (->data test-string))

;; In order to calculate the area we'll use Stoke's theorem:
;;
;; $$ A = \frac 12 \int y dx - x dy $$
;;

(defn area-of-instructions [data]
  (let [[area perim end]
        (reduce
           (fn [[area perim [y x]] {dir :dir amt :amt}]
             (let [[dy dx] (case dir
                            :R [0 amt]
                            :L [0 (- amt)]
                            :U [(- amt) 0]
                            :D [amt 0])]
               [(+ area (* y dx) (- (* x dy)))
                (+ perim (abs dx) (abs dy))
                [(+ y dy) (+ x dx)]]))
           [0 0 [0 0]]
           data)]
    (assert (= end [0 0]))
    (inc (+ (/ (abs area) 2) (/ perim 2)))))



(defn part-1 [data]
  (area-of-instructions data))

(assert (= 62 (part-1 test-data)))
(defonce ans1 (part-1 data))
(assert (= ans1 40714))



;; ## Part 2
;;
;; Looks like the elves messed things up and put things into the hexadecimal codes instead.
;;

(defn hex-number [& vals]
  (let [hexval {\0 0 \1 1 \2 2 \3 3 \4 4
                \5 5 \6 6 \7 7 \8 8 \9 9
                \a 10 \b 11 \c 12 \d 13 \e 14 \f 15}]
    (reduce
     (fn [val x] (+ (* val 16) (hexval x)))
     0
     vals)))

(defn convert [{col :col}]
  (let [[_ a b c d e f] col
        inst-lookup {\0 :R \1 :D \2 :L \3 :U}]
    {:dir (inst-lookup f) :amt (hex-number a b c d e)}))


(defn part-2 [data]
  (area-of-instructions (map convert data)))

(assert (= 952408144115 (part-2 test-data)))
(defonce ans2 (part-2 data))
(assert (= ans2 129849166997110))


(defn -main []
    (println "Answer1:" ans1)
    (println "Answer2:" ans2))
