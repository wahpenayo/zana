(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-02-15"
      :doc 
      "Pose linear programming problems using 
       `AffineFunctional`and solve them with the
       apache commons math3 `SimplexSolver`." }
    
    zana.optimization.math3.lp
  
  (:require [zana.commons.core :as zcc]
            [zana.collections.generic :as zgc]
            [zana.geometry.functionals :as zgf])
  
  (:import [java.util Collection]
           [org.apache.commons.math3.optim
            InitialGuess MaxEval MaxIter OptimizationData 
            PointValuePair SimpleBounds]
           [org.apache.commons.math3.optim.linear
            LinearConstraint LinearConstraintSet
            LinearObjectiveFunction NonNegativeConstraint 
            Relationship SimplexSolver]
           [org.apache.commons.math3.optim.nonlinear.scalar
            GoalType]
           [zana.geometry.functionals 
            AffineFunctional LinearFunctional]))
;;----------------------------------------------------------------
(def ^:private defaults
  "Default options for [[zana.optimization.math3.lp/optimize]]."
  {:objective nil ;; An AffineFunctional or LinearFunctional
   :constraints nil ;; Sequence of [functional relation] pairs.
   :bounds nil ;; Sequence of numeric [lower upper] pairs -> SimpleBounds
   :nonnegative false ;; NonnegativeConstraint
   :minimize? true ;; GoalTYpe
   :start nil ;; InitialGuess
   ;; TODO: check what happens if MaxEval is not set.
   :max-evaluations Integer/MAX_VALUE ;; MaxEval
   :max-iterations Integer/MAX_VALUE ;; MaxIter
   :convergence-tolerance 1.0e-6
   :==delta (Math/ulp 10.0)
   :pivot-cutoff 1.0e-10
   :pivot-selection-rule :dantzig
   })
;;----------------------------------------------------------------
;; TODO: accept symbols/keywords regardless of namespace?

