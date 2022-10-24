(ns advent.utils
  (:require [babashka.curl :as curl]
            [clojure.string :as str]))


; (def ROOT "https://adventofcode.com/{year}/leaderboard/private/view/{num}.json")
(def ROOT "https://adventofcode.com/2021/leaderboard/private/view/173774.json")

(def TOKEN (str/trim (slurp "token.txt")))

(def resp (curl/get ROOT {:headers {"Cookie" (str "session=" TOKEN "")}}))
