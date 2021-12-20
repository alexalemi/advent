(ns advent20
  (:require
   [clojure.string :as str]
   [clojure.test :as test]))

(def data-string (slurp "../input/20.txt"))
(def test-string (slurp "../input/20test.txt"))

(def status {\# true \. false})

(defn enumerate [coll]
  (zipmap (range) coll))

(defn process [s]
  (let [lines (str/split-lines s)
        buffer (first lines)
        image (rest (rest lines))
        lookup (mapv status buffer)
        board (for [[row line] (enumerate image)
                    [coll c] (enumerate line)
                    :when (status c)]
                [row coll])]
    {:lookup lookup
     :store true
     :new-store not
     :board (set board)}))

(def data (process data-string))
(def test-data (assoc (process test-string)
                      :new-store (constantly true)))

(def binary {true 1 false 0})

(defn read-bits
  "Convert a bit-stream to an integer."
  [bits]
  (loop [bits bits x 0]
    (let [bit (first bits)]
      (if (empty? bits) x
          (recur
           (rest bits)
           (bit-or (bit-shift-left x 1) (binary (boolean bit))))))))

(defn neighborhood [loc]
  (let [[row col] loc]
    [[(dec row) (dec col)]
     [(dec row) col]
     [(dec row) (inc col)]
     [row (dec col)]
     [row col]
     [row (inc col)]
     [(inc row) (dec col)]
     [(inc row) col]
     [(inc row) (inc col)]]))

(defn new-state [data loc]
  (let [{:keys [lookup board store new-store]} data
        neighbors (neighborhood loc)
        getter (if store board (complement board))
        id (read-bits (map getter neighbors))
        next-store (new-store store)
        lookuper (if next-store lookup (complement lookup))]
    (lookuper id)))

(defn possible-locs
  "Locations that might turn on."
  [board]
  (set (apply concat (map neighborhood board))))

(defn step [data]
  (let [{:keys [board new-store]} data
        locs (possible-locs board)
        tester (partial new-state data)
        data (update data :store new-store)]
    (assoc data :board (set (filter tester locs)))))

(defn part-1 [data]
  (count (:board (nth (iterate step data) 2))))

(test/deftest test-part-1
  (test/is (= (part-1 test-data) 35)))

(time (def ans1 (part-1 data)))
(println)
(println "Answer 1:" ans1)

(defn part-2 [data]
  (count (:board (nth (iterate step data) 50))))

(test/deftest test-part-2
  (test/is (= (part-2 test-data) 3351)))

(time (def ans2 (part-2 data)))
(println)
(println "Answer 2:" ans2)

(test/run-tests)
