(ns carlisle.warframe.command
  (:gen-class)
  (:use [carlisle.warframe.utils])
  (:import [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components.selections SelectionMenu]))

(start-api-updates)

(def warframe-command-data
  (.. (CommandData. "warframe" "gets information about Warframe PC Worldstate")
      (addOptions [(.. (OptionData. OptionType/STRING "query" "What do you need information about?" true)
                       (addChoice "Void Trader / Baro Ki'Teer" "void-trader"))
                   
                   (OptionData. OptionType/BOOLEAN 
                                "show-everyone" 
                                "would you like everyone to be able to see the result?")])))
      
(defn warframe-command 
  [event]
  (let [query (.. event (getOption "query") getAsString)
        ephemeral? (if-not (empty? (.. event (getOptionsByName "show-everyone")))
                     (not (.. event (getOption "show-everyone") getAsBoolean))
                     true)]

    (.. event 
        (replyEmbeds (build-void-trader-embeds event))
        (setEphemeral ephemeral?)
        (queue))))

