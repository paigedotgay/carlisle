(defproject carlisle "0.1.0-SNAPSHOT"
  :description "A Clojure + Kotlin discord bot"
  :url "https://carlisle.qanazoga.com"
  :license {:name "MIT"
            :url "https://opensource.org/licenses/MIT"}
  :dependencies [[org.clojure/clojure "1.10.1"]]
  :main ^:skip-aot carlisle.core
  :target-path "target/%s"
  :profiles {:uberjar {:aot :all
                       :jvm-opts ["-Dclojure.compiler.direct-linking=true"]}})
