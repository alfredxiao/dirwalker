(ns dirwalker.file-util
  (:require [clojure.java.io :as jio]))

(defn get-file "returns a java.io.File object via a path string"
  [path] 
  (jio/file path))

(defn list-sub-files "lists sub files (files and dirs) of a dir"
  [dir] 
  (seq (.listFiles dir)))

(defn matcher-by-pattern "creates a matcher using wildcards, e.g. *.txt"
  [pattern]
  (if pattern
    (fn [file]
      (let [fpath (.getFileName (.toPath file))
            matcher (.getPathMatcher (java.nio.file.FileSystems/getDefault) (str "glob:" pattern))]
        (.matches matcher fpath)))
    (fn [file] true)))

(defn exists? "returns true if a file specified by the provided path string exists."
  [path]
  (.exists (get-file path)))

(defn keyword-map "convert a map whose keys are string to a map whose keys are keywords"
  [m]
  (reduce #(assoc %1 (first %2) (second %2)) {}  (for [k (keys m)] [(keyword k) (get m k)])))

(defn under? "checks whether a path is actually under a root dir, aiming to prevent .. hacking"
  [path root-dir]
  (.startsWith 
    (.getCanonicalPath (get-file path))
    (.getCanonicalPath (get-file root-dir))))

(defn parent-path "returns parent folder's path"
  [path]
  (.getCanonicalPath (.getParentFile (get-file path))))

(defn get-file-extension "gets the extension of a file"
  [name]
  (let [index (.lastIndexOf name ".")]
    (if (>= index 0)
      (.toLowerCase (.substring name (inc index)))
      "")))
