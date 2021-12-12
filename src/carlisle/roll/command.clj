(ns carlisle.roll.command
  (:import [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build CommandData]))

(def roll-command-data
  (.. (CommandData. "roll" "roll some dice!")
      (addOption OptionType/INTEGER 
                 "number-of-dice" 
                 "The number of dice you want to roll, defaults to 1." 
                 false)
      (addOption OptionType/INTEGER 
                 "sides-per-dice" 
                 "The number of sides on each dice you want to roll, defaults to 20." 
                 false)
      (addOption OptionType/BOOLEAN 
                 "total-all-dice" 
                 "Whether you want the total included with your roll, defaults to true" 
                 false)
      (addOption OptionType/INTEGER 
                 "plus" 
                 "A flat bonus you want to add to your roll, defaults to 0" 
                 false)
      (addOption OptionType/INTEGER 
                 "minus" 
                 "A flat debuff you want to subtract from your roll, defaults to 0" 
                 false)))

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
    (format "%s%nTotal: %s" (clojure.string/join ", " rolls) (reduce + rolls)) 
    (str (apply str rolls)))))

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
                nil)]
    (.. event (reply (roll :dice dice :sides sides :total total :plus plus :minus minus)) complete)))
    
