(ns p11
  (:require [util :as util]
            [clojure.set :as s]
            [clojure.math.combinatorics :as combo]
            [clojure.data.priority-map :refer [priority-map]]
            [clojure.string :as str]
            [clojure.set :as set]))



(def CHEMICALS
  {:polonium :Po
   :hydrogen :H
   :lithium :Li
   :thulium :Tm
   :promethium :Pm
   :ruthenium :Ru
   :cobalt :Co
   :elerium :El
   :dilithium :Dl})

(def test-data
  {:loc 1
   :generators {1 #{} 2 #{:H} 3 #{:Li} 4 #{}}
   :microchips {1 #{:H :Li} 2 #{} 3 #{} 4 #{}}})

(def data
  {:loc 1
   :generators {1 #{:Po :Tm :Pm :Ru :Co} 2 #{} 3 #{} 4 #{}}
   :microchips {1 #{:Tm :Ru :Co} 2 #{:Po :Pm} 3 #{} 4 #{}}})



(defn valid?
  "Check that no chip appears on the same floor as a generator without its own."
  [state]
  (and
   (<= 1 (:loc state) 4)
   (= (into #{} (keys (:generators state))) #{1 2 3 4})
   (= (into #{} (keys (:microchips state))) #{1 2 3 4})
   (every? some? (for [[floor chips] (:microchips state)
                       chip chips]
                  (let [generators ((:generators state) floor)]
                    (or (empty? generators) (generators chip)))))))

(defn well-formed? [state]
  (= (apply s/union (vals (:microchips state)))
     (apply s/union (vals (:generators state)))))


(defn inc-loc [state]
  (update state :loc inc))

(defn dec-loc [state]
  (update state :loc dec))

(defn empty-moves [state]
  [(inc-loc state)
   (dec-loc state)])

(defn gen-moves [data]
  (let [{:keys [loc generators] :as data} data]
    (flatten (for [generator (generators loc)]
              (let [removed (update-in data [:generators loc] disj generator)]
                [(inc-loc (update-in removed [:generators (inc loc)] conj generator))
                 (dec-loc (update-in removed [:generators (dec loc)] conj generator))])))))

(defn chip-moves [data]
  (let [{:keys [loc microchips] :as data} data]
    (flatten (for [chip (microchips loc)]
              (let [removed (update-in data [:microchips loc] disj chip)]
                [(inc-loc (update-in removed [:microchips (inc loc)] conj chip))
                 (dec-loc (update-in removed [:microchips (dec loc)] conj chip))])))))

(defn gen-gen-moves [data]
  (let [{:keys [loc generators] :as data} data]
    (flatten (for [[gen1 gen2] (combo/combinations (generators loc) 2)]
               (let [removed (update-in data [:generators loc] disj gen1 gen2)]
                 [(inc-loc (update-in removed [:generators (inc loc)] conj gen1 gen2))
                  (dec-loc (update-in removed [:generators (dec loc)] conj gen1 gen2))])))))

(defn chip-chip-moves [data]
  (let [{:keys [loc microchips] :as data} data]
    (flatten (for [[chip1 chip2] (combo/combinations (microchips loc) 2)]
               (let [removed (update-in data [:microchips loc] disj chip1 chip2)]
                 [(inc-loc (update-in removed [:microchips (inc loc)] conj chip1 chip2))
                  (dec-loc (update-in removed [:microchips (dec loc)] conj chip1 chip2))])))))

(defn gen-chip-moves [data]
  (let [{:keys [loc generators microchips] :as data} data]
    (flatten (for [generator (generators loc)
                   chip (microchips loc)]
               (let [removed (-> data
                                 (update-in [:generators loc] disj generator)
                                 (update-in [:microchips loc] disj chip))]
                 [(-> removed
                      (update-in [:generators (inc loc)] conj generator)
                      (update-in [:microchips (inc loc)] conj chip)
                      inc-loc)
                  (-> removed
                      (update-in [:generators (dec loc)] conj generator)
                      (update-in [:microchips (dec loc)] conj chip)
                      dec-loc)])))))

(defn find-generator-floor [state chemical]
  (first (filter #(((:generators state) %) chemical) [1 2 3 4])))

(defn find-microchip-floor [state chemical]
  (first (filter #(((:microchips state) %) chemical) [1 2 3 4])))

(defn move-signature [state]
  (let [chemicals (reduce set/union (for [floor [1 2 3 4]] ((:microchips state) floor)))]
    (into #{} (for [chem chemicals]
                 [(find-generator-floor state chem)
                  (find-microchip-floor state chem)]))))

(comment
  (defn neighbors [data]
   (filter valid? (concat (util/distinct-by move-signature (gen-moves data))
                          (util/distinct-by move-signature (chip-moves data))
                          (util/distinct-by move-signature (gen-gen-moves data))
                          (util/distinct-by move-signature (chip-chip-moves data))
                          (util/distinct-by move-signature (gen-chip-moves data))))))
(defn neighbors [data]
  (transduce (comp (util/distinct-by move-signature) (filter valid?)) conj (concat (gen-moves data)
                                                                                   (chip-moves data)
                                                                                   (gen-gen-moves data)
                                                                                   (chip-chip-moves data)
                                                                                   (gen-chip-moves data))))

(defn heuristic [state]
    (let [{:keys [loc generators microchips]} state]
      (apply + (- 4 loc)
             (for [floor [1 2 3 4]]
               (* (- 4 floor) (inc (/ (+ (count (generators floor)) (count (microchips floor))) 2)))))))


(defn make-goal [data]
  {:loc 4
   :generators {1 #{} 2 #{} 3 #{} 4 (reduce s/union (vals (:generators data)))}
   :microchips {1 #{} 2 #{} 3 #{} 4 (reduce s/union (vals (:microchips data)))}})


(defn solution [state]
    (util/a-star
     state ;; start
     #(= % (make-goal state)) ;; goal
     (constantly (constantly 1)) ;; cost
     neighbors ;; neighbors
     (constantly 0))) ;; heuristic

(comment
  (time (solution data)))


(comment
  (let [state data]
   (time (util/a-star
          state ;; start
          #(= % (make-goal state)) ;; goal
          (constantly (constantly 1)) ;; cost
          neighbors ;; neighbors
          heuristic))) ;; heuristic

  (let [state data]
   (time (util/a-star
          state ;; start
          #(= % (make-goal state)) ;; goal
          (constantly (constantly 1)) ;; cost
          neighbors ;; neighbors
          (constantly 0))))) ;; heuristic



(defonce test-fastest-path (solution test-data))

(defn print-state [state]
    (println "State, loc = " (:loc state))
    (println
     (str/join
      "\n"
      (for [floor [4 3 2 1]]
        [(seq ((:generators state) floor)) (seq ((:microchips state) floor))])))
    (println))

(defonce fastest-path (solution data))

(def ans1 (dec (count fastest-path)))
(println "Answer1: " ans1)


(def data-2 (-> data
                (update-in [:generators 1] conj :El :Dl)
                (update-in [:microchips 1] conj :El :Dl)))


;; (defonce fastest-path-2 (solution data-2))
;; (def ans2 (dec (count fastest-path-2)))
;; (println "Answer2: " ans2)

(comment
  (let [state test-data]
    (inc
     (count
      (util/a-star
       state ;; start
       (make-goal state) ;; goal
       (constantly (constantly 1)) ;; cost
       neighbors ;; neighbors
       heuristic)))) ;; heuristic


  (let [data data]
   (filter valid? (concat (empty-moves data)
                        (gen-moves data)
                        (chip-moves data)
                        (gen-gen-moves data)
                        (chip-chip-moves data)
                        (gen-chip-moves data)))



   (let [{:keys [loc generators microchips] :as data} data]
      (for [generator (generators loc)
            microchip (microchips loc)]
         [generator microchip]))))


(comment
  (valid? test-data)
  (valid? data))

;; Alright, right now the solution is taking too long to generate, I realize now that I have to cut down the search
;; space more by eliminating symmetries and stuff, I should probably try to rework the solution so that my representation
;; of the space is just given by the `signature` as in that method above, meaning I represent the space
;; without any reference to the element names but instead as:
;;      {:loc 0 :sets #{[1 2] [1 3]}}
;; here each one of the pairs in `sets` will be the location of the microchip and the generator respectfully
;; for one of the elements.


(def test-state {:loc 0 :sets (util/bag [1 2] [1 3])})
(def state {:loc 0 :sets (util/bag [2 1] [1 1] [2 1] [1 1] [1 1])})



(comment
  state)
