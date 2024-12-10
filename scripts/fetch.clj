(ns fetch
  (:require [babashka.curl :as curl]
            [clojure.string :as str]
            [babashka.fs :as fs]
            [cheshire.core :as json]
            [clojure.tools.logging :as log]
            [clojure.java.io :as io]))


(def ROOT "https://adventofcode.com/")
(def TOKEN (str/trim (slurp ".token")))
(def DZ (java.time.ZoneId/of "America/New_York"))
(def USER-AGENT "github.com/alexalemi/advent by alexalemi@gmail.com")
(def LEADERBOARDS (map read-string (str/split-lines (slurp ".leaderboards"))))
(def LEADERBOARD-DATA "scripts/.leaderboards.json")

(def RENAME
  {"runatme" "Matt"
   "pleonasticperson" "Colin"
   "Sal Gerace" "Sal"
   "Alex Alemi" "Me"
   "Zachary Gerace" "Zach"
   "Andy Bohn" "Andy"
   "Oliver Alemi" "Anax"
   "John Stout" "John"
   "rwharton" "Corky"
   "Colin Clement" "Colin"
   "back2bakula" "Corky"})

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
                                "User-Agent" USER-AGENT} :as :bytes}))
         (io/file (str flname)))
        (let [size (.length (io/file (str flname)))]
          (log/info (format "Got %s with %d bytes" flname size))))))))


(defn fetch-leaderboard
  "Get the data for a leaderboard."
  ([pk year]
   (log/info (format "Requesting the leaderboard for %d @ %d" year pk))
   (let [url (format "https://adventofcode.com/%d/leaderboard/private/view/%d.json" year pk)
         response (curl/get url {:headers {"Cookie" (str "session=" TOKEN) "User-Agent" USER-AGENT}})]
     (if (not= 200 (response :status))
       (log/error "There was an error asking for leaderboard" pk " from year=" year " got status code " (response :status))
       (-> response
           :body
           (json/parse-string true)))))
  ([pk] ()
    (let [{:keys [year month day]} (parse-datetime (now))]
       (fetch-leaderboard pk year))))


(defn update-leaderboards
  "Update the leaderboards data"
  {:org.babashka/cli {:coerce {:year :int}
                      :alias {:y :year}}}
  [m]
  (log/info "Updating the leaderboards...")
  (let [{:keys [year] :or {year (:year (parse-datetime (now)))}} m
        fetcher (fn [x] (fetch-leaderboard x year))
        data (into {} (map (juxt :owner_id identity) (map fetcher LEADERBOARDS)))]
    (spit LEADERBOARD-DATA (json/generate-string data))))

(def HALF-STAR "⯨")
(def STAR "★")

(defn format-duration [ts now]
  (let [diff (- now ts)
        days (quot diff (* 24 60 60))
        hours (quot (mod diff (* 24 60 60)) (* 60 60))
        minutes (quot (mod diff (* 60 60)) 60)
        seconds (mod diff 60)]
    (cond
      (pos? days) (format "%d days %d hours ago" days hours)
      (pos? hours) (format "%d hours %d minutes ago" hours minutes)
      (pos? minutes) (format "%d minutes %d seconds ago" minutes seconds)
      :else (format "%d seconds ago" seconds))))

(defn format-event [[ts who day part]]
  (let [day (read-string (name day))
        part (read-string (name part))]
    (format "%9s got star %02d-%02d %s" who day part (format-duration ts (.toEpochSecond (now))))))

(defn show-leaderboard
  "Print out the combined leaderboard" 
  [m]
  (log/info "Showing leaderboards...")
  (let [data (json/parse-string (slurp LEADERBOARD-DATA) true)
        data (->> data
                  (vals)
                  (map :members)
                  (into {})
                  (map (fn [[a b]] {(RENAME (b :name)) 
                                    (b :completion_day_level)})) 
                  (apply merge-with into))
        stars (fn [who] 
                (let [x (data who)]
                  (apply str 
                   (for [i (range 1 25)]
                    (cond 
                     (get-in x [(keyword (str i)) :2]) STAR
                     (get-in x [(keyword (str i)) :1]) HALF-STAR
                     :else " ")))))
         line (fn [who] (format "%9s: %s" who (stars who)))
         extract-events (fn [[who puzzles]]
                          (mapcat (fn [[puzzle-num parts]]
                                    (mapcat (fn [[part {:keys [get_star_ts]}]]
                                              [[get_star_ts who puzzle-num part]])
                                            parts))
                                  puzzles))
         recent-events (take 15 (sort-by first > (mapcat extract-events data)))]
      (println "\n***LEADERBOARD***")
      (println "                    1111111111222222")
      (println "           1234567890123456789012345")
      (println  (str/join "\n" (map line (keys (sort-by (fn [[a b]] (- (count b))) data)))))
      (println)
      (println "RECENT EVENTS:")
      (println (str/join "\n" (map format-event recent-events)))))


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




