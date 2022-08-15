(ns carlisle.core
  (:gen-class)
  (:use [carlisle.commands.role-signup :only [role-signup-button-listener]]
        [carlisle.config :only [config]] 
        [carlisle.listeners.message]
        [carlisle.listeners.ready]
        [carlisle.listeners.interaction])
  (:import [net.dv8tion.jda.api JDABuilder Permission]
           [net.dv8tion.jda.api.requests GatewayIntent]
           [net.dv8tion.jda.api.utils.cache CacheFlag]
           [net.dv8tion.jda.api.utils MemberCachePolicy])
  (:require [clojure.string :as str]
            [clojure.tools.logging :as log]))

(defn build-full [token]
  (let [cache   [CacheFlag/CLIENT_STATUS
                 CacheFlag/EMOJI
                 CacheFlag/MEMBER_OVERRIDES
                 CacheFlag/ROLE_TAGS
                 CacheFlag/VOICE_STATE]
        intents [GatewayIntent/GUILD_MEMBERS
                 GatewayIntent/GUILD_PRESENCES
                 GatewayIntent/GUILD_BANS 
                 GatewayIntent/GUILD_EMOJIS_AND_STICKERS
                 GatewayIntent/GUILD_VOICE_STATES 
                 GatewayIntent/GUILD_MESSAGES 
                 GatewayIntent/GUILD_MESSAGE_REACTIONS 
                 GatewayIntent/DIRECT_MESSAGES 
                 GatewayIntent/DIRECT_MESSAGE_REACTIONS
                 GatewayIntent/MESSAGE_CONTENT]]
       (.. (JDABuilder/create token intents)
           (enableCache cache)
           (setMemberCachePolicy MemberCachePolicy/ALL)
           (addEventListeners (object-array [message-listener 
                                             ready-listener
                                             role-signup-button-listener
                                             command-listener]))
           (build))))

(defn build-ci-cd [token]
  (.. (JDABuilder/createLight token)
      (addEventListeners (object-array [ready-listener-ci-cd]))
      (build)))

(defn -main
  "Builds a JDA object. 
  ci-cd-mode will run with a token (last item in args), notify if it succesfully logs in, then close.
  dev-mode will use the dev-token in config."
  [& args]

  (let [ci-cd-mode? (some #{"ci-cd-mode"} args)
        dev-mode? (some #{"dev-mode"} args)
        token (if dev-mode?
                (config :dev-token)
                (or (last args) (config :token)))]

    (and ci-cd-mode? 
         (-> (count args) (< 2))
         (throw (Exception. 
                 "in ci-cd-mode you must provide a token")))
    
    (if token
      (if ci-cd-mode?
        (build-ci-cd token)
        (build-full token))
      (throw (Exception. "No token provided!")))))

