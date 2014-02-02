(ns dirwalker.view.html
  (:use [dirwalker.file-util])
  (:use [dirwalker.core])
  (:use [hiccup.core :only (html)]))

(declare ^:dynamic cur-order)
(declare ^:dynamic cur-path)

(def ^:private get-file-type-icon "finds an appropriate icon representing a specified file/dir"
  (memoize (fn
    [is_dir name]
    (if is_dir
      "folder.png"
      (let [ext (get-file-extension name)]
        (cond
          (contains? #{"clj", "java", "class", "sh", "cmd", "bat", "pl", "rb", "py"} ext) "code.png"
          (contains? #{"png", "gif", "jpg", "jpeg", "bmp"} ext) "image.png"
          (contains? #{"txt", "text"} ext) "text.png"
          (contains? #{"htm", "html", "js", "css"} ext) "html.png"
          (contains? #{"xml", "xsl", "xsd", "xs", "xq", "fo"} ext) "xml.png"
          (contains? #{"doc", "docx", "xls", "xlsx", "ppt", "pptx"} ext) "office.png"
          (contains? #{"mp3", "wav"} ext) "audio.png"
          (contains? #{"mp4", "flv", "ogg", "mov", "mpeg"} ext) "video.png"
          (contains? #{"pdf"} ext) "pdf.png"
          (contains? #{"zip", "rar", "jar", "war", "ear", "tar", "gz", "bz"} ext) "compressed.png"
          :default "unknown.png"))))))


(defn url-encode "encodes url components"
  [url]
  (if (empty? url)
    "" (java.net.URLEncoder/encode url)))

(defn- create-anchor-for-file "creates anchor element for a dir/file for html,json,xml"
  [type content path]
  [:a {:href (str "?type=" type "&path=" (url-encode path) "&order=" (url-encode cur-order))} content])

(defn- create-image "creates an image element"
  [image alt]
  [:img {:width 22 :height 22 :src (str "/images/" image) :title alt}])

(def ^:private formatter (java.text.SimpleDateFormat. "dd-MMM-yyyy HH:mm:ssZ"))
(defn- format-date "formats a date for display"
  [date-long]
  (if (nil? date-long) "" (.format formatter (java.util.Date. date-long))))

(defn- create-tr-for-file "generates a TR representing a file/dir"
  [name last-modified path size fcount is_dir]
  (let [formatted-size (if size (format "%,d" size) "-")
        formatted-fcount (if fcount (format "%,d" fcount) "-")]
    [:tr
      [:td (if (or (= name ".") (= name "..")) 
             " " 
             (create-image (get-file-type-icon is_dir name) name))]
      [:td (if is_dir (create-anchor-for-file "html" name path) name)]
      [:td {:class "date"} (format-date last-modified)]
      [:td {:align "right"} formatted-size]
      [:td {:align "right"} formatted-fcount]
      [:td {:align "center"} (create-anchor-for-file "xml" (create-image "xml.png" (str name " - in XML")) path)]
      [:td {:align "center"} (create-anchor-for-file "json" (create-image "json.png" (str name " - in JSON")) path)]]))

(defn- create-anchor-for-sortable-header "creates an anchor as table header which allows sorting files by, for example, size, name"
  [content field-name default-sort]
  (let [cur-sort-field-name (re-find #"\w+" cur-order)
        cur-sort-order (re-find #"\W+" cur-order)]
    [:a 
       {:href (str "?path=" (url-encode cur-path) "&order=" 
                   (str (if (= field-name cur-sort-field-name) 
                           (condp = cur-sort-order
                             nil "-"
                             "+" "-"
                             "-" (url-encode "+")
                             "-"
                           ) 
                           default-sort) field-name)
              )
       }
       content
    ]))

(defn gen-response-html
  "generates response html for a dir-info map"
  [{:keys [name last is_dir path size fcount subs]}
   {:keys [server-name server-port server-brand app-version]}
   order]
  (binding [cur-order order cur-path path]
    (html
      [:html
        [:head
          [:title (str "Index of " cur-path)]
          [:meta {:charset "UTF-8"}]
          [:link {:rel "stylesheet" :href "/css/dw.css"} ]]
        [:body
          [:div {:class "h1"} (str "Index of " cur-path)]
          [:table {:cellspacing 0 :cellpadding 3}
            [:tr {:class "header"}
              [:th (create-anchor-for-sortable-header "Type" "is_dir" "-")]
              [:th (create-anchor-for-sortable-header "Name" "name" "+")]
              [:th (create-anchor-for-sortable-header "Last Modified" "last" "-")]
              [:th (create-anchor-for-sortable-header "Size" "size" "-")]
              [:th (create-anchor-for-sortable-header "Files" "fcount" "-")]
              [:th "XML"]
              [:th "JSON"]]
            (create-tr-for-file "." last cur-path size fcount true)
            (create-tr-for-file ".." nil (parent-path cur-path) nil nil true)
            (for [{:keys [name last is_dir path size fcount]} subs]
              (create-tr-for-file name last path size fcount is_dir))]
          [:div {:class "address"}
            (seq [[:span (str "DirWalker " app-version " running on " server-name ":" server-port ".")]
              [:br]
              [:span (str "Powered by " server-brand ". Developer: Alfred Xiao")]])]]])))
