(defproject slide "0.1.0"
  :description "Several utility functions and classes for Swing-based desktop application."
  :url "https://github.com/sgr/slide"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :main logutil
  :dependencies [[org.clojure/clojure "[1.5,)"]
                 [org.clojure/tools.logging "[0.2,)"]
                 [logutil "[0.2,)"]]
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :test-paths ["src/test/clojure"]
  :test-selectors {:default (complement :regression)
                   :regression :regression
                   :all (constantly true)}
  :aot :all
  :plugins [[codox "0.6.4"]
            [lein-javadoc "0.1.1"]]
  :codox {:sources ["src/main/clojure"]
          :output-dir "doc/apidoc-clj"
          :src-dir-uri "https://github.com/sgr/slide/blob/master"
          :src-linenum-anchor-prefix "L"}
  :javadoc-opts {:package-names ["com.github.sgr.slide"
                                 "com.github.sgr.slide.stream"
                                 "com.github.sgr.slide.logging"]
                 :output-dir "doc/apidoc-java"
                 :additional-args ["-encoding" "UTF-8" "-charset" "UTF-8"]})
