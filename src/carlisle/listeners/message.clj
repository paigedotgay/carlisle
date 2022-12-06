(ns carlisle.listeners.message
  (:gen-class)
  (:use [carlisle.commands.repl]
        [carlisle.config :only [config]] 
        [carlisle.logging :only [log-message]]
        [carlisle.utils])
  (:import [net.dv8tion.jda.api.events.message MessageReceivedEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.components.buttons Button])
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
            (eval-command event)))))
