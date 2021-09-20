(ns carlisle.dead-by-daylight.command
  (:gen-class)
  (:use [carlisle.config :only [config]])
  (:require [camel-snake-kebab.core :as csk]
            [clojure.data.json :as json])
  (:import [net.dv8tion.jda.api EmbedBuilder]
           [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.interactions.commands OptionType Command]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components.selections SelectionMenu]))

;;;;;;;;;;;
;; perks ;;
;;;;;;;;;;;

(def perks ; All perks
  (as-> (slurp "https://dbd-api.herokuapp.com/perks") p
    (json/read-str p :key-fn keyword)
    (filter #(= "en" (% :lang)) p)))

(def sperks ; Survivor perks
  (filter #(= "Survivor" (% :role)) perks))

(def kperks ; Killer perks
  (filter #(= "Killer" (% :role)) perks))

(def aperks ; shared perks
  (filter #(= "All" (% :name)) perks))

;;;;;;;;;;;;
;; embeds ;;
;;;;;;;;;;;;

(defn build-perk-embed
  [perk event]
  (-> (EmbedBuilder.)
      (.setTitle (perk :perk_name))
      (.setDescription (perk :description))
      (.setFooter "<3#3333 made this (◍•ᴗ•◍)" (.. event getJDA (getUserById (config :owner)) getAvatarUrl))
      (.setThumbnail (perk :icon))
      (.build)))

(defn build-perk-roulette-embeds
  [perks event]
  (for [perk (->> perks
                 (shuffle)
                 (take 4))] 
    (build-perk-embed perk event)))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; actual command junk ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def dead-by-daylight-command-data
  (.. (CommandData. "dead-by-daylight" "Various commands related to Dead by Daylight")
      (addOptions [(.. (OptionData. OptionType/STRING "role" "who you're playing as" true)
                       (addChoice "killer" "killer")
                       (addChoice "survivor" "survivor"))])))
      
(defn dead-by-daylight-command 
  [event]
  (let [perks (case (.. event (getOption  "role") (getAsString))
                "survivor" sperks
                "killer" kperks)]
    (.. event
        (replyEmbeds (build-perk-roulette-embeds perks event))
        (setEphemeral true)
        (queue))))
