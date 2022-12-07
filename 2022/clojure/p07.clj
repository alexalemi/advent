;; # 🎄 Advent of Code 2022 - Day 7
(ns p07
  (:require [clojure.edn :as edn]
            [clojure.string :as str]
            [util :as util]
            [clojure.walk :as walk]))

(def data-string (slurp "../input/07.txt"))

(defn process-data [s]
  (rest (str/split s #"\n\$ ")))

(def data (process-data data-string))

(def test-string "$ cd /
$ ls
dir a
14848514 b.txt
8504156 c.dat
dir d
$ cd a
$ ls
dir e
29116 f
2557 g
62596 h.lst
$ cd e
$ ls
584 i
$ cd ..
$ cd ..
$ cd d
$ ls
4060174 j
8033020 d.log
5626152 d.ext
7214296 k")

(def test-data (process-data test-string))

;; ## Building the directory tree
;; Some function to build up the directory tree, which I don't actually need in the end.
(defn consume-one [fs listing]
  (if (str/starts-with? listing "dir")
    (assoc fs (subs listing 4) {})
    (let [[size name] (str/split listing #" ")]
      (assoc fs name (parse-long size)))))

(defn consume [fs listings]
  (reduce consume-one fs listings))

(defn process [state command]
  (cond
    (str/starts-with? command "cd /") (assoc state :pwd nil)
    (str/starts-with? command "cd ..") (update state :pwd butlast)
    (str/starts-with? command "cd") (update state :pwd conj (subs command 3))
    :else (update-in state
               (into [:fs] (:pwd state))
               consume
               (rest (str/split-lines command)))))


(def EMPTY {:fs {} :pwd []})

(def test-fs (reduce process EMPTY test-data))

(defn accumulate-one [pwd sizes listing]
  (if (str/starts-with? listing "dir")
    (assoc sizes (conj pwd (subs listing 4)) 0)
    (let [[size _] (str/split listing #" ")]
      (loop [sizes sizes
             pwd pwd]
        (if (seq pwd)
          (recur
            (update sizes pwd + (parse-long size))
            (rest pwd))
          sizes)))))

(defn process-size [state command]
  (cond
    (str/starts-with? command "cd /") (assoc state :pwd (list "/"))
    (str/starts-with? command "cd ..") (update state :pwd rest)
    (str/starts-with? command "cd") (update state :pwd conj (subs command 3))
    :else (assoc state :sizes (reduce (partial accumulate-one (:pwd state)) (:sizes state) (rest (str/split-lines command))))))

(defn directory-sizes [commands]
  (let [result (reduce process-size {:sizes {'("/") 0} :pwd '("/")} commands)]
     (:sizes result)))

(defn sum-large-directories [commands]
  (let [sizes (directory-sizes commands)]
    (transduce
     (filter (fn [x] (<= x 100000)))
     +
     (vals sizes))))

(sum-large-directories test-data)

(def ans1 (sum-large-directories data))

(defn directory-to-delete [commands]
  (let [sizes (directory-sizes commands)
        total-size (sizes '("/"))
        free-space (- 70000000 total-size)
        needed-space (- 30000000 free-space)]
    (->> sizes
         (map second)
         (filter (fn [size] (>= size needed-space)))
         (sort)
         first)))

(directory-to-delete test-data)

(def ans2 (directory-to-delete data))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))
