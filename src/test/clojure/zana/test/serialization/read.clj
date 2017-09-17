(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2015-11-25"
      :doc "Function serialization experiments." }
    
    zana.test.serialization.read
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.api :as z]))
;;------------------------------------------------------------------------------
(def file (io/file "tst" "fun.bin.gz"))
(defn f ^double [^double x] (* x x))
(let [y 3] (defn g ^double [^double x] (* x y)))
(def fgs [(fn gf ^double [^double x] (g (f x))) 
          (fn fg ^double [^double x] (f (g x)))])
;;------------------------------------------------------------------------------
(defn wrap-test [test]
  (io/make-parents file)
  (with-open [oos (z/object-output-stream file)]
    (.writeObject oos f)
    (.writeObject oos g)
    (.writeObject oos fgs))
  (test)
  (io/delete-file file))
;;------------------------------------------------------------------------------
(test/use-fixtures :each wrap-test)
;;------------------------------------------------------------------------------
(test/deftest functions
  (with-open [ois (z/object-input-stream file)]
    (let [f1 (.readObject ois)
          g1 (.readObject ois)
          fgs1 (.readObject ois)
          [gf fg] fgs
          [gf1 fg1] fgs1]
      (test/is (= (f 2) (f1 2)))
      (test/is (not= f f1))
      (test/is (= (g 2) (g1 2)))
      (test/is (= (gf 2) (gf1 2)))
      (test/is (= (gf 2) (gf1 2)))
      (test/is (fg 2) (fg1 2)))))
;;------------------------------------------------------------------------------