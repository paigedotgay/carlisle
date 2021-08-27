(ns carlisle.repl
  (:gen-class)
  (:use [clojure.java.javadoc]
        [clojure.repl])
  (:require [clojure.string :as str]
            [clojure.data.json :as json])
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
    "135347294093443072"))) ;this shouldn't be hard-coded, I'll fix it soon :p

(defn reply 
  "shortcut to reply in current channel
  don't use outside of an eval command" 
  [msg]
  (.queue (.. channel (sendMessage msg))))

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
         
         (let [result (try
                        (eval (read-string (strip-codeblock txt)))
                        (catch Exception e (format "%nException: %s%nCause: %s"
                                                   (.getMessage e)
                                                   (.getCause e))))
               
               response (->> [(str result) "nil"]
                             (filter #(not (str/blank? %)))
                             (first)
                             (format "```clj%n%s```"))]
           
           (reply response)
           result))
       (def ^:dynamic *last*)))




