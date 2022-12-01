(require '[nextjournal.clerk :as clerk])

;; start Clerk's built-in webserver on the default port 7777, opening the browser when done
; (clerk/serve! {:browse? true})

;; either call `clerk/show!` explicitly
; (clerk/show! "notebooks/rule_30.clj")

;; or let Clerk watch the given ":paths" for changes
(clerk/serve! {:watch-paths ["."]})

;; start with watcher and show filter function to enable notebook pinning
; (clerk/serve! {:watch-paths ["."]})


(comment
  (clerk/build-static-app! {:paths ["p15.clj"]})

  (clerk/build! {:paths ["p15.clj"]})

  (+ 1 2))
