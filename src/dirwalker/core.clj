(ns dirwalker.core
  (:use [dirwalker.file-util]))

(defn- walk-file "returns map struct of a file object"
  [file match]
  (if (match file)
    {:is_dir false 
     :path (.getCanonicalPath file)
     :name (.getName file) 
     :size (.length file) 
     :fcount 1 
     :last (.lastModified file)}
    {}))

(defn- assoc-fn "assoc with new value computed via a fn which take both the 'old' and the 'new' value"
  [m k fn-update v] (assoc m k (fn-update (k m) v)))

(defn- walk-dir [dir matcher] "returns map struct of a dir object, including sub-dirs"
  (if (.isFile dir)
    (walk-file dir matcher)
    (reduce 
      (fn [agg-info ff-info]   ;; ff-info means file or filder info
        (-> agg-info
          (assoc-fn :size + (get ff-info :size  0))
          (assoc-fn :fcount + (get ff-info :fcount 0))
          (assoc-fn :subs conj ff-info))) 
      {:is_dir true
       :path (.getCanonicalPath dir)
       :size 0 
       :fcount 0 
       :last (.lastModified dir)
       :name (.getName dir) 
       :subs []} 
      (map #(walk-dir % matcher) (list-sub-files dir)))))

(defn get-dir-info "Returns information about a dir in a map structure, with descendents included"
  [path pattern]
  (let [dir (get-file path)]
    (if (.exists dir)
      (walk-dir dir (matcher-by-pattern pattern))
      nil)))

(def dir-info-cache (ref {}))


(defn strip-descendents "removes information about descendents while information about direct children is retained"
  [dir-info]
  (assoc (dissoc dir-info :subs) :subs (vec (for [m (:subs dir-info)] (dissoc m :subs)))))

(defn get-dir-info-cached
  [path pattern desc]
  (let [dir (get-file path)]
    (if (.exists dir)
      (dosync
        (let [found (get @dir-info-cache [path pattern desc]) last-mod (.lastModified dir)]
          (if (and found (= last-mod (second found)))
            (first found)
            (let [full-dir-info (get-dir-info path pattern) dir-info (if desc full-dir-info (strip-descendents full-dir-info))]
              (alter dir-info-cache assoc [path pattern desc] [dir-info last-mod])
              dir-info))))
      nil)))

(defn subs-sorted "sort subs accordings to specified sort, e.g. +size, -last, name, -is_dir"
  [dir-info order]
  (let [subs (:subs dir-info)
        order-by (keyword (re-find #"\w+" order))
        sort-order (if (.startsWith order "-") - +)]
    (if (empty? subs)
      dir-info
      (assoc dir-info :subs 
        (vec 
          (map 
            (fn [f-info] (subs-sorted f-info order))
            (sort #(sort-order (compare (order-by %1) (order-by %2))) subs)
          )
        )
      )
    )
  )
)