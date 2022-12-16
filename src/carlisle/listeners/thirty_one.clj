(ns carlisle.listeners.thirty-one
  (:gen-class)
  (:require [carlisle.commands.thirty-one :as cto]
            [carlisle.utils.basic :as utils]
            [thirty-one.core :as thirty-one]
            [thirty-one.gamestate :as gs]
            [thirty-one.io :as io])
  (:import [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions InteractionType]
	   [net.dv8tion.jda.api.interactions.components.buttons Button])
  (:require [clojure.tools.logging :as log]))

(defn io-handler
  [event]
  (fn [{player-index :active-player
        players :players
        player-mention (.. event getGuild getUserById (players player-index) getAsMention)
        :as gamestate}]
    (let [message-id (.. event getMember getIdLong)
          actions (io/get-actions gamestate)]
      )))

(defn join-game-button-action
  [event]
  (let [button-id (.. event getButton getId)
        message-id (.. event getMessageIdLong)
        member-id (.. event getMember getIdLong)]
    (do (swap! cto/games update-in [message-id :players] 
             (if ((-> @cto/games (get message-id) :players) member-id)
               disj
               conj) 
             member-id)
      (if (let [game (get @cto/games message-id)]
            ((game :players) (game :host)))
        (do (.. event
                (editMessageEmbeds [(.. (utils/make-basic-embed)
                                        (setTitle "A game is starting!\nPlayers:")
                                        (setDescription (clojure.string/join "\n"
                                                                             (map #(.. event getGuild (getMemberById %) getAsMention)
                                                                                  (-> @cto/games (get message-id) :players))))
                                        (build))])
                (complete))
            (.. event
                (editButton (Button/secondary "31-join-game" (format "Join/Leave Game [%s/10]"
                                                                     (-> @cto/games (get message-id) :players count))))
                (complete)))
        
        (do (swap! cto/games dissoc message-id)
            (.. event
                (editMessageEmbeds #{(.. (utils/make-basic-embed)
                                         (setTitle "Host Left. Game Cancelled")
                                         (build))})
                (setComponents [])
                (complete)))))))

(defn start-game
  [event]
  (let [button-id (.. event getButton getId)
        message-id (.. event getMessageIdLong)
        member-id (.. event getMember getIdLong)
        get-gamestate (comp :gamestate #(get % message-id) deref)
        get-queued-players (comp :players get-gamestate deref)]
    (swap! cto/games assoc-in [message-id :gamestate] 
           (reduce gs/add-player (gs/new-gamestate) (cto/games get-queued-players)))))    
               
           

(defn start-game-button-action
  [event]
  (let [button-id (.. event getButton getId)
        message-id (.. event getMessageIdLong)
        member-id (.. event getMember getIdLong)]
    (if (= member-id
           (-> @cto/games (get message-id) :host))
      (do 
          (.. event
              (editMessageEmbeds [(.. (utils/make-basic-embed)
                                      (setTitle "Starting Game")
                                      (build))])
              (complete)))
      (.. event
          (reply (format "Only the Host (%s) can start the game." 
                         (.. event getGuild (getMemberById (-> @cto/games (get (.getMessageIdLong event)) :host)) getAsMention)))
          (setEphemeral true)
          (complete)))))

(defn handle-event
  "We use a seperate function for handling the event after it's recieved because repl likes it better."
  [event]
  (case (.. event getButton getId)
    "31-join-game" (join-game-button-action event)
    "31-start-game" (start-game-button-action event)))
                      

(def thirty-one-listener
  (proxy [ListenerAdapter] []
    (onButtonInteraction [event]
      (handle-event event))))
                                        

