;;;; Copyright © 2015-2017 José Pablo Fernández Silva

(ns leiningen.copy-deps-test
  (:require [clojure.test :refer :all]
            [leiningen.copy-deps :refer :all]
            [clojure.java.io :as io]))

(deftest no-copy-deps-config-issues-warning
  (let [warnings (atom 0)]
    (with-redefs [leiningen.core.main/warn (fn [& _] (swap! warnings inc))]
      (copy-deps {}))
    (is (= 1 @warnings))))

(deftest no-destination-issues-warning
  (let [warnings (atom 0)]
    (with-redefs [leiningen.core.main/warn (fn [& _] (swap! warnings inc))]
      (copy-deps {:copy-deps {}}))
    (is (= 1 @warnings))))

(deftest nothing-to-copy-issues-warning
  (let [warnings (atom 0)]
    (with-redefs [leiningen.core.main/warn (fn [& _] (swap! warnings inc))]
      (copy-deps {:copy-deps {:destination "target/jars"}}))
    (is (= 1 @warnings))))

(deftest lack-of-java-agents-issues-warning
  (let [warnings (atom 0)]
    (with-redefs [leiningen.core.main/warn (fn [& _] (swap! warnings inc))]
      (copy-deps {:copy-deps {:java-agents true
                              :destination "target/jars"}}))
    (is (= 1 @warnings))))

(deftest copy-java-agent
  (copy-deps {:java-agents '[[org.clojure/clojure "1.7.0"]]
              :copy-deps   {:java-agents true
                            :destination "target/jars"}})
  (is (.exists (io/as-file "target/jars/org.clojure/clojure.jar")))
  (io/delete-file "target/jars/org.clojure/clojure.jar"))

(deftest copy-plain-jar
  (copy-deps {:copy-deps {:dependencies '[[org.clojure/clojure "1.7.0"]]
                          :destination  "target/jars"}})
  (is (.exists (io/as-file "target/jars/org.clojure/clojure.jar")))
  (io/delete-file "target/jars/org.clojure/clojure.jar"))

(deftest copy-plain-jar-and-java-agents
  (copy-deps {:java-agents '[[org.clojure/clojure "1.7.0"]]
              :copy-deps   {:dependencies '[[org.clojure/clojure-contrib "1.2.0"]]
                            :java-agents  true
                            :destination  "target/jars"}})
  (is (.exists (io/as-file "target/jars/org.clojure/clojure.jar")))
  (is (.exists (io/as-file "target/jars/org.clojure/clojure-contrib.jar")))
  (io/delete-file "target/jars/org.clojure/clojure.jar")
  (io/delete-file "target/jars/org.clojure/clojure-contrib.jar"))

(deftest copy-plain-non-jar
  (copy-deps {:copy-deps {:dependencies '[[org.clojure/clojure "1.7.0" :extension "pom"]]
                          :destination  "target/misc"}})
  (is (.exists (io/as-file "target/misc/org.clojure/clojure.pom")))
  (io/delete-file "target/misc/org.clojure/clojure.pom"))

