(ns advent16
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))

(def test-string "s1,x3/4,pe/b")
(def data-string (slurp "../input/16.txt"))

(defn spin [amount state]
  (let [n (count state)]
    (into [] (take n (drop (- n amount) (cycle state))))))

(defn exchange [locs state]
  (let [[from upto] locs
        a (state from)
        b (state upto)]
    (assoc (assoc state from b) upto a)))

(defn partner [which state]
  (let [[from upto] which
        a (.indexOf state from)
        b (.indexOf state upto)]
    (exchange [a b] state)))

(defn process-command [s]
  (let [kind (subs s 0 1)
        part (subs s 1)]
    (case kind
      "s" [:spin (read-string part)]
      "x" [:exchange (map read-string (str/split part #"/"))]
      "p" [:partner (map keyword (str/split part #"/"))])))

(defn command-to-fun [command]
  (let [[kind parts] command]
    (case kind
      :spin (partial spin parts)
      :exchange (partial exchange parts)
      :partner (partial partner parts))))

(def init-state [:a :b :c :d :e :f :g :h :i :j :k :l :m :n :o :p])

(def dance
  (->> (str/split data-string #",")
       (map process-command)
       (map command-to-fun)
       reverse
       (apply comp)))

(defn do-dance [s state]
  (let [dance (->> (str/split s #",")
                   (map process-command)
                   (map command-to-fun)
                   reverse
                   (apply comp))]
    (apply str (map name (dance state)))))

(time (def ans1
        (apply str (map name (dance init-state)))))

(println)
(println "Answer 1:" ans1)

(defn find-repeat [f init]
  (let [seq (iterate f init)
        x0 (first seq)]
    (reduce (fn [acc val] (if (= val x0) (reduced (inc acc)) (inc acc))) 0 (drop 1 seq))))

(time (def ans2
        (let [cycle-length (find-repeat dance init-state)
              remainder (mod 1000000000 cycle-length)]
          (apply str (map name (nth (iterate dance init-state) remainder))))))

(println)
(println "Answer 2:" ans2)


