(ns carlisle.repl
  (:gen-class)
  (:use [carlisle.config :only [config]] 
        [carlisle.util]
        [clojure.java.javadoc]
        [clojure.repl])
  (:require [clojure.string :as str]
            [clojure.data.json :as json]
            [clojure.data.xml :as xml])
  (:import [java.io ByteArrayOutputStream PrintStream PrintWriter OutputStreamWriter]
           [net.dv8tion.jda.api.events.message MessageReceivedEvent]
           [net.dv8tion.jda.api.entities ChannelType]))

;; Mostly here for repl sessions and debugging
;; being able to look at javadocs quickly is real nice
(add-remote-javadoc "net.dv8tion.jda." "https://ci.dv8tion.net/job/JDA/javadoc/")

;; we need this to expose things to eval
;; when we move eval command to it's own file we should take this too
(declare ^:dynamic event)
(declare ^:dynamic bot)
(declare ^:dynamic author)
(declare ^:dynamic channel)
(declare ^:dynamic msg)
(declare ^:dynamic txt)
(declare ^:dynamic guild)
(declare ^:dynamic *last*)


(def wolf-id "&appid=")
(def wolf-url "https://api.wolframalpha.com/v2/query?input=")

(defn reply 
  "shortcut to reply in current channel
  don't use outside of an eval command" 
  [msg]
  (let [fmt (if (<= 2000 (count msg))
              (if (or (str/starts-with? "```" msg) (str/starts-with? "```clj" msg))
                (trunc msg 2000)
                (str (trunc msg 1994) "...```"))
              msg)]
    (.queue (.. channel (sendMessage fmt)))
    fmt))

(defn strip-codeblock 
  "Writing code in blocks for syntax highlighting is nice, but breaks the evaluator.
  This strips the blocks (and the language identifier) from the text."
  [msg]
  (if (and
       (str/starts-with? msg "```")
       (str/ends-with? msg "```"))
    (str/replace msg #"(^.*?\s)|(\n.*$)" "" )
    msg))

(defn safe-to-eval? [event]
  (and 
   (-> (.. event getMessage getContentDisplay)
       (str/replace "\n" " ")
       (str/split #"\s")
       (first)
       (= "!e"))
   (= 
    (.. event getAuthor getId)
    (config :owner))))

(defn bruh [q]
  (-> (str wolf-url (str/replace q " " "%20") wolf-id)
      (slurp)
      (xml/parse-str)
      :content
      rest first
      :content
      first
      :content
      last
      :content
      first
      reply))

(defn- eval-to-map [txt]
  (with-out-result-map 
    (try (eval (read-string (strip-codeblock txt)))
         (catch Exception e (format "%nException: %s%nCause: %s"
                                    (.getMessage e)
                                    (.getCause e))))))

(defn- format-response [out-result-map]
  (reply (str out-result-map))
  (let [result (out-result-map :result)
        out (out-result-map :out)]
    (str/join "\n"
              [(->> [(str result) "nil"]
                    (filter #(not (str/blank? %)))
                    (first)
                    (format "Return: ```clj%n%s```"))
               (if (str/blank? out)
                 ""
                 (format "Out: ```clj%n%s```" out))])))
  
(defn eval-command 
  "Evaluate arbitrary code.
  The following variables have been assigned for your convenience:
  event   - The MessageReceivedEvent
  bot     - The bot
  author  - You
  channel - The channel the command was sent in
  msg     - The Message object
  txt     - The message, minus the !e
  guild   - The guild the command was sent in
  *last*  - The result of the last evaluation"
  [_event] 
  (->> (binding [*ns* (find-ns 'carlisle.repl)
                 event _event
                 bot (.. _event getJDA)
                 author (.. _event getAuthor)
                 channel (.. _event getChannel)
                 msg (.. _event getMessage)
                 txt (->> (.. _event getMessage getContentDisplay (str/split " "))
                          (filter #(not (str/blank? %)))
                          (rest)
                          (str/join " "))
                 guild (if (.. _event (isFromType ChannelType/TEXT))
                         (.. _event getGuild)
                         nil)]
         (let [result-map (eval-to-map txt)
               result (result-map :result)
               out (result-map :out)
               response (format-response result-map)]
           
           (reply response)
           result))
       (def ^:dynamic *last*)))

(-> (slurp "https://api.warframestat.us/pc/voidTrader")
    (json/read-str :key-fn keyword)
    (str/replace "," "\n"))
                   
