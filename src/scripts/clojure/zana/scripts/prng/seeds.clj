(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns zana.scripts.prng.seeds
  
  {:doc "Generate independent seeds for 
         <code>org.apache.commons.math3.random.RandomGenerator</code>."
   :author "wahpenayo at gmail dot com"
   :since "2017-11-01"
   :date "2017-11-01"}
  
  (:require [clojure.java.io :as io]
            [zana.prob.seed :as seed])
  (:import java.time.LocalDate))
;;----------------------------------------------------------------
;; for Well44497b
#_(seed/write
   (seed/generate-randomdotorg-seed 1391)
   (io/file "src" "main" "resources" "seeds" 
            (str "Well44497b-" (LocalDate/now) ".edn")))
;;----------------------------------------------------------------
;; for Mersenne Twister
(dotimes [i 16]
  (seed/write
    (seed/generate-randomdotorg-seed 624)
    (io/file  "src" "main" "resources" "seeds" 
              (str "MersenneTwister-" (LocalDate/now) "-" 
                   (format "%02d" i) ".edn"))))
;;----------------------------------------------------------------

