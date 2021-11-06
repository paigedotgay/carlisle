(ns carlisle.listeners.ready
  (:use [carlisle.config :only [app-info]])
  (:import [net.dv8tion.jda.api.hooks ListenerAdapter]))

(def ready-listener 
  (proxy [ListenerAdapter] []
    (onReady [event]
      (alter-var-root #'carlisle.config/app-info
              (constantly (.. event
                  getJDA
                  retrieveApplicationInfo
                  complete))))))

(def ready-listener-ci-cd 
  (proxy [ListenerAdapter] []
    (onReady [event]
      (.. event
          getJDA
          retrieveApplicationInfo
          complete
          getOwner
          openPrivateChannel
          complete
          (sendMessage "Build Passed! :tada:")
          complete)
      (System/exit 0))))
