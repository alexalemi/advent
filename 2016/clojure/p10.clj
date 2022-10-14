;; # Advent of Code Day 10 - Balance Bots
(ns p10
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/10.txt"))
(def test-string "value 5 goes to bot 2
bot 2 gives low to bot 1 and high to bot 0
value 3 goes to bot 1
bot 1 gives low to output 1 and high to bot 0
bot 0 gives low to output 2 and high to output 0
value 2 goes to bot 2")

;; The rules here seem somewhat straight forward, there are value chips that
;; are being allocated to a bunch of different bots.  I think I'll represent
;; the state of the world with a map {bot-num [value-chips]}
;; every round we're going to find all of the bots that are holding two
;; chips and then execute their rule.
;;
;; So the world is going to be
;;
;;    {:rules {bot-num {:low {:kind :bot :which low}
;;                      :high {:kind :output :which low}}}}
;;     :bot {bot-num [value-chips]}
;;     :output {output-num [value-chips]}
;; This should let us then just operate on this datastructure turn by turn.
;;
;; First we need to process the input into its two types of lines.

(defn process-line [line]
   (if (str/starts-with? line "bot")
     (let [[_ bot low-out low high-out high] (re-matches #"bot (\d+) gives low to (bot|output) (\d+) and high to (bot|output) (\d+)" line)
           [bot low high] (map read-string [bot low high])]
        {:kind :rule :result {bot {:low {:kind (keyword low-out) :which low} :high {:kind (keyword high-out) :which high}}}})
     (let [[_ value bot] (re-matches #"value (\d+) goes to bot (\d+)" line)
           [value bot] (map read-string [value bot])]
        {:kind :value :result {:bot bot :value value}})))

(defn process [data-string]
 (let [{rules :rule values :value} (group-by :kind (map process-line (str/split-lines data-string)))]
   {:bot (update-vals (group-by :bot (map :result values)) #(into () (map :value %)))
    :rules (into {} (map :result rules))
    :output {}}))

(def data (process data-string))
(def test-data (process test-string))

(defn find-full-bot [data]
  (ffirst (filter (fn [[k v]] (= 2 (count v))) (:bot data))))


(defn step [data]
 (let [bot (find-full-bot data)
       rule (get (get data :rules) bot)
       {low-rule :low high-rule :high} rule
       {low-kind :kind low-which :which} low-rule
       {high-kind :kind high-which :which} high-rule
       [low high] (sort ((:bot data) bot))]
   (-> data
       (update-in [:bot] dissoc bot)
       (update-in [low-kind low-which] #(conj % low))
       (update-in [high-kind high-which] #(conj % high)))))


(defn has-pair? [[x y] data]
  (some #(or (= % (list x y)) (= % (list y x))) (vals (:bot data))))


(defn first-to-process [data pair]
    (->> data
         (iterate step)
         (filter (partial has-pair? pair))
         first
         :bot
         (filter #(or (= pair (val %))
                      (= (reverse pair) (val %))))
         ffirst))

(comment
  (first-to-process test-data [2 5]))

(defonce ans1 (first-to-process data [61 17]))
(println "Answer1:" ans1)


(def extract (comp (fn [x] (map first [(get x 0) (get x 1) (get x 2)])) :output))

(defonce ans2 (reduce * (extract (first (filter #(every? some? (extract %)) (iterate step data))))))
(println "Answer2:" ans2)
