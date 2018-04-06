(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-04-05"
      :doc 
      "Pose unconstrained differentiable optimization problems 
       using `Function` and solve them with the
       commons math3 `NonLinearConjugateGradientOptimizer`." }
    
    zana.optimization.math3.cg
  
  (:require [zana.commons.core :as zcc]
            [zana.collections.generic :as zgc])
  
  (:import [java.util Arrays Collection]
           [clojure.lang IFn]
           [org.apache.commons.math3.analysis
            MultivariateFunction MultivariateVectorFunction]
           [org.apache.commons.math3.optim
            ConvergenceChecker InitialGuess MaxEval MaxIter 
            OptimizationData PointValuePair SimpleBounds]
           [org.apache.commons.math3.optim.nonlinear.scalar
            GoalType]
           [org.apache.commons.math3.optim.nonlinear.scalar
            ObjectiveFunction ObjectiveFunctionGradient]
           [org.apache.commons.math3.optim.nonlinear.scalar.gradient
            NonLinearConjugateGradientOptimizer 
            NonLinearConjugateGradientOptimizer$Formula
            Preconditioner]
           [zana.java.geometry
            Dn]
           [zana.java.geometry.functions
            Function LinearFunctional]))
;;----------------------------------------------------------------
(def ^:private defaults
  "Default options for [[zana.optimization.math3.cg/optimize]]."
  {;; constructor arguments
   ;; :polak-ribiere or :fletcher-reeves
   :update-formula :polak-ribiere
   ;; a predicate function that takes an iteration count, 
   ;; previous value, current value, previous point, current point
   ;; :convergence-test nil
   ;; optimization convergence
   :relative-tolerance 1.0e-6
   :absolute-tolerance 1.0e-6
   ;; line search parameters
   :line-search-relative-tolerance 1.0e-8
   :line-search-absolute-tolerance 1.0e-8
   :initial-bracket-range 1.0e-8
   :preconditioner identity ;; -> IdentityPreconditioner
   ;; optimize() optData
   :objective nil ;; A differentiable Function
   ;; -> ObjectiveFunction and ObjectiveFunctionGradient
   :minimize? true ;; GoalType
   :start nil ;; InitialGuess
   ;; TODO: check what happens if MaxEval is not set.
   :max-evaluations Integer/MAX_VALUE ;; MaxEval
   :max-iterations Integer/MAX_VALUE ;; MaxIter
   })
;;----------------------------------------------------------------
(defn- update-formula
  "Coerces `formula` to one of the values of the 
  `NonLinearConjugateGradientOptimizer$Formula` enum."
  ^NonLinearConjugateGradientOptimizer$Formula [formula]
  (case formula
    :polak-ribiere 
    NonLinearConjugateGradientOptimizer$Formula/POLAK_RIBIERE
    :fletcher-reeves 
    NonLinearConjugateGradientOptimizer$Formula/FLETCHER_REEVES))
;;----------------------------------------------------------------
(deftype ConvergenceWrapper [^IFn converged?]
  ConvergenceChecker
  (converged [_ iteration previous current]
    (converged? iteration previous current)))
(defn- convergence-checker ^ConvergenceChecker [^IFn converged?]
  (ConvergenceWrapper. converged?))
(defn value-convergence ^IFn [^long max-iterations
                              ^double relative-tolerance
                              ^double absolute-tolerance]
  (fn value-converged? [^long iteration
                        ^PointValuePair previous
                        ^PointValuePair current]
    (or (<= max-iterations iteration)
        (let [y0 (.doubleValue ^Double (.getValue previous))
              y1 (.doubleValue ^Double (.getValue current))
              dy (Math/abs (- y1 y0))
              my (Math/max (Math/abs y0) (Math/abs y1))]
          (or (<= dy (* my relative-tolerance))
              (<= dy absolute-tolerance))))))
;;----------------------------------------------------------------
;; skip Preconditioner for now.
;;----------------------------------------------------------------
(deftype ObjectiveWrapper [^Function f]
  MultivariateFunction
  (value [_ p] (.doubleValue f p)))

(defn- objective-function
  ^ObjectiveFunction [^Function f]
  (ObjectiveFunction. (ObjectiveWrapper. f)))
;;----------------------------------------------------------------
(deftype GradientWrapper [^Function f]
  MultivariateVectorFunction
  (value [_ p] 
    (let [df (.derivativeAt f p)]
      (assert (instance? LinearFunctional df)
              (print-str f "\n" df))
      (.dual ^LinearFunctional df))))
(defn- objective-function-gradient
  ^ObjectiveFunctionGradient [^Function f]
  
  (ObjectiveFunctionGradient. (GradientWrapper. f)))
;;----------------------------------------------------------------
;; TODO: look into spec
#_(defn- check
    "Validate options for [[optimize]], throwing
  some exception when one is invalid."
    [options]
    (assert (instance? Function (:objective options))
            (print-str "Not a valid :objective" 
                       (:objective options)))
    
    (let [dim (zgf/domain-dimension (:objective options))]
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
    )
;;----------------------------------------------------------------
;; TODO: generic function/multimethod?

(defn optimize
  
  "Minimize/maximize a differentiable functional over its
   unconstrained domain, using conjugate 
   gradients, as specified in `options`.
   Returns `[x y]` where ``x` is the optimal location in the 
   domain and `y` is the optimal value of the objective."
  
  [options]
  
  (let [options (merge defaults options)
        #_(check options)
        solver (NonLinearConjugateGradientOptimizer.
                 (update-formula (:update-formula options))
                 (convergence-checker
                   (value-convergence
                     (long (:max-iterations options))
                     (:relative-tolerance options)
                     (:absolute-tolerance options)))
                 (double 
                   (:line-search-relative-tolerance options))
                 (double 
                   (:line-search-absolute-tolerance options))
                 (double 
                   (:initial-bracket-range options)))
        objective (:objective options)
        _(assert (:start options)
                 "No :start point in options.")
        start (double-array (:start options))
        _(println :start (Arrays/toString start))
        maxeval (MaxEval. (int (:max-evaluations options)))
        maxiter (MaxIter. (int (:max-iterations options)))
        ^"[Lorg.apache.commons.math3.optim.OptimizationData;"
        opt (into-array OptimizationData 
                        (keep identity  
                              [(objective-function objective)
                               (objective-function-gradient objective)
                               (if (:minimize? options true)
                                 GoalType/MINIMIZE
                                 GoalType/MAXIMIZE) 
                               (InitialGuess. start) 
                               maxeval
                               maxiter]))
        point-value (.optimize solver opt)]
    (println :iterations (.getIterations solver))
    (println :evaluations (.getEvaluations solver))
    [(.getPoint point-value) (.getValue point-value)]))
;;----------------------------------------------------------------
