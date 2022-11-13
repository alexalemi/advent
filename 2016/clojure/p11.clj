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


;; The representation here uses a bag, with the microchip first and the generator second.
;; They are stored in a frequency map of the number of times the config appears.
(def test-data {:loc 1 :configs (frequencies [[1 2] [1 3]])})
(def data {:loc 1 :configs (frequencies [[2 1] [1 1] [2 1] [1 1] [1 1]])})

(defn valid?
  "Check that no chip appears on the same floor as a generator without its own."
  [state]
  (and (<= 1 (:loc state) 4) (->> state
                                  :configs
                                  keys
                                  (into [])
                                  flatten
                                  (every? #(<= 1 % 4)))))

(defn inc-loc [state]
  (update state :loc inc))

(defn dec-loc [state]
  (update state :loc dec))

(defn disjoin
  ([bag key] (if (= (bag key) 1) (dissoc bag key) (update bag key dec)))
  ([bag key & extra] (apply disjoin (disjoin bag key) extra)))

(defn conjoin
  ([bag key] (update bag key (fnil inc 0)))
  ([bag key & extra] (apply conjoin (conjoin bag key) extra)))


;; First consider moving a single generator or microchip
(defn one-moves [data]
  (let [{:keys [loc configs] :as data} data]
    (into #{} (filter some? (flatten (for [[[m g] c] configs]
                                      (let [removed (update data :configs disjoin [m g])]
                                       [(when (and (= g loc) (< loc 4)) (inc-loc (update removed :configs conjoin [m (inc g)])))
                                        (when (and (= m loc) (< loc 4)) (inc-loc (update removed :configs conjoin [(inc m) g])))
                                        (when (and (= g loc) (> loc 1)) (dec-loc (update removed :configs conjoin [m (dec g)])))
                                        (when (and (= m loc) (> loc 1)) (dec-loc (update removed :configs conjoin [(dec m) g])))])))))))


(defn neighbors [data]
    (let [loc (:loc data)
          ones (one-moves data)]
      (into ones (flatten (for [x ones]
                              (filter #(= (:loc x) (:loc %)) (one-moves (assoc x :loc loc))))))))


(defn size [state]
  (reduce + (vals (:configs state))))

(comment
  (neighbors test-data)

  (size data))

(defn make-goal [data]
  {:loc 4
   :configs {[4 4] (size data)}})


(defn solution [state]
    (util/a-star
     state ;; start
     #(= % (make-goal state)) ;; goal
     (constantly (constantly 1)) ;; cost
     neighbors ;; neighbors
     (constantly 0))) ;; heuristic

(comment
  (time (count (solution data))))


(defonce test-fastest-path (solution test-data))

(defn print-state [state]
    (println "State, loc = " (:loc state))
    (println
     (str/join
      "\n"
      (for [floor [4 3 2 1]]
        [(seq ((:generators state) floor)) (seq ((:microchips state) floor))])))
    (println))

(defonce fastest-path (time (solution data)))

(def ans1 (dec (count fastest-path)))
(println "Answer1: " ans1)


(def data-2 (update data :configs conjoin [1 1] [1 1]))

(defonce fastest-path-2 (time (solution data-2)))
(def ans2 (dec (count fastest-path-2)))
(println "Answer2: " ans2)
