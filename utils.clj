(ns utils
  (:require [babashka.curl :as curl]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]
            [babashka.cli :as cli]))


(def ROOT "https://adventofcode.com/")
(def TOKEN (str/trim (slurp ".token")))
(def DZ (java.time.ZoneId/of "America/New_York"))

(defn now [] (java.time.ZonedDateTime/now DZ))

(defn datetime [year month day] (.atStartOfDay (java.time.LocalDate/of year month day) DZ))

(defn parse-datetime [dt]
  {:year (.getYear dt)
   :month (.getValue (.getMonth dt))
   :day (.getDayOfMonth dt)})

(defn get-input
  "Download the input file for a given year and day."
  ([]
   (let [{:keys [year month day]} (parse-datetime (now))]
     (if (= month 12)
       (get-input year day)
       (log/error "It isn't advent! Use command line arguments for the year and day."))))
  ([year day]
   (let [flname (fs/path (str year) "input" (format "%02d.txt" day))]
    (if (fs/exists? flname)
      (log/info (format "Input file %s already exists!" flname))
      (do
        (log/info (format "Fetching %s..." flname))
        (io/copy
         (:body (curl/get (str ROOT (format "%d/day/%d/input" year day))
                     {:headers {"Cookie" (str "session=" TOKEN)
                                "User-Agent" "https://git.alexalemi.com/alemi/advent/src/master/utils.clj by alexalemi@gmail.com"} :as :bytes}))
         (io/file (str flname)))
        (let [size (.length (io/file (str flname)))]
          (log/info (format "Got %s with %d bytes" flname size))))))))


(defn fetch
  "Meant for use from the command line, takes a dictionary as input."
  {:org.babashka/cli {:coerce {:year :int :day :int}
                      :alias {:y :year :d :day}}}
  [m]
  (if (empty? m)
    (get-input)
    (let [{:keys [year day] :or {year (:year (parse-datetime (now)))}} m
          right-now (now)]
      (if day
        (if (.isAfter right-now (datetime year 12 day))
          (get-input year day)
          (log/error (format "You requested a %d/%d, a day in the future!" year day)))
        (log/error "You must call with a --day [-d] argument.")))))
