(ns carlisle.mtg.command
  (:gen-class)
  (:use [carlisle.config :only [config]]
        [carlisle.utils]
        [clojure.repl])
  (:require [clojure.string :as str]
            [clojure.data.json :as json])
  (:import  [net.dv8tion.jda.api.interactions.commands OptionType Command]
            [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]))


(defn mtg [card]
  (-> (slurp
       (format "https://api.scryfall.com/cards/search?order=cmc&q=%s"
               (str/replace card #" " "%20")))
      (json/read-str  :key-fn keyword)
      :data
      first
      :image_uris
      :large))

(def mtg-command-data
  (.. (CommandData. "mtg" "fetch cards")
      (addOptions [(OptionData. OptionType/STRING
                                "name"
                                "name of the card"
                                true)

                   (OptionData. OptionType/STRING
                                "text"
                                "rules text")

                   (OptionData. OptionType/STRING
                                "type"
                                "card types")

                   (OptionData. OptionType/STRING
                                "colors"
                                "for all five, type wubrg, for azorious, type wu, etc")

                   (OptionData. OptionType/BOOLEAN
                                "show-everyone"
                                "would you like everyone to be able to see the result?")])))

(defn mtg-command
  [event]
  (let [name (.. event (getOption "name") getAsString)
        ephemeral? (if-not (empty? (.. event (getOptionsByName "show-everyone")))
                     (not (.. event (getOption "show-everyone") getAsBoolean))
                     true)]
    (.. event deferReply (setEphemeral ephemeral?) queue)
    (.. event
        getHook
        (sendMessage (format "%s's query:\n\t%s\nResult:\n\t%s"
                           (.. event getUser getAsMention)
                           name
                           (mtg name)))
        (queue))))
