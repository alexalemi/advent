(ns advent16
  (:require
   [clojure.test :as test]))

(def data-string (slurp "../input/16.txt"))

(defn byte-to-bits
  "Read the bits from a byte."
  [b] (for [i (range 7 -1 -1)]
        (bit-test b i)))

(def binary
 "Convert booleans to integers."
 {true 1 false 0})

(defn to-byte-array
 "Convert a string to a byte-array." 
  [s] (->> (seq s)
           (partition 2)
           (map (fn [[a b]] (str "0x" a b)))
           (map read-string)
           (byte-array)))

(defn bit-stream
  "Convert a hexadecimal string to a sequence of bits."
  [s] (flatten (map byte-to-bits (to-byte-array s))))

(defn show-bits
  "Show the bits in a bit stream."
  [bits] (apply str "2r" (map binary bits)))

(defn to-hex
  "Convert a number to hexidecimal."
  [x] (format "%x" x))

(defn read-bits
  "Convert a bit-stream to an integer."
  [bits] 
  (loop [bits bits x 0]
    (let [bit (first bits)]
      (if (nil? bit) x
        (recur 
          (rest bits) 
          (bit-or (bit-shift-left x 1) (binary bit))))))) 
      
(defn read-meta
  "Read the metadata from a stream.
  
  Starts with 3 bits for the version and 3 more bits for the type."
  [stream]
  (let [[v stream] (split-at 3 stream)
        [t stream] (split-at 3 stream)]
    [{:version (read-bits v)
      :type (read-bits t)} stream]))

(defn read-literal
  "Read a literal from the stream.
  
  A literal comes in groups of 5 bits, the first bit
  is 1 if there are additional groups and 0 in the last group.
  The remaining four bits from each group encode the literal value."
  [stream]
  (loop [stream stream bits []]
    (let [[group stream] (split-at 5 stream)
          more (first group)
          bits (concat bits (rest group))]
      (if more
        (recur stream bits)
        [{:value (read-bits bits)} stream]))))

(declare read-packet)

(defn read-packets
  "Read off a sequence of packets from a stream." 
  [stream]
  (loop [stream stream
         packets []]
    (if (empty? stream)
      packets
      (let [[packet stream] (read-packet stream)]
        (recur stream (conj packets packet))))))

(defn read-n-packets
  "Read off exactly n packets from a stream."
  [num stream]
  (loop [num num
         stream stream
         packets []]
    (if (pos? num)
      (let [[packet stream] (read-packet stream)]
        (recur (dec num)
               stream
               (conj packets packet)))
      [{:children packets} stream])))

(defn combine
  "Combines data in a reductive fashion."
  [data [data2 stream]] [(merge data data2) stream])

(defn read-operator
  "Read a BITS operator encoding."
  [stream]
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

(defn read-packet
  "Read a single packet from the bit stream."
  [stream]
  (let [[data stream] (read-meta stream)]
    (if (= (:type data) 4) 
      (combine data (read-literal stream))
      (combine data (read-operator stream)))))

(defn read-packet-from-string
  "Read a packet from a hex string."
  [s] (read-packet (bit-stream s)))

(defn total-version
  "Compute the total version number of a nested packet."
  [packet]
  (let [version (:version packet) children (:children packet)]
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

(defn to-int
  "Converts booleans to 0, 1"
  [x] (if (boolean? x) (binary x) x))

(def operation-lookup
  "BITS operator types."
  {0 + 1 * 2 min 3 max 5 > 6 < 7 =})

(defn evaluate
  "Eval a packet."
  [packet]
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
