(ns carlisle.commands.info
  (:gen-class)
  (:use [carlisle.config]
        [carlisle.utils.basic]
        [clojure.java.shell :only [sh]])
  (:require
        [clojure.string :as str])
  (:import [net.dv8tion.jda.api EmbedBuilder Permission]
           [net.dv8tion.jda.api.interactions.commands OptionType Command]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData SubcommandData SubcommandGroupData]
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

(defn build-bot-info-embed [event]
  (let [bot (.. event getJDA)
        commands (.. bot retrieveCommands complete)
        name-desc-list (sort (for [command commands] 
                                (format "**/%s:** %s," 
                                        (.getName command)  
                                        (.getDescription command))))  
        ping (.. bot getRestPing complete)
        largest (first (largest-duration (map-duration (get-millis))))
        duration (if (= 1 (val largest))
                   (str/join "" (drop-last (name (key largest))))
                   (name (key largest)))]
    (.. (make-basic-embed)
        (setThumbnail (.. bot getSelfUser getAvatarUrl))
        (addField "Guilds:" (str (count (.. bot getGuilds))) true)
        (addField "Users:" (str (count (.. bot getUsers))) true)
        (addField "Ping:" (format "%dms" ping) true)
        (addField "Bot Uptime:" 
                     (str (val largest) \space duration)
                   true)
        (addField "Server Uptime" 
                   (-> (sh "uptime" "-p")
                       (:out)
                       (str/split #"( |,|\n)")
                       (rest)
                       (#(str (first  %) \space (second %))))
                   true)
        (addField (str (count commands) " Commands") (str/join "\n" name-desc-list) false)
        build)))

(defn build-guild-info-embed [event]
  (if (.. event getGuild)
    (let [guild (.. event getGuild)
          members (.. guild getMembers)]
      (.. (make-basic-embed)
          (setThumbnail (.. guild getIconUrl))
          (setAuthor (str "Owner: " (.. guild getOwner getUser getAsTag))
                     (str "https://discord.com/users/" (.. guild getOwner getUser getId))
                     (.. guild getOwner getEffectiveAvatarUrl))
          (addField "id:" (.. guild getId) false)
          (addField  (str "Members: " (count members))
                     (format "- **Users:** %d%n- **Bots:** %d" 
                             (count (filter #(not (.. % getUser isBot)) members))
                             (count (filter #(.. % getUser isBot) members)))
                     true)
          (addField (str "Channels: " (- (count (.. guild getChannels)) (count (.. guild getCategories))))
                    (format "- **Text:** %d%n- **Voice:** %d" 
                            (count (.. guild getTextChannels))
                            (count (.. guild getVoiceChannels)))
                    true)
          (addField (let [[year month day] 
                          (clojure.instant/parse-timestamp vector (.. guild getTimeCreated toString))]
                      
                      (format "Founded %s %d, %d"
                              (-> (java.time.Month/of month) str/capitalize)
                              day year))
                    (str "**Custom Emotes:** " (count (.. guild getEmotes)))
                    true)
          build))
    (.. (make-basic-embed)
        (addField "Sorry Friend," "You can only use this command inside a guild" false)
        (build))))
  

(defn build-user-info-embed [event]
  (let [member (.. event (getOption "user") getAsMember)
        user (.. event (getOption "user") getAsUser)]
    (println [user member])
    (as-> (make-basic-embed) embed ;; I hate this I hate this I hate this I hate this
      (.setAuthor embed (if (and member (.. member getNickname))
                          (format "%s (%s)"
                                  (.. member getEffectiveName)
                                  (.. user getAsTag))
                          (.. user getAsTag))
                  (.. user getAvatarUrl)
                  (.. user getAvatarUrl))
      
      (.setImage embed (if member
                             (.. member getEffectiveAvatarUrl)
                             (.. user getAvatarUrl)))
      (.addField embed "id:" (.. user getId) false)
      (.addField embed "Account Created:"
                 (let [[year month day] 
                       (clojure.instant/parse-timestamp vector (.. user getTimeCreated toString))]
                   
                   (format "%s %d, %d"
                           (-> (java.time.Month/of month) str/capitalize)
                           day year))
                 true)
      (if member
        (do (.addField embed "Joined Guild:"
                       (let [[year month day] 
                             (clojure.instant/parse-timestamp vector (.. member getTimeJoined toString))]
                         
                         (format "%s %d, %d"
                                 (-> (java.time.Month/of month) str/capitalize)
                                 day year))
                       true)
            (.addField embed (str "Roles: " (count (.. member getRoles)))
                       (trunc (str/join ", " (map #(.getName %) (.. member getRoles))) 1024)
                       false))
        embed)
      (.build embed))))

(def info-command-data
    (.. (CommandData. "info" "Get useful data")
        (addSubcommandGroups #{(.. (SubcommandGroupData. "about" "select a target")
                                   (addSubcommands (->> #{(SubcommandData. "bot" "Get info about this bot.")
                                                         (SubcommandData. "guild" "Get info about this guild.")
                                                         (.. (SubcommandData. "user" "Get info about a user.")
                                                             (addOption OptionType/USER 
                                                                        "user"
                                                                        "Who do you want information about." true))}
                                                       (map #(.addOption %
                                                                         OptionType/BOOLEAN
                                                                         "show-everyone"
                                                                         "Default: False.")))))})))
  
(defn info-command 
  [event]
  (let [ephemeral? (if-some [option (.. event (getOption "show-everyone"))]
                     (not (.. option getAsBoolean))
                     true)
        build-embed (case (.. event getSubcommandName)
                      "bot"   build-bot-info-embed
                      "guild" build-guild-info-embed
                      "user"  build-user-info-embed
                      (throw (Exception. (format "subcommand %s not found"))))
        response (.. event
                     (replyEmbeds #{(build-embed event)})
                     (setEphemeral ephemeral?))]
    
    (.. (if (= (.. event getSubcommandName) "bot")
          (.. response
              (addActionRow #{(Button/link (str "https://discord.com/invite/" (config :server-invite-code))
                                           "Join the Support Server")
                              (Button/link (config :repo) 
                                           "View the Source Code")})
              (addActionRow #{(Button/link (.. app-info (getInviteUrl #{Permission/ADMINISTRATOR})) 
                                           "Invite Carlisle to Your Server")}))
          response)
        complete)))
