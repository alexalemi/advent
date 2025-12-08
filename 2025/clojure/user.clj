(ns user
  (:require [nextjournal.clerk :as clerk]))

;; start Clerk's built-in webserver on the default port 7777, opening the browser when done
; (clerk/serve! {:browse? true})

;; either call `clerk/show!` explicitly
; (clerk/show! "notebooks/rule_30.clj")

;; or let Clerk watch the given ":paths" for changes
(comment
  (clerk/serve! {:host "0.0.0.0" :port 7777 :watch-paths ["."]}))

(comment
  (clerk/show! 'nextjournal.clerk.tap))
;; start with watcher and show filter function to enable notebook pinning
; (clerk/serve! {:watch-paths ["."]})

(comment
  (clerk/build! {:paths ["p*.clj"]})

  (clerk/clear-cache!))
