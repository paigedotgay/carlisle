(ns carlisle.role-signup.command
  (:gen-class)
  (:use [carlisle.config :only [config]]
        [carlisle.utils])
  (:import [net.dv8tion.jda.api EmbedBuilder Permission]
           [net.dv8tion.jda.api.entities ChannelType]
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
                                (addChoice "Create Buttons For ALL ROLES (below your maximum role) EXCEPT the selected roles" "exclusion-mode"))
                            
                            (for [i (range 24)] (OptionData. OptionType/ROLE (str "role-" (inc i)) "Select a Role (Optional)" false))]))))

(defn select-roles [event] 
  (let [mode (-> event (.getOptionsByType OptionType/STRING) (first) (.getAsString))
        selected-roles (.. event (getOptionsByType OptionType/ROLE))]
    (case mode
      "selection-mode" (println :selection)
      "all-roles-mode" (println :all)
      "exclusion-mode" (println :exclusion)
      (throw (Exception. "no known mode selected")))))

(def role-signup-button-listener
    (proxy [ListenerAdapter] []
      
      (onButtonClick [event]
        (.. event 
            (reply (str "adding role with ID " (.. event getButton getId)))
            (setEphemeral true)
            (queue)))))
      
(defn role-signup-command 
  [event]

  (let [in-dm? (not= ChannelType/TEXT
                     (.. event
                         getChannel
                         getType))
        member (.. event (getMember)) ;; workaround to make things cleaner
        missing-permission? (if member 
                              (not (.. member (hasPermission [Permission/MANAGE_ROLES]))) 
                              true)]
    
    (cond 
      in-dm? (.. event
                 (reply "Sorry, this command can only be used in a guild")
                 (setEphemeral true)
                 (queue))
        
      missing-permission? (.. event
                              (reply "Sorry, you don't have permission to use this command")
                              (setEphemeral true)
                              (queue))
    
      :else (do (select-roles event)
                (.. event
                    (reply "Sign up for roles here!")
                    (addActionRow (for [role (.. event (getOptionsByType  OptionType/ROLE))]
                                    (Button/secondary (.. role getAsRole getId)
                                                      (str \@ (.. role getAsRole getName)))))
                    (queue))))))

