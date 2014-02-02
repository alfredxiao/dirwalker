(ns dirwalker.webserver
  (use [dirwalker.file-util])
  (use [dirwalker.core])
  (use [dirwalker.view.html])
  (use [dirwalker.view.xml])
  (use [dirwalker.view.json])
  (use [ring.adapter.jetty :only (run-jetty)])
  (use [ring.middleware.params :only (wrap-params)])
  (use [compojure.core]))

(def server-name (.getHostName (java.net.InetAddress/getLocalHost)))
(declare server-port)
(def server-brand "clojure/ring/compojure/jetty/hiccup/tools.cli/data.json")
(def app-version "v1.0")

(defn- gen-response-body "generates response body string according to requested format/types, default is html"
  [path type order descendents]
  (let [dir-info (subs-sorted (get-dir-info-cached path nil descendents) order)
        server-info {:server-name server-name :server-port server-port :server-brand server-brand :app-version app-version}]
    (condp = type
      "json" (gen-response-json dir-info)
      "xml" (gen-response-xml dir-info)
      "html" (gen-response-html dir-info server-info order)
      (gen-response-html dir-info server-info order))))

(defn- gen-response-image "generates a response displaying an image"
  [image]
  { :status 200
    :headers { "Content-Type" "image/png" }
    :body (java.io.File.  (str (System/getProperty "user.dir") "/web/images/" image))
  })

(defn- gen-response-plain "generates a response displaying an plain file"
  [rel-file]
  { :status 200
    :headers { "Content-Type" "text/plain" }
    :body (java.io.File.  (str (System/getProperty "user.dir") "/web/" rel-file))
  })

(defn run-dw-server "runs web/rest server at specified port and home directory"
  [root-dir port]
  (defn- dw-handler
    "handles web requests (GET) and returns http response in specified format, e.g. JSON, XML, HTML."
    [request]
    (let [ {:keys [path type descendents order] :or {path root-dir type "html" order "-size"}} (keyword-map (:params request))
           descendents-val (Boolean/valueOf descendents) ]
      (cond
        (not (under? path root-dir))
          { :status 403
            :headers {"Content-Type" "text/plain"}
            :body (format "path %s not allowed." path)}
        (not (exists? path)) 
          { :status 404
            :headers {"Content-Type" "text/plain"}
            :body (format "path %s not found." path)}
        :default 
          { :status 200
            :headers { "Content-Type" (condp = type
                                        "json" "application/json"
                                        "xml" "application/xml"
                                        "html" "text/html"
                                        "text/html")}
            :body (gen-response-body path type order descendents-val)})))
  (defroutes dw-routes
    (GET "/"  req (wrap-params dw-handler))
    (GET "/images/:image"  [image] (gen-response-image image))
    (GET "/css/:css"  [css] (gen-response-plain (str "/css/" css)))
    (GET "/js/:js"  [js] (gen-response-plain (str "/js/" js))))
  (def server-port port)
  (run-jetty dw-routes {:port port :join? false}))