(ns advent23
  (:require
   [clojure.test :as test]
   [clojure.string :as str]))


(def data-string (slurp "../input/23.txt"))

(defn keywordize [x]
  (if (number? x) x (keyword (quote x))))

(defn reader [line]
   (let [form (read-string (str "(" line ")"))
         call (first form)
         args (rest form)]
     (cons call (map keywordize args))))

(def foo (first (let [s data-string]
                  (map reader (str/split-lines s)))))
