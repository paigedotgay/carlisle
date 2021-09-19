(ns carlisle.commands.wf
  (:gen-class)
  (:use [carlisle.utils.wf])
  (:import [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components.selections SelectionMenu]))

(def warframe-command-data
  (-> (CommandData. "warframe" "gets information about Warframe PC Worldstate")
      (.addOption OptionType/STRING "key" "The topic you need information about" false)))

(def warframe-command-menu
  (-> (SelectionMenu/create "warframe-menu")
      (.setPlaceholder "What do you need information about?")
      (.setRequiredRange 1 1)
      (.addOption "Void Trader" "void-trader")
      (.build)))

(def warframe-command-menu-listener
  (proxy [ListenerAdapter] []
    (onSelectionMenu [event]
      (let [key (-> (.getValues event)
                    (first)
                    (keyword))]
            (.. event getMessage delete queue)
            (.. event
                (replyEmbeds [(build-void-trader-embed event)])
                (setEphemeral true)
                (queue))))))
      
(defn warframe-command 
  [event]
  (if (zero? (count (.getOptions event)))
    (.. event (reply "What information do you need?") 
        (setEphemeral false)
        (addActionRow [warframe-command-menu])
        (queue))
    (-> event 
        (.replyEmbeds [(build-void-trader-embed event)])
        (.setEphemeral true)
        (.queue))))

