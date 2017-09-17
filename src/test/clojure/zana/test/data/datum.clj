(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-06-30"
      :doc "Tests for zana.data.datum." }
    
    zana.test.data.datum
  
  (:require [clojure.java.io :as io]
            [clojure.test :as test]
            [zana.test.defs.data.empty :as empty]
            [zana.test.defs.data.primitive :as primitive]
            [zana.test.defs.data.typical :as typical]
            [zana.test.defs.data.change :as change]
            [zana.test.data.setup :as setup]))
;;------------------------------------------------------------------------------
;; mvn -Dtest=zana.test.data.datum clojure:test
#_(test/run-tests 'zana.test.data.datum)
;;------------------------------------------------------------------------------
;; edge case

(test/deftest empty-datum
  (let [[e0 e1 e2] (setup/empties)]
    ;; Datums have identity semantics
    (test/is (empty? empty/fields))
    (test/is (not= e0 e1))
    (test/is (not= e1 e2))
    (test/is (not= e2 e0))
    (test/is (not= (.hashCode e0) (.hashCode e1)))
    (test/is (not= (.hashCode e1) (.hashCode e2)))
    (test/is (not= (.hashCode e2) (.hashCode e0)))))
;;------------------------------------------------------------------------------
;; no recursion

(test/deftest primitive
  (let [[p0 p1 p2 p3 p4] (setup/primitives)]
    (test/is (= [primitive/b primitive/sh primitive/i primitive/l primitive/f 
                 primitive/d]
                primitive/numerical-attributes))
    (test/is (= [primitive/tf primitive/c] primitive/non-numerical-attributes))
    (test/is (== 6.0 (:d p2)))
    (test/is (== 0.0 (primitive/d p3)))
    (test/is (== 5.0 (primitive/d p4)))
    ;; Datums have identity semantics
    (test/is (= primitive/fields
                [primitive/tf primitive/b primitive/sh primitive/i primitive/l
                 primitive/f primitive/d primitive/c]))
    (test/is (not= p0 p1))
    (test/is (not= p0 p2))
    (test/is (not= (.hashCode p0) (.hashCode p1)))
    (test/is (not= (.hashCode p0) (.hashCode p2)))
    (test/is (== (primitive/b p0) (primitive/b p1)))
    (test/is (not (== (primitive/b p0) (primitive/b p2))))
    (test/is (instance? clojure.lang.IFn$OL primitive/b))
    (test/is (instance? clojure.lang.IFn$OL primitive/sh))
    (test/is (instance? clojure.lang.IFn$OL primitive/i))
    (test/is (instance? clojure.lang.IFn$OL primitive/l))
    (test/is (instance? clojure.lang.IFn$OD primitive/f))
    (test/is (instance? clojure.lang.IFn$OD primitive/d))
    (test/is (= (into {} p0)
                {:tf true :b 0 :sh 1 :i 2 :l 3 :f 4.0 :d 5.0 :c \a}))
    ;; could simplify  z/define-datum code if memfn type-hinted the return value
    ;;(test/is (not (instance? clojure.lang.IFn$OD (memfn ^Primitive d))))
    ))
;;------------------------------------------------------------------------------
;; one level of recursion

(test/deftest typical
  (let [[t0 t1 t2 t3 t4] (setup/typicals)]
    (test/is (= (set typical/numerical-attributes)
                #{typical/n typical/x typical/p-b typical/p-sh typical/p-i 
                  typical/p-l typical/p-f typical/p-d typical/p-l2-norm2}))
    (test/is (= (set typical/non-numerical-attributes)
                #{typical/string typical/p typical/p-tf typical/p-c typical/ymd 
                  typical/dt}))
    ;; Datums have identity semantics
    (test/is (not= t0 t1))
    (test/is (not= t0 t2))
    (test/is (not= (.hashCode t0) (.hashCode t1)))
    (test/is (not= (.hashCode t0) (.hashCode t2)))
    (test/is (not= (typical/p t0) (typical/p t1)))
    (test/is (not= (typical/p t0) (typical/p t2)))
    (test/is (== (typical/x t0) (typical/x t1)))
    (test/is (== (typical/p-i t0) (primitive/i (typical/p t1))))
    (test/is (== (typical/p-i t0) (get-in t1 [:p :i])))
    (test/is (= (setup/galileo-birthdate) (typical/ymd t0)))
    (test/is (not (== (typical/n t0) (typical/n t2))))
    (test/is (instance? clojure.lang.IFn$OL typical/n))
    (test/is (instance? clojure.lang.IFn$OD typical/x))))
;;------------------------------------------------------------------------------
;; two levels of recursion

(test/deftest change
  (let [[c00 c01 c10 c11 c20 c21] (setup/changes)]
    (test/is (== 1 (typical/p-sh (change/before c01))))
    (test/is (== 1 (primitive/sh (typical/p (change/before c01)))))
    (test/is (== 1 (change/before-p-sh c01)))
    (test/is (== 1 (get-in c01 [:before :p :sh])))
    (test/is (== (double 6.0) (double (get-in c01 [:after :p :d]))))))
;;------------------------------------------------------------------------------