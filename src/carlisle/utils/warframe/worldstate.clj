(ns carlisle.utils.warframe.worldstate
  (:gen-class)
  (:use [carlisle.utils.basic]
        [carlisle.config]) 
  (:require [camel-snake-kebab.core :as csk]
            [clojure.core.async :as async] 
            [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [clojure.tools.logging :as log])
  (:import [java.io ByteArrayOutputStream PrintStream PrintWriter OutputStreamWriter]
           [net.dv8tion.jda.api EmbedBuilder]))

(def worldstate (atom nil))

(defn start-api-updates []
  (async/go-loop []
    (async/<! (async/timeout 10000))
    (log/debug "Refreshing Warframe API...")
    (try 
      (reset! worldstate (-> (slurp "https://api.warframestat.us/pc")
                             (json/read-str :key-fn csk/->kebab-case-keyword)))
      (catch Exception e (log/warn (format "Could not update API%nException: %s%nCause: %s"
                                           (.getMessage e)
                                           (.getCause e)))))
    (recur)))

(start-api-updates)
