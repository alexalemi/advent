;; From https://github.com/genmeblog/advent-of-code/blob/master/badges/badges.bb
;; by @tschady from slack, modified

(ns badges
  (:require [babashka.curl :as curl]
            [babashka.pods :as pods]
            [clojure.string :as str]))

(pods/load-pod 'retrogradeorbit/bootleg "0.1.9")

(require '[pod.retrogradeorbit.bootleg.utils :refer [convert-to]]
         '[pod.retrogradeorbit.hickory.select :as s])


(def aoc-url "https://adventofcode.com")
(def badge-url "http://img.shields.io/static/v1")
(def icon-path "https://raw.githubusercontent.com/genmeblog/advent-of-code/master/badges/img/aoc-favicon-base64")

(def DZ (java.time.ZoneId/of "America/New_York"))
(def now (java.time.ZonedDateTime/now DZ))
(def current-year (str (.getYear now)))
(def current-day (str (.getDayOfMonth now)))

(def yrs ["2024" "2023" "2022" "2021" "2020" "2019" "2018" "2017" "2016" "2015"])

(def USER-AGENT "github.com/alexalemi/advent by alexalemi@gmail.com")
(def cookie (str/trim (slurp ".token")))
(def headers {:headers {"Cookie" (str "session=" cookie)
                        "User-Agent" USER-AGENT}})

(def badge-style
  {"color"      "00cc00" ; right side
   "labelColor" "0a0e25" ; left side
   "style"      "flat"
   "logo"       (str "data:image/png;base64," (slurp icon-path))})

(defn get-stars
  "Return a string representing number of stars earned for a given `year`"
  [year]
  (let [parsed (-> (str aoc-url year)
                   (curl/get
                    {:headers {"Cookie" (str "session=" cookie)
                               "User-Agent" USER-AGENT}})
                   :body
                   (convert-to :hickory))]
    (-> (s/select (s/class "star-count") parsed)
        first
        :content
        first
        (or "0*"))))

(defn make-badge [year stars]
  (let [params (merge {"label" year, "message" stars} badge-style)]
    (:body (curl/get badge-url {:query-params params}))))

(defn save-badge
  "Create badge with year label and star count, and save to file."
  [[label stars]]
  (let [path  (str "scripts/img/" label ".svg")
        params (merge {"label" label, "message" stars} badge-style)
        badge (:body (curl/get badge-url {:query-params params}))]
    (spit path badge)))

(defn update-badges [_]
  (let [parsed (-> (str aoc-url "/events")
                   (curl/get headers)
                   :body
                   (convert-to :hickory))
        stars (->> parsed
                   (s/select (s/class "star-count"))
                   (drop 1) ; ignore the stars listed by login
                   (mapcat :content))
        all-yrs (mapv str (reverse (range 2015 (inc (Long/valueOf current-year)))))
        yrs->stars (zipmap (conj all-yrs "Total") stars)]
    (run! save-badge yrs->stars)))
