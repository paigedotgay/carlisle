(ns carlisle.info.command
  (:gen-class)
  (:use [carlisle.config :only [config]]
        [carlisle.utils]
        [clojure.java.shell :only [sh]])
  (:require
        [clojure.string :as str])
  (:import [net.dv8tion.jda.api EmbedBuilder]
           [net.dv8tion.jda.api.interactions.commands OptionType Command]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components Button]))

(defn get-millis []
  (.getUptime (java.lang.management.ManagementFactory/getRuntimeMXBean)))

(defn map-duration [millis]
  (->>(java.time.Duration/ofMillis millis)
      (#(zipmap [:seconds :minutes :hours :days]
                [(.toSeconds %)(.toMinutes %)(.toHours %)(.toDays %)]))))

(defn largest-duration [d]
  (->> d
       (filter #(not (zero? (val %))))
       (last)
       (apply array-map)))

(defn build-info-embed [event]
  (let [bot (.. event getJDA)
        ping (.. bot getRestPing complete)
        largest (first (largest-duration (map-duration (get-millis))))
        duration (if (= 1 (val largest))
                   (str/join "" (drop-last (name (key largest))))
                   (name (key largest)))]
    (-> (build-basic-embed event)
        (.setThumbnail (.. bot getSelfUser getAvatarUrl))
        (.addField "Guilds:" (str (count (.. bot getGuilds))) true)
        (.addField "Users:" (str (count (.. bot getUsers))) true)
        (.addField "Ping:" (format "%dms" ping) true)
        (.addField "Bot Uptime:" 
                     (str (val largest) \space duration)
                   true)
        (.addField "Server Uptime" 
                   (-> (sh "uptime" "-p")
                       (:out)
                       (str/split #"( |,|\n)")
                       (rest)
                       (#(str (first  %) \space (second %))))
                   true)
        .build)))

(def info-command-data
  (.. (CommandData. "info" "Get useful bot data")
      (addOption OptionType/BOOLEAN
                 "show-everyone"
                 "would you like everyone to be able to see the result?")))
      
(defn info-command 
  [event]
  (let [ephemeral? (if-not (empty? (.. event (getOptionsByName "show-everyone")))
                     (not (.. event (getOption "show-everyone") getAsBoolean))
                     true)]
    (.. event
        (replyEmbeds #{(build-info-embed event)})
        (addActionRow #{(Button/link (protocol-link :support-server) "Join the Support Server")
                        (Button/link (config :repo) "View the Source Code")})
        (addActionRow #{(Button/link (config :bot-invite) "Invite Carlisle to Your Server")})
        (setEphemeral ephemeral?)
        (queue))))
