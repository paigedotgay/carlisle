(ns carlisle.listeners.message
  (:gen-class)
  (:use [carlisle.config :only [config]] 
        [carlisle.logging :only [log-message]]
        [carlisle.ask.command]
        [carlisle.dead-by-daylight.command]
        [carlisle.info.command]
        [carlisle.mtg.command]
        [carlisle.repl.command]
        [carlisle.role-signup.command]
        [carlisle.utils]
        [carlisle.warframe.command])
  (:import [net.dv8tion.jda.api.events.message MessageReceivedEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.components Button])
  (:require [clojure.tools.logging :as log]))

(def message-listener
      (proxy [ListenerAdapter] []
            
        (onMessageReceived [event] 
          (log-message event)
          (if (safe-to-eval? event)
            (eval-command event)))

        (onMessageUpdate [event]
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
              "warframe"         (warframe-command event)
              "role-signup"      (role-signup-command event))
          
            (catch Exception e  
              (.. event
                  (reply (format "Something went wrong!%nClick one of the links below, describe what you were trying to do, and provide this error code: `%s`"
                                 (.getMessage e)))
                  (addActionRow #{(Button/link (config :server-invite) "Join the support server")
                                  (Button/link (str (config :repo) "/issues/new") "Leave an issue on GitLab")})
                  (setEphemeral true)
                  (queue)))))))
