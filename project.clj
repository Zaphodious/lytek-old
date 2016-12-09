(defproject lytek "0.8.1-SNAPSHOT"
  :description "A library for the creation and validation of Exalted 3rd Edition Characters"
  :url "https://github.com/Zaphodious/lytek"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.9.0-alpha13"]
                 [org.clojure/test.check "0.9.0"]
                 [com.rpl/specter "0.13.1"]]
  :profiles {:dev {:dependencies [[speclj "3.3.1"]]}}
  :plugins [[speclj "3.3.1"]]
  :test-paths ["spec"])
