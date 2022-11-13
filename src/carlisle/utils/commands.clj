(ns carlisle.utils.commands)

(defn get-ephemeral-choice
  "Gets the users 'show-everyone' choice. Default used as a fallback if the user doesn't make a selection

  Because users may know what 'ephemeral' means, 'show-everyone' is used as the prompt name instead.
  However, since these 'ephemeral' and 'show-everyone' are contrasting ideas, we must use the opposite of what the user selects.
  Your 'default' should not be the opposite of what you want.
  For example, if you want a permanent message (where the ephemeral? option should be false) you would use `(get-ephemeral-choice event false)`"
  [event default]
  (if-some [option (.. event (getOption "show-everyone"))]
                     (not (.. option getAsBoolean))
                     default))
  
