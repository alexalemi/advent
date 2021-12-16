(ns advent16
  (:require
   [clojure.test :as test]))

(def data-string (slurp "../input/16.txt"))
(def test-string "D2FE28")

(defn byte-to-bits [b]
  (for [i (range 7 -1 -1)]
    (bit-test b i)))

(def binary {true 1 false 0})

(defn to-byte-array [s]
  (->> (seq s)
       (partition 2)
       (map (fn [[a b]] (str "0x" a b)))
       (map read-string)
       (byte-array)))

(defn bit-stream [s]
  (flatten (map byte-to-bits (to-byte-array s))))

(defn show-bits [bits]
  (apply str "2r" (map binary bits)))

(defn read-bits [bits]
  (if (empty? bits) nil
      (read-string (apply str "2r" (map binary bits)))))

(defn to-hex [x]
  (format "%x" x))

(defn read-meta [stream]
  (let [[v stream] (split-at 3 stream)
        [t stream] (split-at 3 stream)]
    [{:version (read-bits v)
      :type (read-bits t)} stream]))

(defn read-literal [stream]
  (loop [stream stream
         bits []]
    (let [[group stream] (split-at 5 stream)
          more (first group)
          bits (concat bits (rest group))]
      (if more
        (recur stream bits)
        [{:value (read-bits bits)} stream]))))

(declare read-packet)

(defn read-packets [stream]
  (loop [stream stream
         packets []]
    (if (empty? stream)
      packets
      (let [[packet stream] (read-packet stream)]
        (recur stream (conj packets packet))))))

(defn read-n-packets
  ([num stream] (read-n-packets num stream []))
  ([num stream packets]
   (loop [num num
          stream stream
          packets packets]
     (if (pos? num)
       (let [[packet stream] (read-packet stream)]
         (recur (dec num)
                stream
                (conj packets packet)))
       [{:children packets} stream]))))

(defn combine [data [data2 stream]]
  [(merge data data2) stream])

(defn read-operator [stream]
  (let [length-type-id (first stream)
        stream (rest stream)]
    (if length-type-id
      (let [[num-packet-bits stream] (split-at 11 stream)
            num-packets (read-bits num-packet-bits)]
        (combine {:sub-packets num-packets} (read-n-packets num-packets stream)))
      (let [[total-length-bits stream] (split-at 15 stream)
            total-length (read-bits total-length-bits)
            [packet-stream stream] (split-at total-length stream)]
        [{:packet-length total-length :children (read-packets packet-stream)} stream]))))

(defn read-packet [stream]
  (let [[data stream] (read-meta stream)
        t (:type data)]
    (if (= t 4) 
      (combine data (read-literal stream))
      (combine data (read-operator stream)))))

(defn read-packet-from-string [s]
  (read-packet (bit-stream s)))

(defn total-version [packet]
  (let [version (:version packet)
        children (:children packet)]
    (apply + version (map total-version children))))

(defn part-1 [s]
  (total-version (first (read-packet-from-string s))))

(test/deftest test-part-1
  (test/are [x y] (= (part-1 x) y)
    "8A004A801A8002F478" 16
    "620080001611562C8802118E34" 12
    "C0015000016115A2E0802F182340" 23
    "A0016C880162017C3686B18A3D4780" 31))

(time (def ans1 (part-1 data-string)))
(println)
(println "answer 1:" ans1)

(defn to-int [x]
  (if (boolean? x) (binary x) x))

(def operation-lookup {0 + 1 * 2 min 3 max 5 > 6 < 7 =})

(defn evaluate [packet]
  (let [{:keys [type value children]} packet]
    (if (= type 4) value
      (to-int (apply (operation-lookup type) (map evaluate children))))))

(defn part-2 [s]
  (evaluate (first (read-packet-from-string s))))

(test/deftest test-part-2
  (test/are [s v] (= (part-2 s) v)
    "C200B40A82" 3
    "04005AC33890" 54
    "880086C3E88112" 7
    "CE00C43D881120" 9
    "D8005AC2A8F0" 1
    "F600BC2D8F" 0
    "9C005AC2F8F0" 0
    "9C0141080250320F1802104A08" 1))

(time (def ans2 (part-2 data-string)))
(println)
(println "answer 2:" ans2)

(test/run-tests)
