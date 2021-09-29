(ns carlisle.info.command
  (:gen-class)
  (:use [carlisle.config :only [config]]
        [carlisle.utils])
  (:import [net.dv8tion.jda.api EmbedBuilder]
           [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.interactions.commands OptionType Command]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components.selections SelectionMenu]))

(defn get-millis []
  (.getUptime (java.lang.management.ManagementFactory/getRuntimeMXBean)))

(defn start-formatting [millis]
  (->>
    (-> (java.time.Duration/ofMillis millis)
        (str)
        (str/split #"\D"))
    (filterv #(not (str/blank? %)))
    (reverse)
    (rest)
    (zipmap [:seconds :minutes :hours :days :months])))

(defn build-info-embed
  [event]
  (let [bot (.. event getJDA)]
    (-> (build-basic-embed event)
        (.addField "Support Server" (config :server-invite))
        (.addField "Source Code" (config :repo))
        (.addField (str "Invite " (.. bot
                                      (getSelfUser)
                                      (getAsMention)))
                   (config :bot-invite))
        (.addField "Guilds:" (count (.. bot getGuilds)) true)
        (.addField "Users:" (count (.. bot getUsers)) true)
        (.addField "Ping:" (.. bot getRestPing) true)
        (.addField "Bot Uptime:" ))))

(def info-command-data
  (.. (CommandData. "info" "Get useful bot data")))
      
(defn info-command 
  [event]
  (let [perks (case (.. event (getOption  "role") (getAsString))
                "survivor" sperks
                "killer" kperks)]
    (.. event
        (replyEmbeds (build-perk-roulette-embeds perks event))
        (setEphemeral true)
        (queue))))
