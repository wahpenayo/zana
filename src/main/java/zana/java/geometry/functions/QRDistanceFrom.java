package zana.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Smoothed (via Huber) quantile regression 'distance' from a 
 * target vector in {@link zana.java.geometry.Dn}.
 * 
 * TODO: factor out differentiable rho.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-04
 */

@SuppressWarnings("unchecked")
public final class QRDistanceFrom extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  /** target quantile */
  private final double _p;
  /** cost is quadratic within the interval
   * <code>[(1.0-_p)*_epsilon),_p*_epsilon]</code> 
   */
  private final double _epsilon;
  /** target point */
  private final double[] _target;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  private final double rho (final double dy) {
    if (dy <= -_p*_epsilon) { 
      return ((_p-1)*dy) + (-0.5*_p*(1.0-_p)*_epsilon); }
    if (dy <= 0.0) { 
      return ((0.5*(1.0-_p)) / (_p*_epsilon))*dy*dy; }
    if (dy <= (1.0-_p)*_epsilon) { 
      return ((0.5*_p) / ((1.0-_p)*_epsilon))*dy*dy; }
    return (_p*dy) + (-0.5*_p*(1.0-_p)*_epsilon); }

  private final double drho (final double dy) { 
    if (dy <= -_p*_epsilon) { 
      return (_p-1.0); }
    if (dy <= 0.0) { 
      return ((1.0-_p) / (_p*_epsilon))*dy; }
    if (dy <= (1.0-_p)*_epsilon) { 
      return (_p / ((1.0-_p)*_epsilon))*dy; }
    return _p; }

  //--------------------------------------------------------------
  // Function methods
  //--------------------------------------------------------------
  // TODO: replace Kahan sum with fully accurate?

  @Override
  public final double doubleValue (final Object x) {
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
    final int n = _target.length;
    assert n == xx.length;
    double s = 0.0;
    double c = 0.0;
    for (int i=0;i<n;i++) {
      final double zi =  rho(_target[i] - xx[i]) - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  @Override
  public final Function derivativeAt (final Object x) { 
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
     final int n = _target.length;
    assert n == xx.length;
    final double[] dx = new double[n];
    for (int i=0;i<n;i++) { 
      dx[i] = drho(_target[i] - xx[i]); } 
    return LinearFunctional.make(dx); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*Arrays.hashCode(_target);
    return h; }

  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof QRDistanceFrom)
      &&
      (super.equals(o))
      &&
      Arrays.equals(_target,((QRDistanceFrom) o)._target); }

  // TODO: protect against large data sets!
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + Arrays.toString(_target); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private QRDistanceFrom (final double p,
                          final double epsilon,
                          final double[] target) {
    super(Dn.get(target.length),Dn.get(1)); 
    _p = p;
    _epsilon = epsilon;
    _target = target; }

  public static final QRDistanceFrom make (final double p,
                                           final double epsilon,
                                           final double[] target) {
    return new 
      QRDistanceFrom(p,epsilon,Arrays.copyOf(target,target.length)); }

  public static final QRDistanceFrom make (final double p,
                                           final double epsilon,
                                           final Object target) {
    if (target instanceof double[]) {
    return make(p,epsilon,(double[]) target); }
    if (target instanceof List) {
      final int n = ((List) target).size();
      final double[] t = new double[n];
      for (int i=0;i<n;i++) {
        t[i] = ((Number) ((List) target).get(i)).doubleValue(); } 
      return new QRDistanceFrom(p,epsilon,t); }
    throw new IllegalArgumentException(
      "Not a double[] or list of Number:" + target); }

  public static final QRDistanceFrom generate (final double p,
                                               final double epsilon,
                                               final int dimension,
                                               final IFn.D g) {
    final double[] target = new double[dimension];
    for (int i=0;i<dimension;i++) { target[i] = g.invokePrim(); }
    return new QRDistanceFrom(p,epsilon,target); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------