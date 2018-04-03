package zana.java.geometry.functions;

import java.util.List;

import com.google.common.collect.ImmutableList;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** A real-valued function on a space of prediction functions, 
 * which encapsulates a collection of training pairs and a 
 * dissimilarity metric.
 * 
 * \[c(f) = \sum_{(x_i,y_i) \in \mathcal{T}} \rho(y_i - f(x_i))\] 
 * where 
 * \(f : \mathbb(X) \rightarrow \mathbb{Y}\) is a prediction model
 * function,
 * \(\mathbb{X}\) is some space of predictor attribute tuples,
 * \(\mathbb{Y}\) is the response space, usually \(\mathbb{Y}\),
 * \(\mathcal{T}\) is a set of training pairs matching known
 * \(x\) and ground truth \(y\) values, 
 * \(\rho\) is a real-valued function measuring the dissimilarity 
 * between \(y_i\) and \(f(x_i)\) for individual pairs.
 * 
 * Note that \(c(f)\) is differentiable with respect to \(f\) if
 * \(\mathbb{Y}\) is a linear space (like 
 * \(\mathbb{R}\) or \(\mathbb{R}^{n}\)), and \(\rho\) is a
 * differentiable function \(\mathbb{R} \rightarrow \mathbb{R}\).
 * 
 * Note that this can be expressed as the composition of
 * reducing and mapping functions, and the derivative computed
 * using the chain rule.
 * 
 * Closely related to 
 * <href a="https://en.wikipedia.org/wiki/M-estimator">
 * M estimators</a>.
 * 
 * TODO: should this be in taiga instead?
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class PredictionCost extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final List _data;

  private final IFn.OD _groundTruth;

  private final Function _rho;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  // TODO: replace Kahan sum with fully accurate?

  @Override
  public final double doubleValue (final Object f) { 
    final Function ff = (Function) f;
    double s = 0.0;
    double c = 0.0;
    for (final Object xi : _data) {
      // TODO: precompute y_i?
      final double yi = _groundTruth.invokePrim(xi);
      final double zi = 
        _rho.doubleValue(ff.doubleValue(xi) - yi) - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  //--------------------------------------------------------------

  private final class DerivativeAt extends Function {
    private static final long serialVersionUID = 0L;
    private final Function _f0;
    @Override
    public final double doubleValue (final Object f) { 
      final Function ff = (Function) f;
      double s = 0.0;
      double c = 0.0;
      for (final Object xi : _data) {
        final double yi = _groundTruth.invokePrim(xi);
        // TODO: fused multiply add for more accuracy?
        // TODO: precompute y_i, f0(x_i), rho_i, ...
        final double zi = 
          (_rho.slopeAt(_f0.doubleValue(xi) - yi) 
            * ff.doubleValue(xi))
          - c;
        final double t = s + zi;
        c = (t - s) - zi;
        s = t; } 
      return s; }

    private DerivativeAt (final Function f) {
      super(
        PredictionCost.this.domain(),
        PredictionCost.this.codomain()); 
      _f0 = f;  }
  }

  @Override
  public final Function derivativeAt (final Object f) { 
    return new DerivativeAt((Function) f); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*_data.hashCode();
    h += 31*_groundTruth.hashCode();
    h += 31*_rho.hashCode();
    return h; }

  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof PredictionCost)
      &&
      (super.equals(o))
      &&
      _data.equals(((PredictionCost) o)._data)
      &&
      _groundTruth.equals(((PredictionCost) o)._groundTruth)
      &&
      _rho.equals(((PredictionCost) o)._rho); }

  // TODO: protect against large data sets!
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + domain() 
      + "," + _data.toString() 
      + "," + _groundTruth.toString() 
      + "," + _rho.toString() 
      + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private PredictionCost (final Function rho,
                          final IFn.OD groundTruth,
                          final List data) {
    super(Function.class,Dn.get(1)); 
    // TODO: check for symmetry, non-negativity, 
    // differentiability, ...
    assert Dn.get(1).equals(rho.domain());
    assert Dn.get(1).equals(rho.codomain());
    _rho = rho; 
    _groundTruth = groundTruth; 
    _data = data; }

  public static final PredictionCost make (final Function rho,
                                           final IFn.OD groundTruth,
                                           final List data) {
    return new PredictionCost(
      rho,
      groundTruth,
      ImmutableList.copyOf(data)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------