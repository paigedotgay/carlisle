(ns carlisle.commands.warframe
  (:gen-class)
  (:use [carlisle.utils.warframe.worldstate]
        [carlisle.utils.warframe.archon-hunt]
        [carlisle.utils.warframe.void-trader])
  (:import [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build Commands OptionData]))

(def warframe-command-data
  (.. (Commands/slash "warframe" "gets information about Warframe PC Worldstate")
      (addOptions [(.. (OptionData. OptionType/STRING "query" "What do you need information about?" true)
                       (addChoice "Void Trader / Baro Ki'Teer" "void-trader")
                       (addChoice "Archon Hunt" "archon-hunt"))
                   
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
                         true))
        embeds (case query
                 "archon-hunt" (build-archon-hunt-embeds event @worldstate)
                 "void-trader" (build-void-trader-embeds event @worldstate))]

    (.. event 
        (replyEmbeds embeds)
        (setEphemeral ephemeral?)
        (queue))))

