(ns carlisle.utils.general
  (:gen-class)
  (:require [clojure.string :as str])
  (:use [carlisle.config :only [config]])
  (:import [net.dv8tion.jda.api EmbedBuilder]))

(defn trunc
  [s n]
  (subs s 0 (min (count s) n)))

(defmacro with-out-result-map
  [& body]
  `(let [s# (java.io.StringWriter.)]
     (binding [*out* s#
               *flush-on-newline* false]
       (let [r# ~@body]
         {:result r#
          :out (str s#)}))))

(defn build-basic-embed [event]
    (.. (EmbedBuilder.)
        (setFooter "<3#3333 made this (◍•ᴗ•◍)" 
                   (.. event getJDA (getUserById (config :owner)) getAvatarUrl))))
