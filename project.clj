;;;; Copyright © 2015-2017 José Pablo Fernández Silva

(defproject biiwide/copy-deps "0.7.1"

  :description "Copy files from your dependencies into your resources."

  :url "https://github.com/biiwide/copy-deps"

  :license {:name "Eclipse Public License"
            :url  "http://www.eclipse.org/legal/epl-v10.html"}

  :scm {:name "git"
        :url  "https://github.com/biiwide/copy-deps"}

  :eval-in-leiningen true

  :release-tasks [["vcs" "assert-committed"]
                  ["change" "version" "leiningen.release/bump-version" "release"]
                  ["vcs" "commit"]
                  ["vcs" "tag" "--no-sign"]
                  ["deploy" "clojars"]
                  ["change" "version" "leiningen.release/bump-version"]
                  ["vcs" "commit"]
                  ["vcs" "push"]])
