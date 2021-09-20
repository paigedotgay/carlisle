(ns carlisle.warframe.utils
  (:gen-class)
  (:use [carlisle.config :only [config]]
        [carlisle.utils]) 
  (:require [camel-snake-kebab.core :as csk]
            [clojure.core.async :as async] 
            [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]
            [clojure.string :as str])
  (:import [java.io ByteArrayOutputStream PrintStream PrintWriter OutputStreamWriter]
           [net.dv8tion.jda.api EmbedBuilder]))

(def worldstate (atom nil))

(defn build-void-trader-embed [event]
  (-> (build-basic-embed event)
      (.setTitle "baro embed test")
      (.setDescription (str "he will arrive in " ((@worldstate :void-trader) :start-string)))
      (.setThumbnail "https://static.wikia.nocookie.net/warframe/images/a/a7/TennoCon2020BaroCropped.png")
      (.build)))

(defn fuzzy-matcher 
  [key]
  (case (str/replace (str/lower-case key) #"[^a-z]" "")
    ("voidtrader" "baro") :void-trader
    nil))

(defn format-expiry [units]
  (str/join " " 
            (for [unit units] 
              (if (pos? (val unit)) 
                (str (val unit) \space (name (key unit)))))))

(defn start-api-updates []
  (async/go-loop []
    (async/<! (async/timeout 10000))
    (log/info "Refreshing Warframe API...")
    (try 
      (reset! worldstate (-> (slurp "https://api.warframestat.us/pc")
                             (json/read-str :key-fn csk/->kebab-case-keyword)))
      (catch Exception e (log/warn (format "Could not update API%nException: %s%nCause: %s"
                                           (.getMessage e)
                                           (.getCause e)))))
    (recur)))
