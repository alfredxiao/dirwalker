(ns dirwalker.view.xml
  (:use [dirwalker.core])
  (:require [clojure.xml :as xml]))

(defn gen-response-xml [dir-info]
  (defn convert-struct [{:keys [name path size is_dir fcount last subs]}]
    {
      :tag (if is_dir "dir" "file")
      :attrs (merge {:name name :size size :fcount fcount :last last} (when is_dir {:path path}))
      :content (vec (map convert-struct subs))})
  (let [ret-info dir-info]
    (with-out-str (xml/emit (convert-struct ret-info)))))
