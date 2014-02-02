(ns dirwalker.cmdmain
  (:use [dirwalker.core])
  (:use [dirwalker.file-util])
  (:use [dirwalker.view.cmd-tree])
  (:use [dirwalker.webserver])
  (:require [clojure.tools.cli :refer [parse-opts]])
  (:gen-class)
)

(def cli-options
  [["-d" "--dir DIR_PATH" "Directory path"]
   ["-n" "--pattern PATTERN" "wildcard match patterns (not for server mode)"]
   ["-s" "--server" "Starts Web and REST Server"]
   ["-p" "--port PORT" "Port Number to listen at" :default 8080 :parse-fn #(Integer/parseInt %)]
   ["-h" "--help"]
  ])


(defn -main "entry of the program, --dir is required. -s kicks off a web/rest server, otherwise, presents like the tree command."
  [& args]
  (let [parsed (parse-opts args cli-options)
        options (:options parsed)
        a (println options)
        dir (:dir options)
        pattern (:pattern options)
        port (:port options)
        dir-obj (get-file dir)]
    (if (:help options)
      (println (:summary parsed))
      (if (or (nil? dir) (nil? dir-obj) (not (.exists dir-obj)))
        (println "-d DIR is a required argument, you must provide a dir that exists.")
        (if (:server options)
          (do 
            (def server (run-dw-server (.getCanonicalPath dir-obj) port))
            (println "DirWalker server running on port" port))
          (println (dir-tree-as-string (get-dir-info (.getCanonicalPath dir-obj) pattern))))))))