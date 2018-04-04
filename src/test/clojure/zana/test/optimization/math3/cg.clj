(set! *warn-on-reflection* true)
(set! *unchecked-math* :warn-on-boxed)
(ns ^{:author "wahpenayo at gmail dot com" 
      :date "2018-04-04"
      :doc 
      "Tests for [[zana.optimization.math3.cg]]." }
    
    zana.test.optimization.math3.cg
  
  (:require [clojure.test :as test]
            [zana.api :as z])
  (:import [clojure.lang IFn IFn$OD]
           [zana.java.geometry.functions 
            Composition2 L2Distance2From LinearFunctional 
            LinearRows Function]
           [zana.test.java.geometry.functions
            L2DistanceVariance]))
;; mvn -Dtest=zana.test.optimization.math3.cg clojure:test
;;----------------------------------------------------------------
;; Tests borrowed from 
;; org.apache.commons.math3.optim.nonlinear.scalar.gradient
;; NonLinearConjugateGradientOptimizerTest
;; which is derived from Fortran minpack tests
;;----------------------------------------------------------------
(test/deftest trivial
  (let [lr (LinearRows/make [[2.0]])
        l2d2 (L2Distance2From/make [3.0])
        objective (z/compose l2d2 lr) 
        options {:objective objective
                 :max-iterations 100
                 :start [0.0]}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "trivial" (into [] x) y)
    (test/is (z/approximately== epsilon 0.0 y))
    (test/is (z/approximately== epsilon 1.5 (aget x 0)))))
;;----------------------------------------------------------------
(test/deftest column-permutation
  (let [lr (LinearRows/make 
             [[1.0 -1.0] 
              [0.0  2.0] 
              [1.0 -2.0]])
        l2d2 (L2Distance2From/make [4.0 6.0 1.0])
        objective (z/compose l2d2 lr) 
        options {:objective objective
                 :max-iterations 100
                 :start [0.0 0.0]
                 :relative-tolerance 1.0e-6
                 :absolute-tolerance 1.0e-6
                 :line-search-relative-tolerance 1.0e-3
                 :line-search-absolute-tolerance 1.0e-3
                 :initial-bracket-range 1.0}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "column-permutation" (into [] x) y)
    (test/is (z/approximately== epsilon 0.0 y))
    (test/is (z/approximately== epsilon 7.0 (aget x 0)))
    (test/is (z/approximately== epsilon 3.0 (aget x 1)))))
;;----------------------------------------------------------------
(test/deftest no-dependency
  (let [lr (LinearRows/make 
             [[2 0 0 0 0 0]
              [0 2 0 0 0 0]
              [0 0 2 0 0 0]
              [0 0 0 2 0 0]
              [0 0 0 0 2 0]
              [0 0 0 0 0 2]])
        l2d2 (L2Distance2From/make [0.0 1.1 2.2 3.3 4.4 5.5])
        objective (z/compose l2d2 lr) 
        options {:objective objective
                 :max-iterations 100
                 :start [0 0 0 0 0 0]
                 :relative-tolerance 1.0e-6
                 :absolute-tolerance 1.0e-6
                 :line-search-relative-tolerance 1.0e-3
                 :line-search-absolute-tolerance 1.0e-3
                 :initial-bracket-range 1.0}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "no-dependency" (into [] x) y)
    (test/is (z/approximately== epsilon 0.0 y))
    (dotimes [i 6]
      (test/is (z/approximately== epsilon (* i 0.55) (aget x i))))))
;;----------------------------------------------------------------
(test/deftest one-set
  (let [lr (LinearRows/make 
             [[ 1  0  0]
              [-1  1  0]
              [ 0 -1  1]])
        l2d2 (L2Distance2From/make [1 1 1])
        objective (z/compose l2d2 lr) 
        options {:objective objective
                 :max-iterations 100
                 :start [0 0 0]
                 :relative-tolerance 1.0e-6
                 :absolute-tolerance 1.0e-6
                 :line-search-relative-tolerance 1.0e-3
                 :line-search-absolute-tolerance 1.0e-3
                 :initial-bracket-range 1.0}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "one-set" (into [] x) y)
    (test/is (z/approximately== epsilon 0.0 y))
    (test/is (z/approximately== epsilon 1.0 (aget x 0)))
    (test/is (z/approximately== epsilon 2.0 (aget x 1)))
    (test/is (z/approximately== epsilon 3.0 (aget x 2)))))
