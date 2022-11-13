(ns carlisle.commands.mtg
  (:gen-class)
  (:use [carlisle.config :only [config]]
        [carlisle.utils.basic]
        [carlisle.utils.commands]
        [clojure.repl])
  (:require [clojure.string :as str]
            [clojure.data.json :as json])
  (:import  [net.dv8tion.jda.api.interactions.commands OptionType Command]
            [net.dv8tion.jda.api.interactions.commands.build Commands OptionData]))


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
  (.. (Commands/slash "mtg" "fetch cards")
      (addOptions [(OptionData. OptionType/STRING
                                "name"
                                "Name of the card."
                                true)

                   (OptionData. OptionType/STRING
                                "text"
                                "Rules text.")

                   (OptionData. OptionType/STRING
                                "type"
                                "Card types")

                   (OptionData. OptionType/STRING
                                "colors"
                                "For all five, type wubrg, for azorious, type wu, etc.")

                   (OptionData. OptionType/BOOLEAN
                                "show-everyone"
                                "Default: True.")])))

(defn mtg-command
  [event]
  (let [name (.. event (getOption "name") getAsString)
        ephemeral? (get-ephemeral-choice event false)]
    (.. event deferReply (setEphemeral ephemeral?) queue)
    (.. event
        getHook
        (sendMessage (format "%s's query:\n\t%s\nResult:\n%s"
                           (.. event getUser getAsMention)
                           name
                           (mtg name)))
        (queue))))
