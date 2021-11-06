(ns carlisle.config
  (:gen-class)
  (:require [clojure.data.json :as json] 
            [clojure.tools.logging :as log]))

(def app-info (atom nil))

(def config (try
              (log/info "Loading config...")
              (json/read-str (slurp "./config.json") :key-fn keyword)
              (catch Exception e 
                (log/warn "Couln't load config! (I hope we're in CI/CD)")
                {})))

(log/info "Done.")
