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
(def baro-img "https://static.wikia.nocookie.net/warframe/images/a/a7/TennoCon2020BaroCropped.png")
    
(defn void-trader-embed-inactive
  "Returns a set (of one) embed detailing Baro's status.
  a set is used because a collection is expected"
  [event]
  #{ (.. (build-basic-embed event)
           (setTitle "The Void Trader isn't here...")
           (setThumbnail baro-img)
           (setDescription (str "he will arrive in " (-> @worldstate :void-trader :start-string)))
           (build))})

(defn void-trader-embed-active-template
  [event]
  (let [inventory (-> @worldstate :void-trader :inventory)]
    (-> (build-basic-embed event)
        (.setTitle "The Void Trader is here!")
        (.setThumbnail baro-img)
        (.setDescription (str "he will leave in " (-> @worldstate :void-trader :end-string))))))

(defn void-trader-embed-partition
  "Embeds may only hold 25 fields, so this builds a (up to) 25 field partition of the inventory"
  [event inventory-partition]
  (let [embed (void-trader-embed-active-template event)
        ducats-emote (.. event getJDA (getEmoteById 664151434765533206))
        credits-emote (.. event getJDA (getEmoteById  664574338342846464))]
    (doseq [item inventory-partition]
      (.. embed (addField (item :item) 
                          (format "%s%s %s%s" 
                                  (item :ducats) 
                                  ducats-emote
                                  (item :credits)
                                  credits-emote)
                          true)))
      (.build embed)))

(defn void-trader-full-inventory
  "builds as many embeds as will be needed to represent Baro's inventory, returning a set"
  [event]
  (set (for [part (partition-all 25 (-> @worldstate :void-trader :inventory))]
         (void-trader-embed-partition event part))))

(defn build-void-trader-embeds [event]
  (if (-> @worldstate :void-trader :active)
    (void-trader-full-inventory event)
    (void-trader-embed-inactive event)))

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
