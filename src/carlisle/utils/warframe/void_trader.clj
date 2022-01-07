(ns carlisle.utils.warframe.void-trader
  (:gen-class)
  (:use [carlisle.utils.basic]
        [carlisle.config]) 
  (:require [clojure.string :as str]))

(def baro-img "https://static.wikia.nocookie.net/warframe/images/a/a7/TennoCon2020BaroCropped.png")

(defn void-trader-embed-inactive
  "Returns a set (of one) embed detailing Baro's status.
  a set is used because a collection is expected"
  [worldstate]
  #{(.. (make-basic-embed)
        (setTitle "The Void Trader isn't here...")
        (setThumbnail baro-img)
        (setDescription (str "he will arrive in " (-> worldstate :void-trader :start-string)))
        (build))})

(defn void-trader-embed-active-template
  [worldstate]
  (let [inventory (-> worldstate :void-trader :inventory)]
    (.. (make-basic-embed)
        (setTitle "The Void Trader is here!")
        (setThumbnail baro-img)
        (setDescription (str "he will leave in " (-> worldstate :void-trader :end-string))))))

(defn void-trader-embed-partition
  "Embeds may only hold 25 fields, so this builds a (up to) 25 field partition of the inventory"
  [inventory-partition worldstate]
  (let [embed (void-trader-embed-active-template worldstate)
        ducats-emote (.. app-info getJDA (getEmoteById (-> config :emotes :ducats)))
        credits-emote (.. app-info getJDA (getEmoteById (-> config :emotes :credits)))]
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
  [worldstate]
  (set (for [part (partition-all 25 (-> worldstate :void-trader :inventory))]
         (void-trader-embed-partition part))))

(defn build-void-trader-embeds [event worldstate]
  (if (-> worldstate :void-trader :active)
    (void-trader-full-inventory worldstate)
    (void-trader-embed-inactive worldstate)))
