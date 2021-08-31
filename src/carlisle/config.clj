(ns carlisle.config
  (:gen-class)
  (:require [clojure.data.json :as json] 
            [clojure.tools.logging :as log]))

(do
  (def config (json/read-str (slurp  "./config.json") :key-fn keyword))
  (log/info "Config loaded"))
