(ns carlisle.utils.warframe.archon-hunt
  (:gen-class)
  (:use [carlisle.utils.basic]
        [carlisle.config]) 
  (:require [clojure.string :as str]))

(defn build-archon-hunt-embeds [event worldstate]
  (let [boss-str (-> worldstate :archon-hunt :boss)
        time-remaining (-> worldstate :archon-hunt :eta)
        [color image] (case boss-str
                        "Archon Amar"   [13632027 "https://static.wikia.nocookie.net/warframe/images/b/be/ArchonAmar.png"]
                        "Archon Boreal" [4886754 "https://static.wikia.nocookie.net/warframe/images/1/1c/ArchonBoreal.png"]
                        "Archon Nira"   [16312092 "https://static.wikia.nocookie.net/warframe/images/4/4c/ArchonNira.png"])]
    
    #{(.. (make-basic-embed)
          (setColor color)
          (setTitle (str boss-str " is here."))
          (setThumbnail image)
          (setDescription (str "**Time Remaining:**\n" time-remaining))
          (build))}))
