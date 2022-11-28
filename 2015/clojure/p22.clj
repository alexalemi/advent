(ns p22
 (:require [util :as util]))

(def state
  "Default initial state of the game."
  {:player {:hp 50 :mana 500 :armor 0}
   :boss {:hp 55 :damage 8}
   :spent 0
   :effects {}
   :moves []})


(def spells
  "All of the available spells."
  {:magic-missile
   {:cost 53
    :body (fn [state] (update-in state [:boss :hp] - 4))}

   :drain
   {:cost 73
    :body (fn [state] (-> state
                         (update-in [:boss :hp] - 2)
                         (update-in [:player :hp] + 2)))}

   :shield
   {:cost 113
    :effect {:turns 6
             :active (fn [state] (assoc-in state [:player :armor] 7))
             :inactive (fn [state] (assoc-in state [:player :armor] 0))}}
   :poison
   {:cost 173
    :effect {:turns 6
             :active (fn [state] (update-in state [:boss :hp] - 3))}}

   :recharge
   {:cost 229
    :effect {:turns 5
             :active (fn [state] (update-in state [:player :mana] + 101))}}})


(defn valid-spells
  "Determine which spells are valid to play, you must have
  the mana and if its an effect spell, must not already be active."
  [{{mana :mana} :player effects :effects}]
  (map first (filter (fn [[spell {cost :cost}]]
                        (and (<= cost mana)
                             (not (pos? (get effects spell 0)))))
                 spells)))




(defn process-effects [state]
  (reduce-kv (fn [state spell turn]
               (let [state (update-in state [:effects spell] #(max 0 (dec %)))]
                 (if (pos? turn)
                   ((get-in spells [spell :effect :active] identity) state)
                   ((get-in spells [spell :effect :inactive] identity) state))))
             state
             (:effects state)))


(defn cast-spell [state spell]
  (let [{cost :cost effect :effect body :body} (spells spell)
        state (-> state
                  (update-in [:player :mana] - cost)
                  (update :spent + cost)
                  (update :moves conj spell))
        state (if body (body state) state)
        state (if effect (assoc-in state [:effects spell] (:turns effect)) state)]
    state))




(declare boss-turn)

(def boss-dead? (comp not pos? :hp :boss))
(def player-dead? (comp not pos? :hp :player))

(def goal? boss-dead?)

(defn cost [current]
  (if current
   (fn [neigh]
     (if neigh
       (- (:spent neigh) (:spent current))
       ##Inf))
   (constantly ##Inf)))

(defn neighbors [state]
  (let [state (process-effects state)
        spells (valid-spells state)]
    (if (player-dead? state)
        '()
        (map (fn [spell] (boss-turn (cast-spell state spell))) spells))))


(defn boss-turn [state]
  (let [state (process-effects state)
        {boss :boss player :player} state]
      (update-in state [:player :hp] - (max 1 (- (:damage boss) (:armor player))))))




(defn min-spend [state]
   (loop [frontier (util/queue [state])
          best-seen ##Inf]
    ;; (println "frontier = " (into [] frontier) " best-seen=" best-seen)
    (if-let [state (peek frontier)]
      (let [state (process-effects state)
            spells (valid-spells state)]
       (cond
         (boss-dead? state)
         (recur (pop frontier) (min best-seen (:spent state)))

         (or (player-dead? state)
             (empty? spells))
         (recur (pop frontier) best-seen)

         (> (:spent state) best-seen) (recur (pop frontier) best-seen)

         :else
         (recur
          (into (pop frontier) (map (fn [spell] (boss-turn (cast-spell state spell))) spells))
          best-seen)))
      best-seen)))


(defonce ans1 (min-spend state))
(println "Answer1:" ans1)

(comment

  (+ 53 173)

  (let [state {:player {:hp 10 :mana 250 :armor 0}
               :boss {:hp 13 :damage 8}
               :effects {}
               :spent 0}]
    (min-spend state)))


(defn hurt-player [state]
  (update-in state [:player :hp] dec))

(defn min-spend-2 [state]
   (loop [frontier (util/queue [state])
          best-seen ##Inf]
    ;; (println "frontier = " (into [] frontier) " best-seen=" best-seen)
    (if-let [state (peek frontier)]
      (let [state (hurt-player state)]
        (cond
          (boss-dead? state) (recur (pop frontier) (min best-seen (:spent state)))
          (player-dead? state) (recur (pop frontier) best-seen)
          :else
          (let [state (process-effects state)
                spells (valid-spells state)]
             (cond
               (boss-dead? state)
               (recur (pop frontier) (min best-seen (:spent state)))

               (or (player-dead? state)
                   (empty? spells))
               (recur (pop frontier) best-seen)

               (> (:spent state) best-seen) (recur (pop frontier) best-seen)

               :else
               (recur
                 (into (pop frontier) (map (fn [spell] (boss-turn (cast-spell state spell))) spells))
                 best-seen)))))
      best-seen)))


(defonce ans2 (min-spend-2 state))
(println "Answer2:" ans2)
