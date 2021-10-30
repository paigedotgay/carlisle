(ns carlisle.role-signup.command
  (:gen-class)
  (:use [carlisle.config :only [config]]
        [carlisle.utils])
  (:import [net.dv8tion.jda.api EmbedBuilder]
           [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.commands OptionType Command]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.interactions.components Button]))

(def role-signup-command-data
  (.. (CommandData. "role-signup" "Set up a way for users to enrole themselves.")
      (addOptions (flatten [(.. (OptionData. OptionType/STRING "mode" "which method do you want to use to select roles?" true)
                                (addChoice "Create Buttons For Only Selected Roles (recommended)" "selection-mode")
                                (addChoice "Create Buttons For ALL ROLES (below your maximum role)" "all-roles-mode")
                                (addChoice "Create Buttons For ALL ROLES (below your maximum role) EXCEPT the selected roles" "exclusion mode"))
                            
                            (for [i (range 24)] (OptionData. OptionType/ROLE (str "role-" (inc i)) "Select a Role (Optional)" false))]))))

(def role-signup-listener
    (proxy [ListenerAdapter] []
      
      (onButtonClick [event]
        (.. event 
            (reply (str "adding role with ID " (.. event getButton getId)))
            (setEphemeral true)
            (queue)))))
      
(defn role-signup-command 
  [event]
    (.. event
        (reply "Sign up for roles here!")
        (addActionRow (for [role (.getOptionsByType event OptionType/ROLE)]
                         (Button/secondary (.. role getAsRole getId)
                                           (str \@ (.. role getAsRole getName)))))
        (queue)))


