(ns dirwalker.view.json
  (:use [dirwalker.core])
  (:require [clojure.data.json :as json])
)

(defn gen-response-json "generates JSON string representing a dir/file"
  [dir-info]
  (json/write-str dir-info ))
