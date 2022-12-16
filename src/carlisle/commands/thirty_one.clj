(ns carlisle.commands.thirty-one
  (:require [carlisle.utils.basic :as utils]
            [clojure.tools.logging :as log]
            [thirty-one.io :as io])
  (:import [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build Commands OptionData]
           [net.dv8tion.jda.api.interactions.components.buttons Button]
           [net.dv8tion.jda.api.entities.emoji Emoji]))

(def games
  "Contains all active games"
  (atom {}))

(def thirty-one-command-data
  (Commands/slash "thirty-one" "Play a game of 31 with up to 10 players"))

(defn thirty-one-command
  [event]
  (.. event
      (replyEmbeds [(.. (utils/make-basic-embed)
                        (setTitle "A game is starting!\nPlayers:")
                        (setDescription (.. event getMember getAsMention))
                        (build))])
      (addActionRow [(.. (Button/link "https://github.com/qanazoga/thirty-one#rules" "Learn to Play")
                         (withEmoji (Emoji/fromUnicode "❔")))
                     (Button/secondary "31-join-game" "Join/Leave Game [1/10]")
                     (Button/success "31-start-game" "Start Game")])
      (queue (reify java.util.function.Consumer ;; TODO write a damn macro for this
               (accept [this hook]
                 (.. hook 
                     retrieveOriginal 
                     (queue (reify java.util.function.Consumer
                              (accept [this msg]
                                (swap! games 
                                       assoc 
                                       (.getIdLong msg)
                                       {:host (.. event getMember getIdLong)
                                        :players #{(.. event getMember getIdLong)}}))))))))))
  
