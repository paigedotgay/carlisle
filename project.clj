(defproject carlisle "0.1.0-SNAPSHOT"
  :description "A Clojure + Kotlin discord bot"
  :url "https://carlisle.qanazoga.com"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [net.dv8tion/JDA "4.3.0_277"]
                 [org.apache.logging.log4j/log4j-api "2.14.1"]
                 [org.apache.logging.log4j/log4j-core "2.14.1"]]
  :repositories [["m2-dv8tion" "https://m2.dv8tion.net/releases"]]
  :main ^:skip-aot carlisle.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
