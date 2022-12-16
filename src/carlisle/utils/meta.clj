(ns carlisle.utils.meta
  (:require [carlisle.commands.ask :as ask]
            [carlisle.commands.dead-by-daylight :as dead-by-daylight]
            [carlisle.commands.info :as info]
            [carlisle.commands.move :as move]
            [carlisle.commands.mtg :as mtg]
            [carlisle.commands.role-signup :as role-signup]
            [carlisle.commands.roll :as roll]
            [carlisle.commands.warframe :as warframe]))

(def command-namespaces #{'ask 'dead-by-daylight 'info 'move 'mtg 'role-signup 'roll 'warframe})

(defn get-all-command-data 
  "useful for updating all commands at once, note that this does need to eval to work, so it's considered a little dangerous for now"
  []
  (set (for [ns command-namespaces]
         (eval (read-string (format "%s/command-data" ns))))))

(defn get-all-command-listeners
  []
  (set (for [ns command-namespaces]
         (eval (read-string (format "%s/command-listeners" ns))))))
