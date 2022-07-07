(ns carlisle.commands.dead-by-daylight
  (:gen-class)
  (:use [carlisle.utils.basic :only [make-basic-embed]])
  (:require [camel-snake-kebab.core :as csk]
            [clojure.data.json :as json])
  (:import [net.dv8tion.jda.api EmbedBuilder]
           [net.dv8tion.jda.api.interactions.commands Command OptionType]
           [net.dv8tion.jda.api.interactions.commands.build Commands OptionData]))
 

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
  [perk]
  (.. (make-basic-embed)
      (setTitle (perk :perk_name))
      (setDescription (perk :description))
      (setThumbnail (perk :icon))
      (build)))

(defn build-perk-roulette-embeds
  [perks]
  (for [perk (->> perks
                 (shuffle)
                 (take 4))] 
    (build-perk-embed perk)))

;;;;;;;;;;;;;;;;;;;;;;;;;
;; actual command junk ;;
;;;;;;;;;;;;;;;;;;;;;;;;;

(def dead-by-daylight-command-data
  (.. (Commands/slash "dead-by-daylight" "Various commands related to Dead by Daylight")
      (addOptions [(.. (OptionData. OptionType/STRING "role" "Who are you playing as?" true)
                       (addChoice "killer" "killer")
                       (addChoice "survivor" "survivor"))
                   (OptionData. OptionType/BOOLEAN
                                "show-everyone"
                                "Default: False.")])))
      
(defn dead-by-daylight-command 
  [event]
  (let [perks (case (.. event (getOption  "role") (getAsString))
                "survivor" sperks
                "killer" kperks)
        ephemeral? (if-some [option (.. event (getOption "show-everyone"))]
                     (not (.. option getAsBoolean))
                     true)]
    (.. event
        (replyEmbeds (build-perk-roulette-embeds perks))
        (setEphemeral ephemeral?)
        (queue))))
