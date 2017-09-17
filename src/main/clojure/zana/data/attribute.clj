(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "John Alan McDonald" :date "2016-09-06"
      :doc "Attribute function generation."}
    
    zana.data.attribute
  
  (:refer-clojure :exclude [cast emit name])
  (:require [clojure.pprint :as pp]
            [zana.data.reflect :as r]))
;;------------------------------------------------------------------------------
(defn- hint-args [^clojure.lang.IMeta naked-args ^Class c]
  (let [csymbol (symbol (.getName c))
        return-type (case csymbol
                      boolean 'Boolean
                      char 'Character
                      (byte short int long) 'long
                      (float double) 'double
                      csymbol)]
    (with-meta naked-args {:tag return-type})))
;;------------------------------------------------------------------------------
(defn- cast-access [^clojure.lang.IMeta naked-access
                   ^Class c]
  (let [csymbol (symbol (.getName c))
        cast (case csymbol
               boolean 'Boolean/valueOf
               char 'Character/valueOf
               (byte short int long) 'long
               (float double) 'double
               nil)
        access (if cast
                 `(~cast ~naked-access)
                 naked-access)]
    access))
;;------------------------------------------------------------------------------
;; Does this public symbol from another namespace look like an attribute 
;; function?
;; TODO: should attributes be marked with special meta-data?

(defn- attribute? [field-type f]
  (let [ms (meta f)
        arglists (:arglists ms)
        arglist (first arglists)
        arg (first arglist)
        ma (meta arg)
        argtype (eval (:tag ma))]
    (and (== 1 (count arglists))
         (== 1 (count arglist))
         (= field-type argtype))))
;;------------------------------------------------------------------------------
(defn name [defn-expression] 
  (assert (= 'clojure.core/defn (first defn-expression)) 
          (pr-str (first defn-expression) "\n" defn-expression))
  (second defn-expression))
;;------------------------------------------------------------------------------
(defn- qualified-name ^clojure.lang.Symbol [^clojure.lang.Var v]
  (symbol (str (:ns (meta v))) (str (:name (meta v)))))
;;------------------------------------------------------------------------------
(defn- return-type ^clojure.lang.Symbol [^clojure.lang.Var v]
  (r/type (first (:arglists (meta v)))))
;;------------------------------------------------------------------------------
(defn functions [^clojure.lang.Symbol bare-datum-type 
                 ^clojure.lang.Symbol field]
  (let [naked-field (with-meta field {:no-doc true}) ;; turn off codox for now
        datum-type (r/munge bare-datum-type)
        datum (gensym "datum")
        hinted-datum (with-meta datum {:tag datum-type})
        naked-args [hinted-datum]
        field-type (r/type field)
        args (hint-args naked-args field-type)
        accessor (symbol (str "." field))
        naked-access `(~accessor ~hinted-datum)
        access (cast-access naked-access field-type)
        outer `(defn ~naked-field ~args ~access)
        prefix (str field "-")
        inner-defn (fn inner-defn [[^clojure.lang.Symbol unqualified-name 
                                    ^clojure.lang.Var v]]
                     (let [^clojure.lang.Symbol qualified-name (qualified-name v)
                           inner-name (with-meta 
                                        (symbol (str prefix unqualified-name))
                                        {:no-doc true})
                           inner-type (return-type v)
                           inner-call (cast-access `(~qualified-name ~access) inner-type)
                           inner-args (hint-args naked-args inner-type)]
                       `(defn ~inner-name ~inner-args ~inner-call)))
        inner (when (r/datum-class? field-type)
                (mapv inner-defn 
                      (filter (fn [[k v]] (attribute? field-type v)) 
                              (ns-publics (r/namespace field-type)))))]
    (cons outer inner)))
;------------------------------------------------------------------------------
