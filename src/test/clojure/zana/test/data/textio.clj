(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-07-12"
      :doc "Tests for zana.data.datum." }
    
    zana.test.data.textio
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.api :as z]
            [zana.test.defs.data.empty :as empty]
            [zana.test.defs.data.primitive :as primitive]
            [zana.test.defs.data.typical :as typical]
            [zana.test.defs.data.change :as change]
            [zana.test.data.setup :as setup])
  (:import [java.time LocalDateTime LocalDate]
           [zana.test.defs.data.empty Empty]
           [zana.test.defs.data.primitive Primitive]
           [zana.test.defs.data.typical Typical]
           [zana.test.defs.data.change Change]))
;;------------------------------------------------------------------------------
;;  mvn -Dtest=zana.test.data.textio clojure:test
#_(test/run-tests 'zana.test.data.textio)
;;------------------------------------------------------------------------------
;; edge case

(test/deftest empty-datum
  (let [[e0 e1 e2] (setup/empties)
        f (io/file "tst" "empty.tsv")
        _ (empty/write-tsv-file [e0 e1 e2] f)
        es (empty/read-tsv-file f #"\t")]
    (test/is (= "#zana.test.defs.data.empty/Empty {}" (.toString e0)))
    (test/is (= (.toString e0) (.toString e1)))
    (test/is (every? #(instance? Empty %) es))))
;;------------------------------------------------------------------------------
;; no recursion

(test/deftest primitive
  (let [ps0 (setup/primitives)
        r (io/resource "zana/test/data/primitive.ssv")
        ps1 (zana.test.defs.data.primitive/read-tsv-file r #"\s")
        f (io/file "tst" "primitive.tsv")
        ps2 (concat ps0 ps1)
        _ (primitive/write-tsv-file ps2 f)
        ps3 (primitive/read-tsv-file f)]
    (test/is (= "#zana.test.defs.data.primitive/Primitive {:tf true :b 0 :sh 1 :i 2 :l 3 :f 4.0 :d 5.0 :c \\a}"
                (.toString (first ps0))))
    (test/is (== 2 (count ps1)))
    (test/is (== (count ps2) (count ps3)))
    (test/is (every? #(instance? Primitive %) ps0))
    (test/is (every? #(instance? Primitive %) ps1))
    (test/is (every? #(instance? Primitive %) ps2))
    (test/is (every? #(instance? Primitive %) ps3))
    (z/mapc (fn [p0 p1] (test/is (setup/equal-primitives? p0 p1))) ps2 ps3)))
;;------------------------------------------------------------------------------
;; one level of recursion

(test/deftest typical
  (let [ts0 (setup/typicals)
        r (io/resource "zana/test/data/typical.ssv")
        _ (test/is (not (nil? r)))
        ts1 (typical/read-tsv-file r #"\s" typical/header-key)
        f (io/file "tst" "typical.tsv")
        ts2 (concat ts0 ts1)
        _ (typical/write-tsv-file ts2 f " ")
        ts3 (typical/read-tsv-file f #"\s")]
    ;; TODO: need to print dates readably
    #_(test/is 
      (= 
        "#zana.test.defs.data.typical/Typical {:n 17 :x 3.141592653589793 :string \"Galileo\" :p {:tf true :b 0 :sh 1 :i 2 :l 3 :f 4.0 :d 5.0 :c \\a} :ymd #object[java.time.LocalDate 0x281f23f2 \"1564-02-15\"] :dt #object[java.time.LocalDateTime 0x87abc48 \"2015-02-20T10:15:30\"]}"
        (.toString (first ts0))))
    (test/is (== 2 (count ts1)))
    (test/is (== (count ts2) (count ts3)))
    (test/is (every? #(instance? Typical %) ts0))
    (test/is (every? #(instance? Typical %) ts1))
    (test/is (every? #(instance? Typical %) ts2))
    (test/is (every? #(instance? Typical %) ts3))
    (test/is (== 5.0 (typical/p-d (first ts0)))) 
    (test/is (== 5.5 (typical/p-d (second ts0))))
    (z/mapc (fn [t0 t1] (test/is (setup/equal-typicals? t0 t1))) ts2 ts3)))
;;------------------------------------------------------------------------------
;; two levels of recursion

(test/deftest change
  (let [cs0 (setup/changes)
        f (io/file "tst" "change.tsv")
        _ (change/write-tsv-file cs0 f " ")
        cs1 (change/read-tsv-file f #"\s")]
    (test/is (== 6 (count cs0) (count cs1)))
    (test/is (every? #(instance? Change %) cs0))
    (test/is (every? #(instance? Change %) cs1))
    (z/mapc (fn [c0 c1] (test/is (setup/equal-changes? c0 c1))) cs0 cs1)))
;;------------------------------------------------------------------------------