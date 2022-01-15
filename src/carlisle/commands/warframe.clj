(ns carlisle.commands.warframe
  (:gen-class)
  (:use [carlisle.utils.warframe.worldstate]
        [carlisle.utils.warframe.void-trader])
  (:import [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components.selections SelectionMenu]))

(def warframe-command-data
  (.. (CommandData. "warframe" "gets information about Warframe PC Worldstate")
      (addOptions [(.. (OptionData. OptionType/STRING "query" "What do you need information about?" true)
                       (addChoice "Void Trader / Baro Ki'Teer" "void-trader"))
                   
                   (OptionData. OptionType/BOOLEAN 
                                "show-everyone" 
                                "Default: Varies.")])))
      
(defn warframe-command 
  [event]
  (let [query (.. event (getOption "query") getAsString)
        ephemeral? (if-some [option (.. event (getOption "show-everyone"))]
                     (not (.. option getAsBoolean))
                     (case query
                       "void-trader" (not (-> @worldstate :void-trader :active))
                         true))]

    (.. event 
        (replyEmbeds (build-void-trader-embeds event @worldstate))
        (setEphemeral ephemeral?)
        (queue))))

