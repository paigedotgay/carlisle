(ns carlisle.logging
  (:gen-class)
  (:import [net.dv8tion.jda.api.entities ChannelType]
           [net.dv8tion.jda.api.events.message MessageReceivedEvent]))

  (defn log-message [event]
    (let [channel (if (.. event (isFromType ChannelType/TEXT))
                    (format "[%s/#%s]%n    "
                            (.. event getGuild getName)
                            (.. event getChannel getName))
                    "[DM] ")]
      (println (format "%s%s: %s" 
                       channel 
                       (.. event getAuthor getAsTag)
                       (.. event getMessage getContentDisplay)))))
