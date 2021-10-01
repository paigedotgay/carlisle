(ns carlisle.core
  (:gen-class)
  (:use [carlisle.config :only [config]] 
        [carlisle.logging :only [log-message]]
        [carlisle.ask.command]
        [carlisle.dead-by-daylight.command]
        [carlisle.info.command]
        [carlisle.mtg.command]
        [carlisle.repl.command]
        [carlisle.utils]
        [carlisle.warframe.command]
        [clojure.java.javadoc])
  (:import [net.dv8tion.jda.api JDABuilder Permission]
           [net.dv8tion.jda.api.events.message MessageReceivedEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.components Button]
           [net.dv8tion.jda.api.requests GatewayIntent]
           [net.dv8tion.jda.api.utils.cache CacheFlag]
           [net.dv8tion.jda.api.utils MemberCachePolicy]
           [java.lang Object])
  (:require [clojure.tools.logging :as log]))

(defn -main
  [& args]

  ;; Mostly here for repl sessions and debugging
  ;; being able to look at javadocs quickly is real nice
  (add-remote-javadoc "net.dv8tion.jda." "https://ci.dv8tion.net/job/JDA/javadoc/")
  
  (def message-listener
    (proxy [ListenerAdapter] []
            
      (onMessageReceived [event] 
        (log-message event)
        (if (safe-to-eval? event)
          (eval-command event)))
      
      (onSlashCommand [event]
        (try
          (case (.getName event)
            "ask"              (ask-command event)
            "dead-by-daylight" (dead-by-daylight-command event)
            "info"             (info-command event)
            "mtg"              (mtg-command event)
            "warframe"         (warframe-command event))
          
          (catch Exception e  
            (.. event
                (reply (format "Something went wrong!%nClick one of the links below, describe what you were trying to do, and provide this error code: `%s`"
                               (.getMessage e)))
                (addActionRow #{(Button/link (protocol-link :support-server) "Join the support server")
                                (Button/link (str (config :repo) "/issues/new") "Leave an issue on GitLab")
                                (Button/link (protocol-link :owner) "DM the owner (for friends only)")})
                (setEphemeral true)
                (queue)))))))
  
  (def carlisle (.. (JDABuilder/create (config :token) 
                                       [GatewayIntent/GUILD_MEMBERS 
                                        GatewayIntent/GUILD_PRESENCES
                                        GatewayIntent/GUILD_BANS 
                                        GatewayIntent/GUILD_EMOJIS 
                                        GatewayIntent/GUILD_VOICE_STATES 
                                        GatewayIntent/GUILD_MESSAGES 
                                        GatewayIntent/GUILD_MESSAGE_REACTIONS 
                                        GatewayIntent/DIRECT_MESSAGES 
                                        GatewayIntent/DIRECT_MESSAGE_REACTIONS])
                    (enableCache [CacheFlag/CLIENT_STATUS
                                  CacheFlag/EMOTE
                                  CacheFlag/MEMBER_OVERRIDES
                                  CacheFlag/ROLE_TAGS
                                  CacheFlag/VOICE_STATE])
                    (setMemberCachePolicy MemberCachePolicy/ALL)
                    (addEventListeners (into-array Object [message-listener]))
                  (build))))

