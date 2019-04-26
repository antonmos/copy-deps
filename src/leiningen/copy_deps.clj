;;;; Copyright © 2015-2017 José Pablo Fernández Silva

(ns leiningen.copy-deps
  (:require [leiningen.core.main :as lein]
            [cemerick.pomegranate.aether :as aether]
            [clojure.java.io :as io]))


(defn parse-dependency
  [[artifact version & {:as options} :as dep]]
  (merge options
         (meta dep)
         {:artifact artifact
          :version  version}))


(defn- do-copy-deps! [deps destination]
  (doseq [dep (keys (aether/resolve-dependencies :coordinates deps))]
      (let [{:keys [artifact extension file]} (parse-dependency dep)
            dst-file (io/file destination (str artifact "." (or extension "jar")))]
        (when file
          (lein/info "Copying" (.getPath file) "to" (.getPath dst-file))
          (io/make-parents dst-file)
          (io/copy file dst-file)))))


(def ^{:private true} misses? (complement contains?))


(defn copy-deps "Copy a file from your dependencies into your resources."
  [project & args]
  (if (misses? project :copy-deps)
    (lein/warn "copy-deps is not configured. Your Leiningen project needs a :copy-deps entry.")
    (if (misses? (:copy-deps project) :destination)
      (lein/warn "copy-deps is missing the destination configuration. Your :copy-deps entry needs a :destination value.")
      (if (and (misses? (:copy-deps project) :dependencies)
               (misses? (:copy-deps project) :java-agents))
        (lein/warn "copy-deps has nothing to copy, you need to specify :dependencies or :java-agents (or both).")
        (let [destination (get-in project [:copy-deps :destination])]
          (if (and (get-in project [:copy-deps :java-agents])
                   (misses? project :java-agents))
            (lein/warn "copy-deps requested to copy java agents, but no java agents were specified in your project.")
            (do-copy-deps! (:java-agents project) destination))
          (when (contains? (:copy-deps project) :dependencies)
            (do-copy-deps! (get-in project [:copy-deps :dependencies]) destination)))))))
