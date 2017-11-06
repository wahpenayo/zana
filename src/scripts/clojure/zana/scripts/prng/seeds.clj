(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
;;----------------------------------------------------------------
(ns zana.scripts.prng.seeds
  
  {:doc "Generate independent seeds for 
         <code>org.apache.commons.math3.random.RandomGenerator</code>."
   :author "wahpenayo at gmail dot com"
   :since "2017-11-01"
   :date "2017-11-05"}
  
  (:require [clojure.java.io :as io]
            [zana.prob.seed :as seed])
  (:import java.time.LocalDate))
;;----------------------------------------------------------------
#_(def generator seed/generate-randomdotorg-seed)
(def generate seed/generate-default-seed)

;; for Well44497b
(dotimes [i 16]
  (seed/write
    (generate 1391)
    (io/file "src" "main" "resources" "seeds" 
             (str "Well44497b-" (LocalDate/now) "-" 
                  (format "%02d" i) ".edn"))))
;;----------------------------------------------------------------
;; for Mersenne Twister
(dotimes [i 16]
  (seed/write
    (generate 624)
    (io/file  "src" "main" "resources" "seeds" 
              (str "MersenneTwister-" (LocalDate/now) "-" 
                   (format "%02d" i) ".edn"))))
;;----------------------------------------------------------------

