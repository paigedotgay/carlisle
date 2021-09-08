(ns carlisle.commands.warframe
  (:gen-class)
  (:use [carlisle.config :only [config]] 
        [carlisle.util]
        [clojure.java.javadoc])
  (:require [camel-snake-kebab.core :as csk]
            [clojure.core.async :as a] 
            [clojure.data.json :as json]
            [clojure.data.xml :as xml]
            [clojure.tools.logging :as log]
            [clojure.string :as str])
  (:import [java.io ByteArrayOutputStream PrintStream PrintWriter OutputStreamWriter]
           [net.dv8tion.jda.api EmbedBuilder]
           [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.events.message MessageReceivedEvent]
           [net.dv8tion.jda.api.entities ChannelType]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components.selections SelectionMenu]))

(def worldstate (atom nil))

(defn start-api-updates []
  (a/go-loop []
    (a/<! (a/timeout 10000))
    (log/info "Refreshing Warframe API...")
    (reset! worldstate (json/read-str (slurp "https://api.warframestat.us/pc") :key-fn csk/->kebab-case-keyword))
    (recur)))

(def warframe-command-data
  (-> (CommandData. "warframe" "gets information about Warframe PC Worldstate")
      (.addOption OptionType/STRING "key" "The topic you need information about" false)))

(def warframe-command-menu
  (-> (SelectionMenu/create "warframe-menu")
      (.setPlaceholder "What do you need information about?")
      (.setRequiredRange 1 1)
      (.addOption "Void Trader" "void-trader")
      (.build)))

(defn fuzzy-matcher 
  [key]
  (case (str/replace (str/lower-case key) #"[^a-z]" "")
    ("voidtrader" "baro") :void-trader
    nil))

 (defn build-void-trader-embed [event]
   (let [avatar (.. event getJDA (getUserById (config :owner)) getAvatarUrl)
         time ((@worldstate :void-trader) :start-string)]
     (-> (EmbedBuilder.)
         (.setTitle "baro embed test")
         (.setDescription (str "he will arrive in " time))
         (.setFooter "<3#3333 made this (◍•ᴗ•◍)" avatar)
         (.setThumbnail "https://static.wikia.nocookie.net/warframe/images/a/a7/TennoCon2020BaroCropped.png")
         (.build))))

(def warframe-command-menu-listener
  (proxy [ListenerAdapter] []
    (onSelectionMenu [event]
      (let [key (-> (.getValues event)
                    (first)
                    (keyword))]
            (.. event getMessage delete queue)
            (.. event
                (replyEmbeds [(build-void-trader-embed event)])
                (setEphemeral true)
                (queue))))))
      
(defn warframe-command 
  [event]
  (if (zero? (count (.getOptions event)))
    (.. event (reply "What information do you need?") 
        (setEphemeral false)
        (addActionRow [warframe-command-menu])
        (queue))
    (-> event 
        (.replyEmbeds [(build-void-trader-embed event)])
        (.setEphemeral true)
        (.queue))))

;(start-api-updates)