(defn- relationship 
  
  "Coerces `r` to one of the values of the 
  `org.apache.commons.math3.optim.linear.Relationship` enum.
   `r` may a `Relationship`, function, var, or string
   like `==`, `<=`, `>=`, which translate to `Relationship/EQ`,
   `Relationship/LEQ`, or `Relationship/GEQ`.

   The intent here is to accept any unambiguous specification
   of the numerical (in)equality 
v
       ."
  
  ^Relationship [r]
  (cond 
    (instance? Relationship r)
    r
    
    (or (= clojure.core/== r)
        (= clojure.core/= r)
        (= #'clojure.core/== r)
        (= #'clojure.core/= r)
        (= "==" r)
        (= "=" r))
    Relationship/EQ 
    
    (or (= clojure.core/<= r)
        (= #'clojure.core/<= r)
        (= "<=" r))
    Relationship/LEQ 
    
    (or (= clojure.core/>= r)
        (= #'clojure.core/>= r)
        (= ">=" r))
    Relationship/GEQ 
    
    :else
    (throw 
      (IllegalArgumentException.
        (print-str 
          "Can't coerce to a Relationship:" r)))))
;;----------------------------------------------------------------
;; TODO: generic function/multimethod?
;; TODOL: accept double[] args as well as functionals?

(defn- affine-constraint 
  
  "This constructs an instance of 
  `org.apache.commons.math3.optim.linear.LinearConstraint`.
  That commons math3 class actually represents *affine* 
  constraints, meaning it restricts parameter values to a affine
  hyperplanes or half spaces in <b>R</b><sup>n</sup>.
  (A true linear constraint could only specify hyperplanes or
  halfspaces passing thru the origin.)
  <dl>
  <dt><code>f</code></dt>
  <dd>An <code>AffineFunctional</code> 
  or <code>LinearFunctional</code>.
  <dt><code>r</code>
  <dd>Usually <code><=</code>, <code>==</code>, or 
  <code>>=</code>, or something that can be translated 
  unambiguously, like <code>\"==\"</code>.
  </dl>
  `(affine-constraint f r)` is equivalent to the predicate
  `(r (f x) 0.0)`, or, in more conventional math notation,
   `f(x) <r> 0.0`, where <r> is <=, =, or >=."
  
  ^LinearConstraint [f r]
  
  (cond 
    (instance? AffineFunctional f)
    (let [lhs (zgf/dual (zgf/linear f))
          rhs (- (zgf/translation f))]
      (LinearConstraint. lhs (relationship r) rhs))
    
    (instance? LinearFunctional f)
    (LinearConstraint. (zgf/dual f) (relationship r) 0.0)
    
    :else
    (throw 
      (IllegalArgumentException.
        (print-str 
          "Can't construct a LinearConstraint from:" 
          f "and" r)))))
;;----------------------------------------------------------------
(defn- affine-constraint-set ^LinearConstraintSet [constraints]
  (LinearConstraintSet. 
    ^Collection (mapv affine-constraint constraints)))
;;----------------------------------------------------------------
;; TODO: if we had the dim could return 
;; (SimpleBounds/unbounded dim)

(defn- simple-bounds ^SimpleBounds [bounds]
  (when bounds
    (let [l (zgc/map-to-doubles #(double (first %)) bounds)
          u (zgc/map-to-doubles #(double (second %)) bounds)]
      (SimpleBounds. l u))))
;;----------------------------------------------------------------
;; TODO: generic function/multimethod?

(defn- affine-objective-function
  ^LinearObjectiveFunction [f]
  (cond 
    (instance? AffineFunctional f)
    (LinearObjectiveFunction. (zgf/dual (zgf/linear f))
                              (zgf/translation f))
    
    (instance? LinearFunctional f)
    (LinearObjectiveFunction. (zgf/dual f) 
                              0.0)
    
    :else
    (throw 
      (IllegalArgumentException.
        (print-str 
          "can't construct a LinearObjectFunction from:" f)))))
;;----------------------------------------------------------------
;; TODO: look into spec
(defn- check
  "Validate options for [[optimize]], throwing
  some exception when one is invalid."
  [options]
  (assert (zgf/flat-functional? (:objective options))
          (print-str "Not a valid :objective" 
                     (:objective options)))
  
  (let [dim (zgf/domain-dimension (:objective options))]
    (when-let [constraints (:constraints options)]
      (zgc/mapc 
        (fn [c] 
          (assert (== 2 (count c))
                  (print-str 
                    "not a valid constraint spec:" c))
          (let [[f r] c]
            (assert (== dim (zgf/domain-dimension f))
                    (print-str "domains don't match:" 
                               f (:objective options))) 
            (assert (and (zgf/flat-functional? f)
                         (instance? Relationship (relationship r)))
                    (print-str 
                      "Not a valid constraint spec:" f r)))
          constraints)))
    
    (when-let [bounds (:bounds options)]
      ;; TODO: arrays as an option?
      (assert (== dim (count bounds))
              (print-str "Need" dim "bounds" bounds))
      (zgc/mapc 
        (fn [b]
          (assert (== 2 (count b))
                  (print-str "not a valid bound:" b))
          (let [[^double l ^double u] b]
            (assert (and (number? l) (number? u) (<= l u))
                    (print-str "not valid bounds:" l u))))
        bounds))
    
    (when-let [start (:start options)]
      (assert (and (zcc/double-array? start)
                   
                   (== dim (alength (doubles start))))
              (print-str 
                "Not an element of the objective domain:" start))))
  (assert (< 0 (int (:max-evaluations options)))
          (print-str 
            :max-evaluations "must be a positive integer:" 
            (:max-evaluations options)))
  (assert (< 0 (int (:max-iterations options)))
          (print-str 
            :max-iterations "must be a positive integer:" 
            (int (:max-iterations options))))
  (assert (< 0.0 (double (:convergence-tolerance options)))
          (print-str
            :convergence-tolerance "must be a positive dSouble:" 
            (double (:convergence-tolerance options))))
  (assert (< 0.0 (double (:==delta options)))
          (print-str
            :==delta "must be a positive double:" 
            (double (:==delta options))))
  (assert (< 0.0 (double (:pivot-cutoff options)))
          (print-str
            :pivot-cutoff "must be a positive double:" 
            (double (:pivot-cutoff options))))
  
  (assert (#{:dantzig :bland} (:pivot-selection-rule options))
          (print-str  :pivot-selection-rule "invalid" 
                      (:pivot-selection-rule options))))
;;----------------------------------------------------------------
;; TODO: generic function/multimethod?

(defn optimize
  
  "Solve a linear program specified via `options`.
   Returns `[x y]` where ``x` is the optimal location in the 
   domain and `y` is the optimal value of the objective."
  
  [options]
  
  (let [options (merge defaults options)
        _ (check options)
        solver (SimplexSolver.
                 (double (:convergence-tolerance options))
                 (double (:==delta options))
                 (double (:pivot-cutoff options)))
        objective (affine-objective-function 
                    (:objective options))
        constraints (affine-constraint-set (:constraints options))
        nonnegative (NonNegativeConstraint. 
                      (boolean (:nonnegative options)))
        goaltype (if (:minimize? options)
                   GoalType/MINIMIZE
                   GoalType/MAXIMIZE)
        start (when (:start options) 
                (InitialGuess. (:start options))) 
        bounds (simple-bounds (:bounds options))
        maxeval (MaxEval. (int (:max-evaluations options)))
        maxiter (MaxIter. (int (:max-iterations options)))
        ^"[Lorg.apache.commons.math3.optim.OptimizationData;"
        opt (into-array OptimizationData 
                        (keep identity  
                              [objective constraints nonnegative
                               goaltype 
                               start bounds 
                               maxeval maxiter]))
        point-value (.optimize solver opt)]
    ;; TODO: return the affine dual of the solution?
    [(.getPoint point-value) (.getValue point-value)]))
;;----------------------------------------------------------------
