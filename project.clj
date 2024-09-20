(defproject http+kafka "0.1.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.10.1"]
                 [metosin/compojure-api "2.0.0-alpha33"]
                 [fundingcircle/jackdaw "0.7.8"]
                 [pjstadig/humane-test-output "0.11.0"]
                 [com.taoensso/timbre "6.6.0-RC1"]]
  :ring {:handler http+kafka.routes/app}
  :profiles {:dev
             {:plugins [[lein-ring "0.12.6"]]}}
  :repl-options {:init-ns http+kafka.routes})