;;----------------------------------------------------------------
;    @Test
;    public void testTwoSets() {
;        final double epsilon = 1.0e-7;
;        LinearProblem problem = new LinearProblem(new double[][] {
;                {  2,  1,   0,  4,       0, 0 },
;                { -4, -2,   3, -7,       0, 0 },
;                {  4,  1,  -2,  8,       0, 0 },
;                {  0, -3, -12, -1,       0, 0 },
;                {  0,  0,   0,  0, epsilon, 1 },
;                {  0,  0,   0,  0,       1, 1 }
;        }, new double[] { 2, -9, 2, 2, 1 + epsilon * epsilon, 2});
;
;        final Preconditioner preconditioner
;            = new Preconditioner() {
;                    public double[] precondition(double[] point, double[] r) {
;                        double[] d = r.clone();
;                        d[0] /=  72.0;
;                        d[1] /=  30.0;
;                        d[2] /= 314.0;
;                        d[3] /= 260.0;
;                        d[4] /= 2 * (1 + epsilon * epsilon);
;                        d[5] /= 4.0;
;                        return d;
;                    }
;                };
;
;        NonLinearConjugateGradientOptimizer optimizer
;           = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
;                                                     new SimpleValueChecker(1e-13, 1e-13),
;                                                     1e-7, 1e-7, 1,
;                                                     preconditioner);
;
;        PointValuePair optimum
;            = optimizer.optimize(new MaxEval(100),
;                                 problem.getObjectiveFunction(),
;                                 problem.getObjectiveFunctionGradient(),
;                                 GoalType.MINIMIZE,
;                                 new InitialGuess(new double[] { 0, 0, 0, 0, 0, 0 }));
;
;        final double[] result = optimum.getPoint();
;        final double[] expected = {3, 4, -1, -2, 1 + epsilon, 1 - epsilon};
;
;        Assert.assertEquals(expected[0], result[0], 1.0e-7);
;        Assert.assertEquals(expected[1], result[1], 1.0e-7);
;        Assert.assertEquals(expected[2], result[2], 1.0e-9);
;        Assert.assertEquals(expected[3], result[3], 1.0e-8);
;        Assert.assertEquals(expected[4] + epsilon, result[4], 1.0e-6);
;        Assert.assertEquals(expected[5] - epsilon, result[5], 1.0e-6);
;
;    }
;;----------------------------------------------------------------
(test/deftest not-invertible
  (let [lr (LinearRows/make 
             [[ 1  2 -3]
              [ 2  1  3]
              [-3  0 -9]])
        l2d2 (L2Distance2From/make [1 1 1])
        objective (z/compose l2d2 lr) 
        options {:objective objective
                 :max-iterations 100
                 :start [0 0 0]
                 :relative-tolerance 1.0e-6
                 :absolute-tolerance 1.0e-6
                 :line-search-relative-tolerance 1.0e-3
                 :line-search-absolute-tolerance 1.0e-3
                 :initial-bracket-range 1.0}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "not-invertiable" (into [] x) y)
    (test/is (< 0.5 y))
    #_(test/is (z/approximately== epsilon 1.0 (aget x 0)))
    #_(test/is (z/approximately== epsilon 2.0 (aget x 1)))
    #_(test/is (z/approximately== epsilon 3.0 (aget x 2)))))
;;----------------------------------------------------------------
(test/deftest ill-conditioned-0
  (let [lr (LinearRows/make 
             [[10  7  8  7]
              [ 7  5  6  5]
              [ 8  6 10  9]
              [ 7  5  9 10]])
        l2d2 (L2Distance2From/make [32 23 33 31])
        objective (z/compose l2d2 lr) 
        options {:objective objective
                 :max-iterations 200
                 :start [0 1 2 3]
                 :relative-tolerance 1.0e-13
                 :absolute-tolerance 1.0e-13
                 :line-search-relative-tolerance 1.0e-15
                 :line-search-absolute-tolerance 1.0e-15
                 :initial-bracket-range 1.0}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "ill-conditioned-0" (into [] x) y)
    #_(test/is (< 0.5 y))
    (test/is (z/approximately== 1.0e-4 1.0 (aget x 0)))
    (test/is (z/approximately== 1.0e-3 1.0 (aget x 1)))
    (test/is (z/approximately== 1.0e-4 1.0 (aget x 2)))
    (test/is (z/approximately== 1.0e-4 1.0 (aget x 3)))))
;;----------------------------------------------------------------
(test/deftest ill-conditioned-1
  (let [lr (LinearRows/make 
             [[10.00 7.00 8.10 7.20]
              [ 7.08 5.04 6.00 5.00]
              [ 8.00 5.98 9.89 9.00]
              [ 6.99 4.99 9.00 9.98]])
        l2d2 (L2Distance2From/make [32 23 33 31])
        objective (z/compose l2d2 lr) 
        options {:objective objective
                 :max-iterations 200
                 :start [0 1 2 3]
                 :relative-tolerance 1.0e-13
                 :absolute-tolerance 1.0e-13
                 :line-search-relative-tolerance 1.0e-15
                 :line-search-absolute-tolerance 1.0e-15
                 :initial-bracket-range 1.0}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "ill-conditioned-1" (into [] x) y)
    #_(test/is (< 0.5 y))
    (test/is (z/approximately== 2 -81 (aget x 0)))
    (test/is (z/approximately== 4 137 (aget x 1)))
    (test/is (z/approximately== 1 -34 (aget x 2)))
    (test/is (z/approximately== 1  22 (aget x 3)))))
;;----------------------------------------------------------------
(test/deftest circle-fitting
  (let [^L2DistanceVariance objective (L2DistanceVariance/make 
                                        [[ 30  68]
                                         [ 50  -6]
                                         [110 -20]
                                         [ 35  15]
                                         [ 45  97]])
        options {:objective objective
                 :max-iterations 100
                 :start [98.680 47.345]
                 :relative-tolerance 1.0e-30
                 :absolute-tolerance 1.0e-30
                 :line-search-relative-tolerance 1.0e-15
                 :line-search-absolute-tolerance 1.0e-13
                 :initial-bracket-range 1.0}
        [^doubles x ^double y] (z/optimize-cg options)
        epsilon (double 1.0e-10)]
    (println "circle-fitting" (into [] x) y)
    (test/is (z/approximately== 
               1.0e-8 69.960161753 (.meanL2Distance objective x)))
    (test/is (z/approximately== 
               1.0e-7 96.075902096 (aget x 0)))
    (test/is (z/approximately== 
               1.0e-6 48.135167894 (aget x 1)))))
;;----------------------------------------------------------------
;    @Test
;    public void testMoreEstimatedParametersSimple() {
;        LinearProblem problem = new LinearProblem(new double[][] {
;                { 3.0, 2.0,  0.0, 0.0 },
;                { 0.0, 1.0, -1.0, 1.0 },
;                { 2.0, 0.0,  1.0, 0.0 }
;        }, new double[] { 7.0, 3.0, 5.0 });
;
;        NonLinearConjugateGradientOptimizer optimizer
;            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
;                                                      new SimpleValueChecker(1e-6, 1e-6),
;                                                      1e-3, 1e-3, 1);
;        PointValuePair optimum
;            = optimizer.optimize(new MaxEval(100),
;                                 problem.getObjectiveFunction(),
;                                 problem.getObjectiveFunctionGradient(),
;                                 GoalType.MINIMIZE,
;                                 new InitialGuess(new double[] { 7, 6, 5, 4 }));
;        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);
;
;    }
;
;    @Test
;    public void testMoreEstimatedParametersUnsorted() {
;        LinearProblem problem = new LinearProblem(new double[][] {
;                 { 1.0, 1.0,  0.0,  0.0, 0.0,  0.0 },
;                 { 0.0, 0.0,  1.0,  1.0, 1.0,  0.0 },
;                 { 0.0, 0.0,  0.0,  0.0, 1.0, -1.0 },
;                 { 0.0, 0.0, -1.0,  1.0, 0.0,  1.0 },
;                 { 0.0, 0.0,  0.0, -1.0, 1.0,  0.0 }
;        }, new double[] { 3.0, 12.0, -1.0, 7.0, 1.0 });
;        NonLinearConjugateGradientOptimizer optimizer
;           = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
;                                                     new SimpleValueChecker(1e-6, 1e-6),
;                                                     1e-3, 1e-3, 1);
;        PointValuePair optimum
;            = optimizer.optimize(new MaxEval(100),
;                                 problem.getObjectiveFunction(),
;                                 problem.getObjectiveFunctionGradient(),
;                                 GoalType.MINIMIZE,
;                                 new InitialGuess(new double[] { 2, 2, 2, 2, 2, 2 }));
;        Assert.assertEquals(0, optimum.getValue(), 1.0e-10);
;    }
;
;    @Test
;    public void testRedundantEquations() {
;        LinearProblem problem = new LinearProblem(new double[][] {
;                { 1.0,  1.0 },
;                { 1.0, -1.0 },
;                { 1.0,  3.0 }
;        }, new double[] { 3.0, 1.0, 5.0 });
;
;        NonLinearConjugateGradientOptimizer optimizer
;            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
;                                                      new SimpleValueChecker(1e-6, 1e-6),
;                                                      1e-3, 1e-3, 1);
;        PointValuePair optimum
;            = optimizer.optimize(new MaxEval(100),
;                                 problem.getObjectiveFunction(),
;                                 problem.getObjectiveFunctionGradient(),
;                                 GoalType.MINIMIZE,
;                                 new InitialGuess(new double[] { 1, 1 }));
;        Assert.assertEquals(2.0, optimum.getPoint()[0], 1.0e-8);
;        Assert.assertEquals(1.0, optimum.getPoint()[1], 1.0e-8);
;
;    }
;
;    @Test
;    public void testInconsistentEquations() {
;        LinearProblem problem = new LinearProblem(new double[][] {
;                { 1.0,  1.0 },
;                { 1.0, -1.0 },
;                { 1.0,  3.0 }
;        }, new double[] { 3.0, 1.0, 4.0 });
;
;        NonLinearConjugateGradientOptimizer optimizer
;            = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
;                                                      new SimpleValueChecker(1e-6, 1e-6),
;                                                      1e-3, 1e-3, 1);
;        PointValuePair optimum
;            = optimizer.optimize(new MaxEval(100),
;                                 problem.getObjectiveFunction(),
;                                 problem.getObjectiveFunctionGradient(),
;                                 GoalType.MINIMIZE,
;                                 new InitialGuess(new double[] { 1, 1 }));
;        Assert.assertTrue(optimum.getValue() > 0.1);
;
;    }
;
;    @Test
;    public void testCircleFitting() {
;        CircleScalar problem = new CircleScalar();
;        problem.addPoint( 30.0,  68.0);
;        problem.addPoint( 50.0,  -6.0);
;        problem.addPoint(110.0, -20.0);
;        problem.addPoint( 35.0,  15.0);
;        problem.addPoint( 45.0,  97.0);
;        NonLinearConjugateGradientOptimizer optimizer
;           = new NonLinearConjugateGradientOptimizer(NonLinearConjugateGradientOptimizer.Formula.POLAK_RIBIERE,
;                                                     new SimpleValueChecker(1e-30, 1e-30),
;                                                     1e-15, 1e-13, 1);
;        PointValuePair optimum
;            = optimizer.optimize(new MaxEval(100),
;                                 problem.getObjectiveFunction(),
;                                 problem.getObjectiveFunctionGradient(),
;                                 GoalType.MINIMIZE,
;                                 new InitialGuess(new double[] { 98.680, 47.345 }));
;        Vector2D center = new Vector2D(optimum.getPointRef()[0], optimum.getPointRef()[1]);
;        Assert.assertEquals(69.960161753, problem.getRadius(center), 1.0e-8);
;        Assert.assertEquals(96.075902096, center.getX(), 1.0e-7);
;        Assert.assertEquals(48.135167894, center.getY(), 1.0e-6);
;    }
;
;    private static class LinearProblem {
;        final RealMatrix factors;
;        final double[] target;
;
;        public LinearProblem(double[][] factors,
;                             double[] target) {
;            this.factors = new BlockRealMatrix(factors);
;            this.target  = target;
;        }
;
;        public ObjectiveFunction getObjectiveFunction() {
;            return new ObjectiveFunction(new MultivariateFunction() {
;                    public double value(double[] point) {
;                        double[] y = factors.operate(point);
;                        double sum = 0;
;                        for (int i = 0; i < y.length; ++i) {
;                            double ri = y[i] - target[i];
;                            sum += ri * ri;
;                        }
;                        return sum;
;                    }
;                });
;        }
;
;        public ObjectiveFunctionGradient getObjectiveFunctionGradient() {
;            return new ObjectiveFunctionGradient(new MultivariateVectorFunction() {
;                    public double[] value(double[] point) {
;                        double[] r = factors.operate(point);
;                        for (int i = 0; i < r.length; ++i) {
;                            r[i] -= target[i];
;                        }
;                        double[] p = factors.transpose().operate(r);
;                        for (int i = 0; i < p.length; ++i) {
;                            p[i] *= 2;
;                        }
;                        return p;
;                    }
;                });
;        }
;    }
;}
