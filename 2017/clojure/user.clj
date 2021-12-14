(require '[nextjournal.clerk :as clerk])
(require '[clojure.string])

;; start Clerk's buit-in webserver on the default port 7777, opening the browser when done
(clerk/serve! {:browse? true})

;; either call `clerk/show!` explicitly
(clerk/show! "notebooks/rule_30.clj")

;; or let Clerk watch the given `:paths` for changes
(clerk/serve! {:watch-paths ["notebooks" "src"]})

;; start with watcher and show filter function to enable notebook pinning
(clerk/serve! {:watch-paths ["notebooks" "src"] :show-filter-fn #(clojure.string/starts-with? % "notebooks")})
