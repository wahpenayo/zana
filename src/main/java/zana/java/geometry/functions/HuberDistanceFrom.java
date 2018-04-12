package zana.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Smoothed (via Huber) l1 'distance' from a 
 * target vector in {@link zana.java.geometry.Dn}.
 * 
 * TODO: factor out differentiable rho.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-06
 */

@SuppressWarnings("unchecked")
public final class HuberDistanceFrom extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double _epsilon;
  private final double _a;
  private final double _b;
  /** target point */
  private final double[] _target;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  private final double rho (final double dy) {
    if (dy >= _epsilon) { return dy + _b; }
    if (dy <= -_epsilon) { return -dy + _b; }
    return _a*dy*dy; }

  private final double drho (final double dy) { 
    if (dy >= _epsilon) { return 1.0; }
    if (dy <= -_epsilon) { return -1.0; }
    return 2.0*_a*dy; }

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
    for (int i=0;i<n;i++) {
      s += rho(_target[i] - xx[i]); } 
//    double c = 0.0;
//    for (int i=0;i<n;i++) {
//      final double zi =  rho(_target[i] - xx[i]) - c;
//      final double t = s + zi;
//      c = (t - s) - zi;
//      s = t; } 
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
      dx[i] = -drho(_target[i] - xx[i]); } 
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
      (o instanceof HuberDistanceFrom)
      &&
      (super.equals(o))
      &&
      Arrays.equals(_target,((HuberDistanceFrom) o)._target); }

  // TODO: protect against large data sets!
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + domain() + "]"; } 
  //+ Arrays.toString(_target); }


  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private HuberDistanceFrom (final double epsilon,
                             final double[] target) {
    super(Dn.get(target.length),Dn.get(1)); 
    _epsilon = epsilon;
    _a = 0.5 / epsilon;
    _b = -0.5 * epsilon; 
    _target = target; }

  public static final HuberDistanceFrom make (final double epsilon,
                                              final double[] target) {
    return new 
      HuberDistanceFrom(epsilon,Arrays.copyOf(target,target.length)); }

  public static final HuberDistanceFrom make (final double epsilon,
                                              final Object target) {
    if (target instanceof double[]) {
      return make(epsilon,(double[]) target); }
    if (target instanceof List) {
      final int n = ((List) target).size();
      final double[] t = new double[n];
      for (int i=0;i<n;i++) {
        t[i] = ((Number) ((List) target).get(i)).doubleValue(); } 
      return new HuberDistanceFrom(epsilon,t); }
    throw new IllegalArgumentException(
      "Not a double[] or list of Number:" + target); }

  public static final HuberDistanceFrom generate (final double epsilon,
                                                  final int dimension,
                                                  final IFn.D g) {
    final double[] target = new double[dimension];
    for (int i=0;i<dimension;i++) { target[i] = g.invokePrim(); }
    return new HuberDistanceFrom(epsilon,target); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------