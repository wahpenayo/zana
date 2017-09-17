(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-11-07"
      :doc "Tests for zana.io.paths." }
    
    zana.test.io.paths
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.api :as z]))
;;------------------------------------------------------------------------------
(comment
  mvn -Dtest=zana.test.io.paths clojure:test
  (test/run-tests 'zana.test.io.paths)
  )
;;------------------------------------------------------------------------------
(def ^:private files [(io/file "src" "main" "clojure" "zana" "io" "paths.clj")
                      (io/file "src" "main" "clojure" "zana" "io" "gz.clj")
                      (io/file "src" "main" "clojure" "zana" "io" "edn.clj")
                      ])
;; edge case
(test/deftest ls-missing
  (let [folder (io/file "does" "not" "exist")]
    (test/is (nil? (z/ls folder)))
    (test/is (nil? (z/ls "[\\-_.A-Za-z0-9]+\\.clj" folder)))
    (test/is (nil? (z/ls (re-pattern "[\\-_.A-Za-z0-9]+\\.clj") folder)))
    (test/is (nil? (z/ls #(.endsWith (z/pathname %) ".clj") folder)))))
;;------------------------------------------------------------------------------
(test/deftest ls
  (let [folder (io/file "src" "main" "clojure" "zana" "io")] ))

;    (test/is (= files (z/ls folder)))
;    (test/is (= files (z/ls "[\\-_.A-Za-z0-9]+\\.clj" folder)))
;    (test/is (= files (z/ls (re-pattern "[\\-_.A-Za-z0-9]+\\.clj") folder)))
;    (test/is (= files (z/ls #(.endsWith (z/pathname %) ".clj") folder)))))
;;------------------------------------------------------------------------------
