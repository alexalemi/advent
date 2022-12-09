;; # 🎄 Advent of Code 2022 - Day 7
(ns p07
  (:require [clojure.string :as str]
            [clojure.test :as test]
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

(defn accumulate-one
  "Accumulate the sizes for a single listing."
  [pwd sizes listing]
  (if (str/starts-with? listing "dir")
    ;; A new directory, initialize to 0
    (assoc sizes (conj pwd (subs listing 4)) 0)
    (let [[size _] (str/split listing #" ")]
      ;; For all of the parents, update the size
      (loop [sizes sizes pwd pwd]
        (if (seq pwd)
          (recur
           (update sizes pwd + (parse-long size))
           (pop pwd))
          sizes)))))

(defn process-size
  "Update our sizes with a single command."
  [state command]
  (condp (fn [t s] (str/starts-with? s t)) command
    "cd /" (assoc state :pwd ["/"])
    "cd .." (update state :pwd pop)
    "cd" (update state :pwd conj (subs command 3))
    ;; ls
    (assoc state :sizes (reduce (partial accumulate-one (:pwd state)) (:sizes state) (rest (str/split-lines command))))))

(defn directory-sizes
  "Compute all of the directory sizes."
  [commands]
  (:sizes (reduce process-size
                  {:sizes {["/"] 0} :pwd ["/"]}
                  commands)))

(defn sum-small-directories
  "Find the sum of all of the small directories."
  [commands]
  (let [sizes (directory-sizes commands)]
    (transduce
     (filter (fn [x] (<= x 100000)))
     +
     (vals sizes))))

(test/deftest test-part-1
  (test/is 95437 (sum-small-directories test-data)))

(def ans1 (sum-small-directories data))

;; ## Part 2
(defn directory-to-delete [commands]
  (let [sizes (directory-sizes commands)
        total-size (sizes '("/"))
        free-space (- 70000000 total-size)
        needed-space (- 30000000 free-space)]
    (transduce
     (comp
      (map second)
      (filter (fn [size] (>= size needed-space))))
     min
     ##Inf
     sizes)))

(test/deftest test-part-2
  (test/is 24933642 (directory-to-delete test-data)))

(def ans2 (directory-to-delete data))

;;

(defn -test [_]
  (test/run-tests 'p07))

(defn -main [_]
  (println "Answer1:" ans1)
  (println "Answer2:" ans2))

;; Building Directory Tree
;;
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
    (str/starts-with? command "cd ..") (update state :pwd pop)
    (str/starts-with? command "cd") (update state :pwd conj (subs command 3))
    :else (update-in state
                     (into [:fs] (:pwd state))
                     consume
                     (rest (str/split-lines command)))))

(def test-fs (reduce process {:fs {} :pwd []} test-data))

(def data-fs (reduce process {:fs {} :pwd []} data))

;; This will annotate each directory with its own size.
(def data-fs-with-sizes
  (walk/postwalk (fn [x] (if (map? x)
                           (assoc x :size
                                  (reduce-kv
                                   (fn [tot k v] (+ tot (if (map? v) (:size v) v)))
                                   0 x))
                           x))
                 (:fs test-fs)))
