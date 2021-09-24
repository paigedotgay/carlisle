(ns carlisle.utils
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
        (setColor 4708752)
        (setFooter "<3#3333 made this (◍•ᴗ•◍)" 
                   (.. event getJDA (getUserById (config :owner)) getAvatarUrl))))

(defn ^java.util.function.Consumer then [f]
  "Converts a function to java.util.function.Consumer, use in .queue to make things happen after"
  (reify java.util.function.Consumer
    (accept [this arg] (f arg))))
