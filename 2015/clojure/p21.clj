(ns p21
  (:require [clojure.math.combinatorics :as combo]))


(def boss
  {:hp 100
   :damage 8
   :armor 2})

(def store
  {:weapons [{:name :dagger :cost 8 :damage 4 :armor 0}
             {:name :shortsword :cost 10 :damage 5 :armor 0}
             {:name :warhammer :cost 25 :damage 6 :armor 0}
             {:name :longsword :cost 40 :damage 7 :armor 0}
             {:name :geataxe :cost 74 :damage 8 :armor 0}]
   :armor [{:name :leather :cost 13 :damage 0 :armor 1}
           {:name :chainmail :cost 31 :damage 0 :armor 2}
           {:name :splitmail :cost 53 :damage 0 :armor 3}
           {:name :bandedmail :cost 75 :damage 0 :armor 4}
           {:name :platemail :cost 102 :damage 0 :armor 5}]
   :rings [{:name :damage1 :cost 25 :damage 1 :armor 0}
           {:name :damage2 :cost 50 :damage 2 :armor 0}
           {:name :damage3 :cost 100 :damage 3 :armor 0}
           {:name :defense1 :cost 20 :damage 0 :armor 1}
           {:name :defense2 :cost 40 :damage 0 :armor 2}
           {:name :defence3 :cost 80 :damage 0 :armor 3}]})

(def NOTHING {:name :nothing :cost 0 :damage 0 :armor 0})
;; You need exactly 1 weapon. Armor is optional but capped at 1
;; And you can buy 0 to 2 rings.  There is only one of each
;; item.

(def test
  [{:hp 8 :damage 5 :armor 5}
   {:hp 12 :damage 7 :armor 2}])

(defn round [[attacker defender]]
  (let [{damage :damage} attacker
        {armor :armor} defender]
    [(update defender :hp - (max 1 (- damage armor)))
     attacker]))

(defn winner [boss player]
   ((comp :who second) (first (drop-while (comp pos? :hp first) (iterate round [(assoc player :who :player) (assoc boss :who :boss)])))))

(def my-winner (partial winner boss))


(def all-baskets
  (for [weapons (:weapons store)
        armor (conj (:armor store) NOTHING)
        rings (concat [[NOTHING]] (map list (:rings store)) (combo/combinations (:rings store) 2))]
    (concat [weapons] [armor] rings)))


(defn combine-basket [basket]
  (reduce
   (fn [acc val]
     (-> acc
         (update :cost + (:cost val))
         (update :damage + (:damage val))
         (update :armor + (:armor val))
         (update :items conj (:name val))))
   {:items [] :cost 0 :damage 0 :armor 0}
   basket))


(defn min-reducer
  ([] ##Inf)
  ([x] x)
  ([x y] (min x y)))

(def ans1
  (transduce
   (comp
    (map combine-basket)
    (map #(assoc % :hp 100))
    (map (juxt :cost my-winner))
    (filter #(= (second %) :player))
    (map first))
   min-reducer
   all-baskets))
(println "Answer1:" ans1)



(defn max-reducer
  ([] ##-Inf)
  ([x] x)
  ([x y] (max x y)))

(def ans1
  (transduce
   (comp
    (map combine-basket)
    (map #(assoc % :hp 100))
    (map (juxt :cost my-winner))
    (filter #(= (second %) :boss))
    (map first))
   max-reducer
   all-baskets))
(println "Answer2:" ans2)
