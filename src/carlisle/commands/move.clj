(ns carlisle.commands.move
  (:import [net.dv8tion.jda.api Permission]
           [net.dv8tion.jda.api.interactions.commands OptionType]
           [net.dv8tion.jda.api.interactions.commands.build CommandData OptionData]
           [net.dv8tion.jda.api.utils AttachmentOption]))

(def move-command-data 
  (.. (CommandData. "move" "Move a message from one channel to another")
      (addOptions [(OptionData. OptionType/STRING
                                "message-id"
                                "To get the ID, right click or tap and hold on the message and select Copy ID"
                                true)
                   (OptionData. OptionType/CHANNEL
                                "target-channel" 
                                "The channel you want to paste the message in"
                                true)
                   (.. (OptionData. OptionType/STRING
                                    "mode" 
                                    "Default: copy. Copy preserves the original message, cut deletes it" 
                                    false)
                       (addChoice "copy" "copy")
                       (addChoice "cut" "cut"))])))
                                      
(defn add-all-files
  [message files]
  ;; Gets the path of /tmp or the windows equivalent, adds a / to the end if there isn't one.
  (let [tmp-dir (as-> (System/getProperty "java.io.tmpdir") path
                  (case (last path)
                    (\\ \/) path
                    (str path \/)))] 
    (loop [message message
           files files]
        (if (zero? (count files))
          message
          (let [file (first files)
                path (str tmp-dir (.getFileName file))
                dlfile (.. file (downloadToFile path) get)
                spoiler (and (.isSpoiler file) AttachmentOption/SPOILER)]
            (recur (.. message 
                       (addFile dlfile
                                (into-array AttachmentOption nil)))
                   (rest files)))))))

(defn move
  "Moves a message by placing its content in an embed, and attaching its embeds and files.
  If there are already 10 embeds in the message the new embed will be sent first and the rest will follow."
  [event message message-author event-author target-channel mode]
  (let [copy? (= "copy" mode)
        embeds (.getEmbeds message)
        files (.. message getAttachments)
        op-content (.. message getContentRaw)
        main-embed (as-> (carlisle.utils.basic/make-basic-embed) embed
                     (.setTitle embed 
                                (str "Moved here by " (.. event-author getEffectiveName)))
                     (.setAuthor embed 
                                 (str "OP: " (.. message-author getEffectiveName) 
                                      (when copy?
                                        ", click here for original message"))
                                 (when copy? (.. message getJumpUrl))
                                 (.. message-author getEffectiveAvatarUrl))
                     (.setDescription embed op-content)
                     (if (not-empty embeds)
                       (.addField embed 
                                  (str "Embeds: " (count embeds))  
                                  "(will be sent following this one)"
                                  true)
                       embed)
                     (if (not-empty files)
                       (.addField embed 
                                  (str "Files: " (count files))
                                  "(should appear before this embed)"
                                  true)
                       embed)
                     (.build embed))
        all-embeds (cons main-embed embeds)
        sent-msg (if (> (count all-embeds) 10)
                   (do (-> target-channel
                           (.sendMessageEmbeds [(first all-embeds)])
                           (add-all-files files)
                           .complete)
                       (-> target-channel
                           (.sendMessageEmbeds (rest all-embeds))
                           (add-all-files files)
                           .complete))
                   (-> target-channel
                       (.sendMessageEmbeds all-embeds)
                       (add-all-files files)
                       .complete))]
          
    (when-not copy?
      (.. message delete complete))
  
    (.. event 
        getHook 
        (editOriginal (format "Success!, click [here](%s) to jump to the new message."
                      (.. sent-msg getJumpUrl)))
        complete)))

(defn move-command [event]
  (let [event-author (.. event getMember)
        message-id (.. event (getOption "message-id") getAsLong)
        message (try (.. event getChannel (retrieveMessageById message-id) complete)
                     (catch Exception e nil)) 
        message-author (when message
                         (.. event getGuild (retrieveMember (.getAuthor message)) complete))
        target-channel (.. event (getOption "target-channel") getAsGuildChannel)
        mode (if-let [x (.. event (getOption "mode"))]
               (.getAsString x)
               "copy")
        files-ok? (when message
                    (empty? (filter #(> (.getSize %) 8388608) (.getAttachments message))))
        can-delete? (or (= "copy" mode)
                        (= message-author event-author)
                        (.. event-author
                            (hasPermission (.. event getChannel) [Permission/MESSAGE_READ Permission/MESSAGE_MANAGE])))
        can-send? (.. event-author
                      (hasPermission target-channel [Permission/MESSAGE_WRITE]))
        error-msg (cond
                    (not can-delete?) "You can't delete that message!"
                    (not can-send?) "You can't send messages in that channel!"
                    (nil? message) (str "The message with id `" message-id "` was not found in this channel!\nRemember you need to use this command in the channel that has the original message")
                    (not files-ok?) "One of the attached files is too big for me to send!")]
    (.. event (deferReply true) complete)
    
    (if error-msg
      (.. event
          getHook
          (editOriginal error-msg)
          complete)
      (move event message message-author event-author target-channel mode))))

