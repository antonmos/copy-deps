# copy-deps

[![Code at GitHub](https://img.shields.io/badge/code-github-green.svg)](https://github.com/biiwide/copy-deps)
[![Clojars](https://img.shields.io/clojars/v/biiwide/copy-deps.svg)](https://clojars.org/biiwide/copy-deps)

[copy-deps](https://github.com/biiwide/copy-deps) is a Leiningen plugin to copy dependencies into your project's
resources. This is necessary in the case of Java agents for example, which have to be present as jars outside the uberjar,
or native library dependencies.

## Usage

In your `project.clj`, in the `:plugins` section, add:

[![Clojars Project](http://clojars.org/biiwide/copy-deps/latest-version.svg)](http://clojars.org/biiwide/copy-deps)

To run this plug in, execute:

    $ lein copy-deps

If you want the task to run automatically, which is recommended, add:

    :prep-tasks ["javac" "compile" "copy-deps"]

and it'll be invoked every time you build your uberjar. The essential plug-in configuration goes into your `project.clj`
and looks like this:

    :copy-deps {:destination "resources/jars"}

`:destination` specifies where to copy the jars. You can then specify the jars you want to copy in this fashion:

    :copy-deps {:dependencies [[org.clojure/clojure "1.7.0"]]
                :destination  "resources/jars"}

or, if you have `:java-agents` in your project, there's a shortcut to just copy them:

    :jar-copier {:java-agents true
                 :destination "resources/jars"}

They can both be mixed if desired.

A full example using Java agents can be found in
[proclodo-spa-server-rendering](https://github.com/ldnclj/proclodo-spa-server-rendering):

    (defproject proclodo-spa-server-rendering "0.1.0-SNAPSHOT"
      :dependencies [[org.clojure/clojure "1.7.0"]]
      :plugins [[jar-copier "0.1.0"]]
      :prep-tasks ["javac" "compile" "jar-copier"]
      :java-agents [[com.newrelic.agent.java/newrelic-agent "3.20.0"]]
      :copy-deps {:java-agents true
                  :destination "resources/jars"})

## Change log

### v0.6.0 - 2019-04-25
- Forked from https://github.com/pupeno/jar-copier
- Renamed `jar-copier` to `copy-deps`
- Preserve dependency extension when present instead of always using `.jar`.

### v0.4.0 - 2017-01-28
- Changed the groupId to com.pupeno.
- Fixed typo: https://github.com/pupeno/jar-copier/pull/4

### v0.3.1 - 2017-01-28
- Changed metadata to point to the new group for this library.

### v0.3.0 - 2015-11-18
- Better reporting of misconfiguration.
- Thoroughly testing misconfiguration reporting.
- Added the possibility to manually specify the jars (not java-agents).

### v0.2.0 - 2015-09-04
- Changed the groupId to com.carouselapps
- Added a test
- Added travis ci
- Improved documentation

### v0.1.0 - 2015-09-03
- Initial release supporting copying java-agents.

## License

Copyright © 2015-2017 José Pablo Fernández Silva

Distributed under the Eclipse Public License either version 1.0 or (at your option) any later version.
