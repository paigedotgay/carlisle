(ns carlisle.logging
  (:gen-class)
  (:import [net.dv8tion.jda.api.entities.channel ChannelType]
           [net.dv8tion.jda.api.events.message MessageReceivedEvent])
  (:require [clojure.tools.logging :as log]))

  (defn log-message [event]
    (let [channel (if (.. event (isFromType ChannelType/TEXT))
                    (format "[%s/#%s] "
                            (.. event getGuild getName)
                            (.. event getChannel getName))
                    (str "[DM/" (.. event getAuthor getAsTag) "] "))]
      (log/info (format "%s| %s | %s" 
                       channel 
                       (.. event getAuthor getAsTag)
                       (.. event getMessage getContentDisplay)))))
