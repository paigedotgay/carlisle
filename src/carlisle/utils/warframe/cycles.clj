(ns carlisle.utils.warframe.cycles
  (:gen-class)
  (:use [carlisle.utils.basic]) 
  (:require [clojure.string :as str]))

(defn build-time-cycle-embeds   
  "Could I have made this shorter? yes. But the API uses inconsistent terms >:C"
  [event worldstate]
  (let [earth (worldstate :earth-cycle)
        cetus (worldstate :cetus-cycle)
        cambion (worldstate :cambion-cycle)
        vallis (worldstate :vallis-cycle)
        zariman (worldstate :zariman-cycle)]
    #{(.. (make-basic-embed)
          (setTitle "Current Time States")
          (addField "Earth (Not Cetus)" 
                    (str/capitalize (str (earth :state) " for the next " (earth :time-left)))
                    false)
          (addField "Cetus"
                    (str/capitalize (str (cetus :state) " for the next " (cetus :time-left)))
                    false)
          (addField "Cambion Drift" 
                    (str/capitalize (str (cambion :active) " for the next " (cambion :time-left)))
                    false)
          (addField "Orb Vallis"
                    (str/capitalize (str (vallis :state) " for the next " (vallis :time-left)))
                    false)
          (addField "Zariman"
                    (str "Invaded by " (str/capitalize (zariman :state)) " for the next " (zariman :time-left))
                    false)
          (build))}))
