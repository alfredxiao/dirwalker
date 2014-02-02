(ns dirwalker.view.cmd-tree
  (:use [dirwalker.core]))

(defn- cmd-visitor "formats a string representation of information about a node"
  [step {:keys [name size fcount is_dir prefix nl-prefix is-root is-last sublen]}]
  (cond
    (nil? name) ""
    (= "pre" step) (if is-root "" (str prefix (if is-last "└" "├") "── "))
    (= "self" step) (if is_dir (format "%s [%12d/%-8d]\n" name size fcount) (format "'%s'[%12d]\n" name size))
    :else ""))

(defn- visit-node "Visits a node and generates/returns a vector of string representation of it and its sub-nodes"
  [dir-info prefix is-root is-last]
  (let [ {:keys [name size fcount is_dir subs]} dir-info
         sublen (if (= subs nil) 0 (count subs))
         nl-prefix (if is-root "" (str prefix (if is-last "    " "│   ")))
         node-info {:name name :size size :fcount fcount :prefix prefix :nl-prefix nl-prefix
                    :is_dir is_dir :is-last is-last :is-root is-root}] 
    (reduce into
      [(cmd-visitor "pre" node-info) (cmd-visitor "self" node-info)]
      (map-indexed 
        (fn [idx ff-info] 
          (visit-node ff-info nl-prefix false (= sublen (inc idx))))
        (:subs dir-info)))))

(defn dir-tree-as-string "Returns the tree information as a string for printing"
  [dir-info]
  (apply str (visit-node dir-info "" true false)))