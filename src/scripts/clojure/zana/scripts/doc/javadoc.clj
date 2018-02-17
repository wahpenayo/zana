#_(ns ^{:author "John Alan McDonald" :date "2016-08-29"
       :doc "Run javadoc on zana java source." }
    
     zana.scripts.doc.javadoc)
;;------------------------------------------------------------------------------
;; need to add jdkX.Y.Z_bbb/lib/tools.jar to classpath for this to work.
;; probably easier to use maven or a shell script

#_(com.sun.tools.javadoc.Main/execute
   (into-array 
     ["-d" "target/javadoc" 
      "-sourcepath" "src/main/java"
      "-doctitle" "Zana Java classes"
      "-overview" "src/main/java/overview.html"
      "-public"
      "-version" 
      "-author" 
      "-quiet"
      "zana.java"])
;;------------------------------------------------------------------------------

