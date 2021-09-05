(ns carlisle.util
  (:gen-class)
  (:require [clojure.string :as str]))

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
