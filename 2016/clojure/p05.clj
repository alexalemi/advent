(ns p05)

(import 'java.security.MessageDigest
        'java.math.BigInteger)

(defn md5 [^String s]
  (let [algorithm (MessageDigest/getInstance "MD5")
        raw (.digest algorithm (.getBytes s))]
    (format "%032x" (BigInteger. 1 raw))))


(def salt "uqwqemis")

(comment
  ; (first (filter #(= "00000" (take 5 %)) (map (comp md5 #(str salt %)) (range))))
  (first (filter #(= "00000" (take 5 %)) (map (comp md5 #(str "abc" %)) (range)))))
