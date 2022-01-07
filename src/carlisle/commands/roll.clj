(ns carlisle.commands.roll
  (:import [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build CommandData]))

(def roll-command-data
  (.. (CommandData. "roll" "roll some dice!")
      (addOption OptionType/INTEGER 
                 "number-of-dice" 
                 "Default: 1. The number of dice you want to roll." 
                 false)
      (addOption OptionType/INTEGER 
                 "sides-per-dice" 
                 "Default: 20. The number of sides on each dice you want to roll." 
                 false)
      (addOption OptionType/BOOLEAN 
                 "total-all-dice" 
                 "Default: True. Whether you want the total included with your roll." 
                 false)
      (addOption OptionType/INTEGER 
                 "plus" 
                 "Default: 0. A flat bonus you want to add to your roll." 
                 false)
      (addOption OptionType/INTEGER 
                 "minus" 
                 "Default: 0. A flat debuff you want to subtract from your roll." 
                 false)
      (addOption OptionType/BOOLEAN
                 "show-everyone"
                 "Default: True.")))

(defn roll
  [& {:keys [dice sides total plus minus] 
      :or {dice 1 
           sides 20 
           total true 
           plus nil 
           minus nil}}]
  (as-> (for [_ (range dice)] (inc (rand-int sides))) rolls
  (if plus
    (conj rolls plus)
    rolls)
  (if minus
    (conj rolls (* -1 minus))
    rolls)
  (if (and total 
           (or plus minus (> dice 1)))
    (format "%s%nTotal: %s" (clojure.string/join ", " rolls) (reduce +' rolls)) 
    (clojure.string/join ", " rolls))))

(defn roll-command [event]
  (let [dice (if-let [x (.. event (getOption "number-of-dice"))] 
               (.getAsLong x) 
               1)
        sides (if-let [x (.. event (getOption "sides-per-dice"))] 
                (.getAsLong x) 
                20)
        total (if-let [x (.. event (getOption "total-all-dice"))]
                (.getAsBoolean x)
                true)
        plus (if-let [x (.. event (getOption "plus"))]
               (.getAsLong x)
               nil)
        minus (if-let [x (.. event (getOption "minus"))]
                (.getAsLong x)
                nil)
        ephemeral? (if-some [option (.. event (getOption "show-everyone"))]
                     (not (.. option getAsBoolean))
                     false)]
    (.. event 
        (reply (roll :dice dice :sides sides :total total :plus plus :minus minus)) 
        (setEphemeral ephemeral?)
        complete)))
    
