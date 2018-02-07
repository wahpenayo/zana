(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com"
      :date "2018-02-06"
      :doc "Tests for zana.data.flatten." }
    
    zana.test.data.affine
  
  (:require [clojure.pprint :as pp]
            [clojure.test :as test]
            [zana.api :as z]
            [zana.test.defs.data.empty :as empty]
            [zana.test.defs.data.primitive :as primitive]
            [zana.test.defs.data.typical :as typical]
            [zana.test.defs.data.change :as change]
            [zana.test.data.setup :as setup])
  (:import [java.util Arrays]
           [zana.test.defs.data.empty Empty]
           [zana.test.defs.data.primitive Primitive]
           [zana.test.defs.data.typical Typical]
           [zana.test.defs.data.change Change]))
;;----------------------------------------------------------------
;;  mvn -Dtest=zana.test.data.affine clojure:test
#_(test/run-tests 'zana.test.data.embed)
(def nss (str *ns*))
;;----------------------------------------------------------------
;; edge case

(test/deftest empty-datum
  (let [empties (setup/empties)
        embed0 (z/affine-embedding "empty" [] empties)
        efile (setup/embed-file nss embed0)
        _ (z/write-edn embed0 efile)
        embed1 (z/read-edn efile)]
    (z/mapc (fn [^Empty x]
              (let [d0 (doubles (embed0 x))
                    d1 (doubles (embed1 x))
                    d2 (doubles (into-array Double/TYPE [1.0]))]
                (test/is (Arrays/equals d0 d1)) 
                (test/is (Arrays/equals d0 d2))))
            empties)))
;;----------------------------------------------------------------
;; no recursion

(test/deftest primitive
  ;; note: skips categorical c
  (let [primitives (setup/primitives)
        embed0 (z/affine-embedding
                 "primitive"
                 primitive/numerical-attributes 
                 primitives)
        efile (setup/embed-file nss embed0)
        _ (z/write-edn embed0 efile)
        embed1 (z/read-edn efile)]
    (defn- to-doubles ^doubles [^Primitive p]
      (into-array Double/TYPE 
                  (conj 
                    (mapv #(% p) primitive/numerical-attributes)
                    1.0)))
    (z/mapc (fn [^Primitive x]
              (let [d0 (doubles (embed0 x))
                    d1 (doubles (embed1 x))
                    d2 (to-doubles x)]
                (test/is (Arrays/equals d0 d1)) 
                (test/is (Arrays/equals d0 d2))))
            primitives)))
;;----------------------------------------------------------------
;; one level of recursion

(test/deftest typical
  (let [embeddable [typical/n 
                      typical/x 
                      typical/string 
                      typical/p-sh 
                      typical/p-i 
                      typical/p-l2-norm2
                      typical/p-l 
                      typical/p-b 
                      typical/p-d 
                      typical/p-f]
        typicals (setup/typicals)
        embed0 (z/affine-embedding 
                 "typical" embeddable typicals)
        efile (setup/embed-file nss embed0)
        _ (z/write-edn embed0 efile)
        embed1 (z/read-edn efile)
        to-doubles (fn to-doubles ^doubles [^Typical t]
                     (into-array Double/TYPE 
                                 [(typical/n t) 
                                  (typical/x t)  
                                  (if (= "Darwin" (typical/string t)) 1.0 0.0) 
                                  (typical/p-sh t)  
                                  (typical/p-i t)  
                                  (typical/p-l2-norm2 t) 
                                  (typical/p-l t)  
                                  (typical/p-b t)  
                                  (typical/p-d t)  
                                  (typical/p-f t)
                                  1.0]))]
    (z/mapc 
      (fn [^Typical x]
        (let [d0 (doubles (embed0 x))
              d1 (doubles (embed1 x))
              d2 (doubles (to-doubles x))]
          (test/is (Arrays/equals d0 d1)) 
          (test/is (Arrays/equals d0 d2))))
      typicals)))
;;----------------------------------------------------------------
;; two levels of recursion

(test/deftest change
  (let [changes (setup/changes)
        embeddable [change/before-x
                      change/before-p-d
                      change/before-p-l
                      change/before-p-f
                      change/before-p-sh
                      change/before-n
                      change/before-string
                      change/before-p-b
                      change/before-p-l2-norm2
                      change/before-p-i
                      change/after-x
                      change/after-p-d
                      change/after-p-l
                      change/after-p-f
                      change/after-p-sh
                      change/after-n
                      change/after-string
                      change/after-p-b
                      change/after-p-l2-norm2
                      change/after-p-i]
        embed0 (z/affine-embedding 
                 "change" embeddable changes)
        efile (setup/embed-file nss embed0)
        _ (z/write-edn embed0 efile)
        embed1 (z/read-edn efile)
        to-doubles (fn to-doubles ^doubles [^Change c]
                     (into-array 
                       Double/TYPE 
                       [(change/before-x c)
                        (change/before-p-d c)
                        (change/before-p-l c)
                        (change/before-p-f c)
                        (change/before-p-sh c)
                        (change/before-n c)
                        (if (= "Darwin" (change/before-string c)) 1.0 0.0)
                        (if (= "Kepler" (change/before-string c)) 1.0 0.0)
                        (change/before-p-b c)
                        (change/before-p-l2-norm2 c)
                        (change/before-p-i c)
                        (change/after-x c)
                        (change/after-p-d c)
                        (change/after-p-l c)
                        (change/after-p-f c)
                        (change/after-p-sh c)
                        (change/after-n c)
                        (if (= "Galileo" (change/after-string c)) 1.0 0.0)
                        (if (= "Kepler" (change/after-string c)) 1.0 0.0)
                        (change/after-p-b c)
                        (change/after-p-l2-norm2 c)
                        (change/after-p-i c)
                        1.0]))]
    #_(pp/pprint (mapv z/name change/attributes))
    #_(pp/pprint (z/frequencies change/before-string changes))
    #_(pp/pprint (z/frequencies change/after-string changes))
    (z/mapc 
      (fn [^Change x]
        (let [d0 (doubles (embed0 x))
              d1 (doubles (embed1 x))
              d2 (doubles (to-doubles x))]
          (test/is (Arrays/equals d0 d1)) 
          (test/is (Arrays/equals d0 d2))))
      changes)))
;;----------------------------------------------------------------