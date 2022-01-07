(ns carlisle.config
  (:gen-class)
  (:require [clojure.data.json :as json] 
            [clojure.tools.logging :as log]))

(def app-info nil)

(def config (try
              (log/info "Loading config...")
              (clojure.edn/read-string (slurp "./config.edn"))
              (catch Exception e 
                (log/warn "Couln't load config! (I hope we're in CI/CD)")
                {})))

(log/info "Done.")
