(defproject slide "0.2.3"
  :description "Several utility functions and classes for Swing-based desktop application."
  :url "https://github.com/sgr/slide"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.6.0"]
                 [org.clojure/tools.logging "0.3.1"]
                 [seesaw "1.4.4"]
                 [logutil "0.2.4"]
                 [org.apache.commons/commons-lang3 "3.3.2"]]
  :source-paths ["src/main/clojure"]
  :java-source-paths ["src/main/java"]
  :resource-paths ["resources"]
  :test-paths ["src/test/clojure"]
  :test-selectors {:default (complement :regression)
                   :gui :gui
                   :dlg :dlg
                   :mlabel :mlabel
                   :llabel :llabel
                   :logging :logging
                   :regression :regression
                   :all (constantly true)}
  :java-cmd ~(let [sys (.toLowerCase (System/getProperty "os.name"))]
               (condp re-find sys
                 #"mac" "/System/Library/Java/JavaVirtualMachines/1.6.0.jdk/Contents/Home/bin/java"
                 (or (System/getenv "JAVA_CMD") "java")))
  :plugins [[codox "0.8.10"]
            [lein-javadoc "0.1.1"]]
  :codox {:sources ["src/main/clojure"]
          :output-dir "doc/apidoc-clj"
          :src-dir-uri "https://github.com/sgr/slide/blob/master/"
          :src-linenum-anchor-prefix "L"}
  :javadoc-opts {:package-names ["com.github.sgr.slide"
                                 "com.github.sgr.slide.stream"
                                 "com.github.sgr.slide.logging"]
                 :output-dir "doc/apidoc-java"
                 :additional-args ["-encoding" "UTF-8" "-charset" "UTF-8"]})
