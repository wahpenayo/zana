package zana.java.geometry.functions;

import java.util.List;

import com.google.common.collect.ImmutableList;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** A real-valued function on a space of prediction functions, 
 * which encapsulates a collection of training pairs and computes
 * sum of squared prediction errors.
 * 
 * \[c(f) = \sum_{(x_i,y_i) \in \mathcal{T}} (y_i - f(x_i)^2\] 
 * where 
 * \(f : \mathbb(X) \rightarrow \mathbb{Y}\) is a prediction model
 * function,
 * \(\mathbb{X}\) is some space of predictor attribute tuples,
 * \(\mathbb{Y}\) is the response space, usually \(\mathbb{Y}\),
 * \(\mathcal{T}\) is a set of training pairs matching known
 * \(x\) and ground truth \(y\) values, 
 * 
 * Note that \(c(f)\) is differentiable with respect to \(f\) if
 * \(\mathbb{Y}\) is a linear space (like 
 * \(\mathbb{R}\) or \(\mathbb{R}^{n}\)).
 * 
 * Note that this can be expressed as the composition of
 * reducing and mapping functions, and the derivative computed
 * using the chain rule.
 * 
 * TODO: should this be in taiga instead?
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-03-01
 */

@SuppressWarnings("unchecked")
public final class L2Cost extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final List _data;

  private final IFn.OD _groundTruth;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  // TODO: replace Kahan sum with fully accurate?

  @Override
  public final double doubleValue (final Function f) { 
    double s = 0.0;
    double c = 0.0;
    for (final Object xi : _data) {
      // TODO: precompute y_i?
      final double yi = _groundTruth.invokePrim(xi);
      final double ei = f.doubleValue(xi) - yi;
      final double zi =  ei*ei - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  //--------------------------------------------------------------

  private final class DerivativeAt extends Function {
    private static final long serialVersionUID = 0L;
    private final Function _f0;
    @Override
    public final double doubleValue (final Function f) { 
      double s = 0.0;
      double c = 0.0;
      for (final Object xi : _data) {
        final double yi = _groundTruth.invokePrim(xi);
        // TODO: fused multiply add for more accuracy?
        // TODO: precompute y_i, f0(x_i), ...
        final double zi = 
          (2 
            * (_f0.doubleValue(xi) - yi) 
            * f.doubleValue(xi))
          - c;
        final double t = s + zi;
        c = (t - s) - zi;
        s = t; } 
      return s; }

    private DerivativeAt (final Function f) {
      super(
        L2Cost.this.domain(),
        L2Cost.this.codomain()); 
      _f0 = f;  }
  }

  @Override
  public final Function derivativeAt (final Function f) { 
    return new DerivativeAt(f); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*_data.hashCode();
    h += 31*_groundTruth.hashCode();
    return h; }

  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof L2Cost)
      &&
      (super.equals(o))
      &&
      _data.equals(((L2Cost) o)._data)
      &&
      _groundTruth.equals(((L2Cost) o)._groundTruth); }

  // TODO: protect against large data sets!
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + domain() 
      + "," + _data.toString() 
      + "," + _groundTruth.toString() 
      + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private L2Cost (final IFn.OD groundTruth,
                  final List data) {
    super(Function.class,Dn.get(1)); 
    _groundTruth = groundTruth; 
    _data = data; }

  public static final L2Cost make (final IFn.OD groundTruth,
                                   final List data) {
    return new L2Cost(
      groundTruth,
      ImmutableList.copyOf(data)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------