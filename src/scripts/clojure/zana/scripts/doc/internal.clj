(set! *warn-on-reflection* false)
(set! *unchecked-math* false)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-16"
      :doc 
      "Generate codox for zana internals."}
  
  
  zana.scripts.doc.internal
  
  
  (:require [clojure.java.io :as io]
            [codox.main :as codox])
  #_(:import [org.eclipse.jgit.storage.file FileRepositoryBuilder]))
;----------------------------------------------------------------
#_(set! *warn-on-reflection* true)
#_(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
;; TODO: get this from the pom somehow?
(def version "4.1.0")
(def group "wahpenayo")
(def project-name "zana")
(def description "A random forest library.")
(def doc-files 
  ["docs/0overview.md" "docs/1api.md" "docs/2collections.md" 
   "docs/3commons.md" "docs/4data.md" "docs/5functions.md" 
   "docs/6html.md" "docs/7io.md" "docs/8stats.md" "docs/9time.md" 
   "docs/changes.md"])
#_(def namespaces [#"^zana.api$"])
;;----------------------------------------------------------------
(defn- src-path [subfolder] (str "src/" subfolder "/clojure"))
(defn- src-pattern [subfolder] (re-pattern (src-path subfolder)))
(defn- src-uri [subfolder]
  (str "https://github.com/"
       group
       "/blob/"
       project-name
       "-{version}/"
       (src-path subfolder)
       "/{classpath}#L{line}"))
;; source-uri for a branch rather than a tag
#_(str ""
       branch
       "/--/src/main/clojure/{classpath}#L{line}")
;;:source-uri "file:///{filepath}#line={line}"
;;----------------------------------------------------------------
(let [#_branch #_(.getBranch 
                   (.build 
                     (.findGitDir
                       (.readEnvironment
                         (.setGitDir 
                           (FileRepositoryBuilder.)
                           (io/file ".git"))))))
      source-paths (mapv src-path ["main" "test" "scripts"])
      source-uri (into {} (map #(vector (src-pattern %) (src-uri %))
                               ["main" "test" "scripts"]))
      options {:name project-name
               :version version
               :description description
               :language :clojure
               :root-path (io/file "./")
               :output-path "target/internal-codox"
               :source-paths source-paths
               :source-uri source-uri
               ;;:namespaces namespaces
               ;;:doc-paths ["docs/codox"]
               :doc-files doc-files
               :html {:namespace-list :flat}
               ;;:exclude-vars #"^(map)?->\p{Upper}"
               :metadata {:doc "TODO: write docs"
                          :doc/format :markdown}
               :themes [:default]}]
  (codox/generate-docs options))
;;----------------------------------------------------------------

