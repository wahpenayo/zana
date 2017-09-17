;; codox has reflection and boxed math warnings
(ns ^{:author "John Alan McDonald" :date "2016-09-16"
      :doc "Run codox on zana clojure source." }
    
    zana.scripts.doc.codox
  
  (:require [clojure.java.io :as io]
            [codox.main :as codox])
  (:import [org.eclipse.jgit.storage.file FileRepositoryBuilder]))
;;------------------------------------------------------------------------------
(let [branch (.getBranch 
               (.build 
                 (.findGitDir
                   (.readEnvironment
                     (.setGitDir 
                       (FileRepositoryBuilder.)
                       (io/file ".git"))))))
      properties (java.util.Properties.)
      _ (with-open [r (io/reader (io/resource "version.properties"))]
          (.load properties r))
      version (.getProperty properties "version")
      build (.getProperty properties "build.timestamp")]
  (codox/generate-docs
    {:name "Zana" 
     :version (str version " (built: " build " UTC)")
     :description "Clojure utilities."
     :root-path "."
     :source-paths ["src/main/clojure"]
     :namespaces [#"\.api$"];; "zana.collections.generic" "zana.collections.guava"]
     :metadata {:doc "---needs documentation---" :doc/format :markdown}
     :doc-paths ["src/doc/codox"]
     :output-path "target/codox"
     :source-uri (str ""
                      branch
                      "/--/src/main/clojure/{classpath}#L{line}")}))
;;------------------------------------------------------------------------------

