(ns carlisle.commands.ask
  (:gen-class)
  (:use [carlisle.config :only [config]] 
        [carlisle.utils.basic]
        [carlisle.utils.commands]
        [clojure.java.javadoc]
        [clojure.repl])
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.data.xml :as xml])
  (:import  [net.dv8tion.jda.api.interactions.commands OptionType Command]
            [net.dv8tion.jda.api.interactions.commands.build Commands OptionData]))
           


(defn perform-query [q]
  (try 
    (-> (format "https://api.wolframalpha.com/v2/query?input=%s&appid=%s" 
                (str/replace q " " "%20")
                (config :wolfram-token))
        (slurp)
        (xml/parse-str)
        :content
        second
        :content
        first
        :content
        last
        :content
        first)
    (catch Exception e "Something went wrong, tell Paige")))

(def ask-command-data
  (.. (Commands/slash "ask" "general info")
      (addOptions [(OptionData. OptionType/STRING 
                                "query" 
                                "What info do you need?" 
                                true)
                   
                   (OptionData. OptionType/BOOLEAN 
                                "show-everyone" 
                                "Default: False.")])))
      
(defn ask-command 
  [event]
  (let [query (.. event (getOption "query") getAsString)
        ephemeral? (get-ephemeral-choice event true)]
    (.. event deferReply (setEphemeral ephemeral?) queue)
    (.. event 
        getHook 
        (sendMessage (format "__%s's query:__\n\t`%s`\n__Result:__\n`%s`"
                           (.. event getUser getAsMention)
                           query
                           (perform-query query)))
        (queue))))
