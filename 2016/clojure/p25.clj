(ns p25
 (:require [clojure.string :as str]))

;; # 2016 - Day 25
;;
;; For this puzzle we are going to use the virtual machine from Day 23
(def data-string (slurp "../input/25.txt"))

;; In order to turn this into some representable code, let's consume the
;; string and put out a list of instructions.

(def DIGITS (into #{} "+-0123456789"))

(defn value-or-reg [x]
  (if (every? DIGITS x)
    (read-string x)
    (keyword x)))

(defn process-line [line]
  (let [[op a b] (str/split line #" ")]
    (if b
        [(keyword op) (value-or-reg a) (value-or-reg b)]
        [(keyword op) (value-or-reg a)])))

(defn process [s]
  (mapv process-line (str/split-lines s)))

(def data (process data-string))

;; To represent a machine, we'll load in the code, as well as some `loc` field and the register values themselves.

(defn machine [codes]
  {:registers {:a 0 :b 0 :c 0 :d 0}
   :loc 0
   :done false
   :output []
   :codes codes})

;; Now let's implement the machine itself, the main method will be step, which will do a single update given the code.
(defn value [machine x]
  (if (keyword? x) ((:registers machine) x) x))


(defn toggle [inst]
 (let [[op a b] inst]
   (cond
     (nil? b) (if (= op :inc)
                [:dec a]
                [:inc a])
     (some? b) (if (= op :jnz)
                 [:cpy a b]
                 [:jnz a b]))))


(defn step [machine]
  (let [{:keys [codes loc]} machine
        code (get codes loc)
        [op a b] code]
    (case op
      nil (assoc machine :done true)
      :cpy (if (keyword? b)
             (-> machine
              (assoc-in [:registers b] (value machine a))
              (update :loc inc))
             (update machine :loc inc))
      :inc (if (keyword? a)
            (-> machine
             (update-in [:registers a] inc)
             (update :loc inc))
            (update machine :loc inc))
      :dec (if (keyword? a)
             (-> machine
              (update-in [:registers a] dec)
              (update :loc inc))
             (update machine :loc inc))
      :jnz (if (zero? (value machine a))
             (update machine :loc inc)
             (update machine :loc + (value machine b)))
      :out (-> machine
               (update :loc inc)
               (update :output conj (value machine a)))
      :tgl (let [which (+ loc (value machine a))
                 code (get-in machine [:codes which])]
             (if code
                (-> machine
                 (assoc-in [:codes which] (toggle code))
                 (update :loc inc))
                (update machine :loc inc))))))

;; Having defined that, we can execute the machine and return the value in the :a register.
(defn run-and-return-a [machine]
  ((comp :a :registers) (last (take-while (complement :done) (iterate step machine)))))


(def warm-up (first (drop-while #(< (:loc %) 8) (iterate step (assoc-in (machine data) [:registers :a] 0)))))


(defn test-runner [n]
  (->> (update-in warm-up [:registers :d] + n)
     (iterate step)
     (drop 30000)
     first
     :output))

(defn check [seq]
  (reduce
   (fn [a v] (if (= ({0 1 1 0} a) v) v (reduced false)))
   1
   seq))


(defn to-binary [n]
  (map {\1 1 \0 0} (Integer/toString n 2)))


(defonce long-ans2 (first (filter #(check (test-runner %)) (range))))

(defonce ans2 (first (filter #(check (reverse (to-binary (+ 2538 %)))) (range))))
(println "Answer2:" ans2)

(test-runner ans2)


(comment
  (defonce ans1 (run-and-return-a (assoc-in (machine data) [:registers :a] 7)))
  (println "Answer1:" ans1)

  (defonce ans2 (run-and-return-a (assoc-in (machine data) [:registers :a] 12)))
  (println "Answer2:" ans2))
