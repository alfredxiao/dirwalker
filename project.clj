(defproject dirwalker "0.1.0-SNAPSHOT"
  :description "FIXME: write description"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [
                 [org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.2.1"]
                 [ring/ring-jetty-adapter "1.2.1"]
                 [compojure "1.1.6"]
                 [org.clojure/tools.cli "0.3.0"]
                 [org.clojure/data.json "0.2.3"]
                 [hiccup "1.0.4"]
                ]
  :main dirwalker.cmdmain
)
