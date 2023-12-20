;; # Advent of Code 2023 - Day 20
(ns p20
  (:require [clojure.string :as str]))

(def data-string (slurp "../input/20.txt"))
(def test-string-1 "broadcaster -> a, b, c
%a -> b
%b -> c
%c -> inv
&inv -> a")

(def test-string-2 "broadcaster -> a
%a -> inv, con
&inv -> b
%b -> con
&con -> output")

(defn process-line [s]
  (let [[_ type module outputs] (re-matches #"(%|&)?(\w+) -> ([\w, ]+)" s)
        outputs (map keyword (str/split outputs #", "))]
    [(keyword module)
     (into {:outputs outputs}
       (case type
         "%" {:type :flip-flop :state :off}
         "&" {:type :conjuction :memory {}}
         (if (= module "broadcaster")
             {:type :broadcaster}
             {})))]))

(defn module-parents [data name]
 (transduce
  (comp (filter (fn [[k v]] (nat-int? (.indexOf (v :outputs) name))))
        (map first))
  conj data))

(defn populate-memory [data]
  (let [conjuctions (transduce
                     (comp (filter (fn [[k v]] (= :conjuction (v :type)))) (map first))
                     conj data)]
    (apply merge-with merge data
     (for [c conjuctions]
       {c {:memory (zipmap (module-parents data c) (repeat :lo))}}))))

(defn ->data [s]
  (populate-memory (into {} (map process-line (str/split-lines s)))))

(def data (->data data-string))
(def test-data-1 (->data test-string-1))
(def test-data-2 (->data test-string-2))

(defn toggle [state]
  (if (= state :off) :on :off))

(defmulti action (fn [module _signal _from] (module :type)))

(defmethod action :flip-flop [module signal _from]
  (let [{:keys [outputs state]} module]
    (case signal
      :hi [module []]
      :lo [(update module :state toggle) (map vector outputs (repeat (if (= state :off) :hi :lo)))])))

(comment
  (action {:outputs [:a] :state :off :type :flip-flop} :hi :frm)
  (action {:outputs [:a] :state :off :type :flip-flop} :lo :frm)
  (action {:outputs [:a] :state :on :type :flip-flop} :lo :frm))

(defn hi? [x] (= x :hi))

(defmethod action :conjuction [module signal from]
    (let [{outputs :outputs} module
          module (assoc-in module [:memory from] signal)
          all-hi (every? hi? (vals (module :memory)))]
       [module (map vector outputs (repeat (if all-hi :lo :hi)))]))

(comment
  (action {:outputs [:b] :memory {:a :lo} :type :conjuction} :hi :a)
  (action {:outputs [:b] :memory {:a :lo} :type :conjuction} :lo :a))

(defmethod action :broadcaster [module signal _from]
  (let [{outputs :outputs} module]
    [module (map vector outputs (repeat signal))]))

(defmethod action :default [module signal _from]
  (let [{outputs :outputs} module]
    [module []]))

(comment
  (action {:outputs [:b :c] :type :broadcaster} :hi :a))

(defn add-from [frm [to signal]] [to frm signal])
(defn add-froms [frm signals] (map (partial add-from frm) signals))

(defn push-button [data]
  (loop [data data
         queue (conj clojure.lang.PersistentQueue/EMPTY [:broadcaster :button :lo])
         los 0
         his 0]
    (if-let [[to frm signal] (peek queue)]
      (if-let [module (data to)]
        (let [[module signals] (action (data to) signal frm)]
          ;(println frm " -" signal "-> " to)
          (recur
           (assoc data to module)
           (if signals
             (reduce conj (pop queue) (add-froms to signals))
             (pop queue))
           (if (hi? signal) los (inc los))
           (if (hi? signal) (inc his) his)))
        (recur data (pop queue)
           (if (hi? signal) los (inc los))
           (if (hi? signal) (inc his) his)))
      [data [los his]])))

(defn round [[odata [n olos ohis]]]
  (let [[data [los his]] (push-button odata)]
    [data [(inc n) (+ olos los) (+ ohis his)]]))

(defn part-1 [data]
  (let [[n los his] (second (nth (iterate round [data [0 0 0]]) 1000))]
    (* los his)))

(assert (= (part-1 test-data-1) 32000000))
(assert (= (part-1 test-data-2) 11687500))
(defonce ans1 (part-1 data))

;; ## Part 2
;;  I need to deliver a lo pulse to rx

(defn push-button-watch [data [watch-to watch-frm watch-sig]]
  (loop [data data
         queue (conj clojure.lang.PersistentQueue/EMPTY [:broadcaster :button :lo])]
    (if-let [[to frm signal] (peek queue)]
      (if (and (= to watch-to) (= frm watch-frm) (= signal watch-sig))
         [data true]
         (if-let [module (data to)]
           (let [[module signals] (action (data to) signal frm)]
             (recur
              (assoc data to module)
              (if signals
                (reduce conj (pop queue) (add-froms to signals))
                (pop queue))))
           (recur data (pop queue))))
      [data false])))

;; How are the parents of rx?
(def rx-parent (first (module-parents data :rx)))
;; This is tg for my graph
;;
;; Now who are their parents?
(def to-watch (module-parents data rx-parent))
;; db ln tf vq


(defn third [[_ _ x]] x)

(defn generate-counter [data x]
  (letfn [(counter [[data n _done?]]
             (let [[data done?] (push-button-watch data [rx-parent x :hi])]
               [data (inc n) done?]))]
    (second (first (filter third (iterate counter [data 0 false]))))))


(def ans2 (reduce * (map (partial generate-counter data) to-watch)))
(assert (= ans2 252667369442479))


(defn -main []
  (println "Answer 1:" ans1)
  (println "Answer 2:" ans2))
