(ns carlisle.role-signup.command
  (:gen-class)
  (:use [carlisle.utils])
  (:require [clojure.string :as str])
  (:import [net.dv8tion.jda.api EmbedBuilder Permission]
           [net.dv8tion.jda.api.entities ChannelType]
           [net.dv8tion.jda.api.events.interaction SelectionMenuEvent]
           [net.dv8tion.jda.api.hooks ListenerAdapter]
           [net.dv8tion.jda.api.interactions.commands OptionType Command]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData SubcommandData]
           [net.dv8tion.jda.api.interactions.components ActionRow Button]))

(def role-signup-command-data
  (.. (CommandData. "role-signup" "Set up a way for users to enrole themselves.")
      (addSubcommands #{(.. (SubcommandData. "selection-mode" "Create Buttons For Only Selected Roles (below your maximum role) (recommended)")
                            (addOption OptionType/STRING "message" "What would you like the message attactched to the buttons to say?")
                            (addOptions (for [i (range 24)] (OptionData. OptionType/ROLE (str "role-" (inc i)) "Select a Role (Optional)" false))))

                        (.. (SubcommandData. "all-roles-mode" "Create Buttons For ALL ROLES (below your maximum role)")
                            (addOption OptionType/STRING "message" "What would you like the message attactched to the buttons to say?"))

                        (.. (SubcommandData. "exclusion-mode" "Create Buttons For ALL ROLES (below your maximum role) EXCEPT the selected roles")
                            (addOption OptionType/STRING "message" "What would you like the message attactched to the buttons to say?")
                            (addOptions (for [i (range 24)] (OptionData. OptionType/ROLE (str "role-" (inc i)) "Select a Role (Optional)" false))))})))

(defn get-desired-roles [event] 
  (let [mode (.. event getSubcommandName)
        selected-roles (map #(.getAsRole %) (.. event (getOptionsByType OptionType/ROLE)))
        legal-roles (filter #(and (not (.isManaged %))
                                  (not (.isPublicRole %))
                                  (.. event getMember (canInteract %)))
                            (.. event getGuild getRoles))]
    (case mode
      "selection-mode" (filter #((set legal-roles) %) selected-roles)
      "all-roles-mode" legal-roles
      "exclusion-mode" (remove (set selected-roles) legal-roles)
      (throw (Exception. "no known mode selected")))))

(def role-signup-button-listener
  (proxy [ListenerAdapter] []
      
    (onButtonClick [event]
      (if (= "role-signup" (first (str/split (.. event getButton getId) #" ")))
        (let [guild (.. event getGuild)
              member (.. event getMember)
              role-id (second (str/split (.. event getButton getId) #" "))
              role (.. guild (getRoleById role-id))
              [modification-fn modification-str] (if (some #{role} (.. member getRoles))
                                                   [#(.removeRoleFromMember %1 %2 %3) "removed"]
                                                   [#(.addRoleToMember %1 %2 %3) "added"])]
              
              (-> event
                  .getGuild
                  (modification-fn member role)
                  .complete)
              
              (.. event 
                  (reply (format "Successfully %s role @%s!"
                                 modification-str
                                 (.. role getName)))
                  (setEphemeral true)
                  (complete)))))))
      
(defn role-signup-command 
  [event]

  (let [in-dm? (not= ChannelType/TEXT
                     (.. event
                         getChannel
                         getType))
        member (.. event (getMember)) ;; workaround to make things cleaner
        missing-permission? (if member 
                              (not (.. member (hasPermission [Permission/MANAGE_ROLES]))) 
                              true)
        message (if-some [msg (.. event (getOption "message"))]
                  (.getAsString msg)
                  "Sign up for roles here!")]
    
    (cond 
      in-dm? 
      (.. event
          (reply "Sorry, this command can only be used in a guild")
          (setEphemeral true)
          (queue))
        
      missing-permission? 
      (.. event
          (reply "Sorry, you don't have permission to use this command")
          (setEphemeral true)
          (queue))
    
      :else 
      (let [roles (get-desired-roles event)
            partitions (partition-all 5 roles)]
        (cond
          (< 25 (count roles))
          (.. event
              (reply "Sorry, due to discord limitations we can only make 25 buttons at a time.\nTry using selection-mode multiple times?")
              (setEphemeral true)
              (queue))
          
          (= 0 (count roles))
          (.. event
              (reply (str "Sorry, you need to select at least one legal role.\n(You cannot select @everyone or integration specific roles like " (.. event getGuild getBotRole getAsMention)\)))
              (setEphemeral true)
              (queue))
          
          :else
          (.. event
              (reply message)
              (addActionRows (for [part partitions]
                               (ActionRow/of
                                (for [role part] 
                                  (Button/secondary (str "role-signup " (.. role getId))
                                                    (.. role getName))))))
              (queue)))))))
