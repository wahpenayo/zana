#_(ns ^{:author "John Alan McDonald" :date "2016-08-24"}
    
     zana.scripts.doc.marginalia
  
   #_(:use marginalia.core 
          [marginalia.html :only (*resources*)]))
;;------------------------------------------------------------------------------
#_(binding [*resources* ""]
   (ensure-directory! "target/marginalia")
   (uberdoc!
     "target/marginalia/uberdoc.html"
     (format-sources ["src/main/clojure"])
     {:dev-dependencies []
      :name "zana"
      :description "CLojure utilities."
      :version "3.0.0"
      :dependencies [["marginalia/marginalia" "0.9.0"]]}))
;;------------------------------------------------------------------------------
