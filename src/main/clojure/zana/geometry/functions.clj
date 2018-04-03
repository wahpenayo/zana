(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-04-02"
      :doc 
      "Functions between affine/linear spaces." }
    
    zana.geometry.functions
  
  (:require [zana.commons.core :as commons]
            [zana.collections.clojurize :as clojurize]
            [zana.io.edn :as zedn])
  
  (:import [java.util List Map]
           [java.io Serializable Writer]
           [clojure.lang IFn IFn$D IFn$OD]
           [zana.java.arrays Arrays]
           [zana.java.geometry.functions 
            AffineDual AffineFunctional L2Distance2 
            LinearFunctional Sample]))
;;----------------------------------------------------------------
#_(deftype LinearFunctional [^doubles dual]
   Serializable
   IFn$OD 
   (invokePrim ^double [_ v] (Arrays/dot dual v))
   IFn 
   (invoke [this v] (.invokePrim this v))
   Object 
   (equals [_ that] 
     (and (instance? LinearFunctional that)
          (java.util.Arrays/equals 
            dual 
            (doubles (.dual ^LinearFunctional that)))))
   (hashCode [_] (java.util.Arrays/hashCode dual))
   (toString [_] (str "LinearFunctional" (into [] dual))))
;;----------------------------------------------------------------
(defn dual ^doubles [^LinearFunctional lf]
  (.dual lf))
;;----------------------------------------------------------------
(defn linear-functional ^IFn$OD [dual]
  ;; double-array copies if it's already a double[]
  (let [^doubles dual (double-array dual)]
    (LinearFunctional/make dual)))
;;----------------------------------------------------------------
(defn linear-functional? [f] (instance? LinearFunctional f))
;;----------------------------------------------------------------
(defn generate-linear-functional ^IFn$OD [^long dim ^IFn$D g]
  "Generate a linear functional whose coordinates come from
   successive calls to `g`, which is typically a pseudo-random
   numbver generator."
  (LinearFunctional/generate (int dim) g))
;;----------------------------------------------------------------
#_(deftype AffineFunctional [^LinearFunctional linear-part
                            ^double translation]
   Serializable
   IFn$OD 
   (invokePrim ^double [_ v] 
     (+ translation (.invokePrim linear-part v)))
   IFn 
   (invoke [this v] (.invokePrim this v))
   Object 
   (equals [_ that] 
     (and (instance? AffineFunctional that)
          (let [^AffineFunctional that that]
            (and (== translation (.translation that))
                 (.equals linear-part (.linearPart that))))))
   (hashCode [_]    
     (let [h (int 17)
           h (unchecked-multiply-int h (int 31))
           h (unchecked-add-int h (.hashCode linear-part))
           h (unchecked-multiply-int h (int 31))
           h (unchecked-add-int h (Double/hashCode translation))]
       h))
   (toString [_]
     (str "AffineFunctional[" linear-part ", " translation "]")))
;;----------------------------------------------------------------
(defn linear-part ^LinearFunctional [^AffineFunctional af]
  (.linearPart af))
(defn translation ^double [^AffineFunctional af]
  (.translation af))
;;----------------------------------------------------------------
(defn affine-functional 
  
  (^IFn$OD [linear-part ^double translation]
    (let [^LinearFunctional linear-part 
          (cond (instance? LinearFunctional linear-part)
                linear-part
                (or (commons/double-array? linear-part)
                    (instance? List linear-part))
                (linear-functional linear-part)
                :else
                (throw (IllegalArgumentException.
                         (print-str 
                           "can't construct an affine functional:"
                           linear-part translation))))]
      (AffineFunctional/make linear-part translation)))
  
  (^IFn$OD [homogeneous]
    ;; p+1 homogeneous coordinates
    (let [homogeneous (double-array homogeneous)
          n+1 (int (alength homogeneous))
          n (dec n+1)]
      (affine-functional 
        (java.util.Arrays/copyOf homogeneous n)
        (aget homogeneous n)))))
;;----------------------------------------------------------------
(defn affine-functional? [f] (instance? AffineFunctional f))
;;----------------------------------------------------------------
(defn generate-affine-functional 
  "Generate an affine functional whose coordinates come from
   successive calls to generator functions, which are typically a 
   pseudo-random number generators. Two arities allow different
  generators for linear and translation components."
  (^IFn$OD [^long dim ^IFn$D gl ^IFn$D gt]
    (AffineFunctional/generate dim gl gt))
  (^IFn$OD [^long dim ^IFn$D g]
    (AffineFunctional/generate dim g)))
;;----------------------------------------------------------------
;; generic flat functionals
;;----------------------------------------------------------------
(defn flat-functional? [f] 
  (or (linear-functional? f) (affine-functional? f)))
;;----------------------------------------------------------------
(defn domain-dimension ^long [f]
  (cond (linear-functional? f)
        (alength (dual f))
        (affine-functional? f)
        (domain-dimension (linear-part f))
        :else
        (throw 
          (IllegalArgumentException.
            (print-str 
              "can't determine the domain-dimension of" f)))))
;;----------------------------------------------------------------
(defn l2distance2-from 
  "Return a real-valued function that measures the squared l2 
  distance from the supplied point."
  ^L2Distance2 [p]
  (L2Distance2/make p))
;;----------------------------------------------------------------
(defn sampler 
  "Return a function that maps a real-valued function to a vector 
   (<code>double[]</code>) where each output coordinate is the
   value of the function at each datum in a data list."
  ^Sample [data]
  (Sample/make data))
;;----------------------------------------------------------------
(defn affine-dual 
  "Return a function that maps a homogenenous coordinate vector
   to an AffineFunctional."
  ^AffineDual [domain]
  (AffineDual/make domain))
;;----------------------------------------------------------------
;; EDN io
;;----------------------------------------------------------------
(defn map->LinearFunctional ^LinearFunctional [^Map m] 
  (linear-functional (:dual m)))
(defn map<-LinearFunctional ^Map [^LinearFunctional lf] 
  {:dual (into [] (.dual lf))})
(defmethod clojurize/clojurize LinearFunctional [this]
  (map<-LinearFunctional this))
(defmethod print-method LinearFunctional
  [^LinearFunctional this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.geometry.functions.LinearFunctional " )
      (.write w (pr-str (map<-LinearFunctional this))))
    (.write w 
      (print-str (map<-LinearFunctional this)))))
;;----------------------------------------------------------------
(defn map->AffineFunctional ^AffineFunctional [^Map m] 
  (affine-functional (:linear m) (:translation m)))
(defn map<-AffineFunctional 
  ^Map [^AffineFunctional af] 
  {:linear (.linearPart af) :translation (.translation af)})
(defmethod clojurize/clojurize AffineFunctional [this]
  (map<-AffineFunctional this))
(defmethod print-method AffineFunctional
  [^AffineFunctional this ^Writer w]
  (if *print-readably*
    (do
      (.write w " #zana.java.geometry.functions.AffineFunctional " )
      (.write w (pr-str (map<-AffineFunctional this))))
    (.write w (print-str (map<-AffineFunctional this)))))
;;----------------------------------------------------------------
;; EDN input (output just works?)
;;----------------------------------------------------------------
(zedn/add-edn-readers! 
  {'zana.java.geometry.functions.LinearFunctional
   map->LinearFunctional 
   'zana.java.geometry.functions.AffineFunctional 
   map->AffineFunctional})
;;----------------------------------------------------------------
