(defproject carlisle "0.1.0-SNAPSHOT"
  :description "A Clojure + Kotlin discord bot"
  :url "https://carlisle-bot.com"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[camel-snake-kebab "0.4.2"]
                 [net.dv8tion/JDA "4.3.0_339"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/core.async "1.3.618"]
                 [org.clojure/data.json "2.4.0"]
                 [org.clojure/data.xml "0.0.8"]
                 [org.clojure/tools.logging "1.1.0"]
                 [org.slf4j/slf4j-simple "1.7.9"]]
  :repositories [["m2-dv8tion" "https://m2.dv8tion.net/releases"]]
  :main ^:skip-aot carlisle.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"
                                  "-Dclojure.tools.logging.factory=clojure.tools.logging.impl/slf4j-factory"]}})
