;;;; Copyright © 2015-2017 José Pablo Fernández Silva

(ns leiningen.copy-deps
  (:require [leiningen.core.main :as lein]
            [cemerick.pomegranate.aether :as aether]
            [clojure.java.io :as io])
  (:import  (java.nio.file Files)))


(defn parse-dependency
  [[artifact version & {:as options} :as dep]]
  (merge options
         (meta dep)
         {:artifact artifact
          :version  version}))


(defn- readable-file? [x]
  (try
    (let [f (io/file x)]
      (and (.exists f)
           (.isFile f)
           (.canRead f)))
    (catch Exception _ false)))


(defn- same-file? [file-a file-b]
  (Files/isSameFile
    (.toPath (io/file file-a))
    (.toPath (io/file file-b))))


(defn- link [src dst]
  (Files/deleteIfExists
    (.toPath (io/file dst)))
  (try
    (Files/createLink
      (.toPath (io/file dst))
      (.toPath (io/file src)))
    (catch Exception e
      (throw (ex-info "Failed to link dependency!"
                      {:source (str src)
                       :dest   (str dst)
                       :pwd    (.getCanonicalPath (java.io.File. "."))}
                      e)))))


(defn- do-copy-deps! [deps destination verbose?]
  (doseq [dep (keys (aether/resolve-dependencies :coordinates deps))]
    (let [{:keys [artifact extension file]} (parse-dependency dep)
          dst-file (io/file destination (str artifact "." (or extension "jar")))]
      (when (readable-file? file)
        (io/make-parents dst-file)
        (when verbose?
          (lein/info "[copy-deps] Checking:" (.getPath dst-file)))
        (if (and (.exists (io/file dst-file))
                 (same-file? dst-file file))
          (when verbose?
            (lein/info "[copy-deps] Already Linked:" (.getPath dst-file) "to" (.getPath file) ))
          (do (when verbose?
                (lein/info "[copy-deps] Linking:" (.getPath dst-file) "to" (.getPath file)))
              (link file dst-file)))))))


(defn get-destination [project]
  (if-some [destination (get-in project [:copy-deps :destination])]
    (let [dst (io/file destination)]
      (if (.isAbsolute dst)
        dst
        (io/file (:root project) dst)))))


(defn get-verbose? [project]
  (true? (get-in project [:copy-deps :verbose?])))


(def ^{:private true} misses? (complement contains?))


(defn copy-deps "Copy a file from your dependencies into your resources."
  [project & args]
  (if (misses? project :copy-deps)
    (lein/warn "copy-deps is not configured. Your Leiningen project needs a :copy-deps entry.")
    (if-some [destination (get-destination project)]
      (if (and (misses? (:copy-deps project) :dependencies)
               (misses? (:copy-deps project) :java-agents))
        (lein/warn "copy-deps has nothing to copy, you need to specify :dependencies or :java-agents (or both).")
        (let [destination (get-destination project)
              verbose?    (get-verbose? project)]
          (if (and (get-in project [:copy-deps :java-agents])
                   (misses? project :java-agents))
            (lein/warn "copy-deps requested to copy java agents, but no java agents were specified in your project.")
            (do-copy-deps! (:java-agents project) destination verbose?))
          (when (contains? (:copy-deps project) :dependencies)
            (do-copy-deps! (get-in project [:copy-deps :dependencies]) destination verbose?))))
      (lein/warn "copy-deps is missing the destination configuration. Your :copy-deps entry needs a :destination value."))))
