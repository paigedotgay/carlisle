(ns carlisle.core
  (:gen-class)
  (:use [carlisle.logging :only [log-message]]
        [carlisle.repl]
        [clojure.java.javadoc])
  (:import [net.dv8tion.jda.api JDABuilder Permission]
           [net.dv8tion.jda.api.events.message MessageReceivedEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [java.lang Object]))

(defn -main
  [& args]

  (def token (first args))

  ;; Mostly here for repl sessions and debugging
  ;; being able to look at javadocs quickly is real nice
  (add-remote-javadoc "net.dv8tion.jda." "https://ci.dv8tion.net/job/JDA/javadoc/")
  
  (def listener
   (proxy [ListenerAdapter] []
      (onReady [event] 
        (println "\nSuccessfully logged in!\n-----------------------"))
      
      (onMessageReceived [event] 
        (log-message event)
        (if (safe-to-eval? event) (eval-command event)))))
  
  (def carlisle (-> (JDABuilder/createDefault token)
      (.addEventListeners (into-array Object [listener]))
      (.build))))

