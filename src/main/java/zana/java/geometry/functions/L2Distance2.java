package zana.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Squared l2 distance from a target vector in
 *  {@link zana.java.geometry.Dn}.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-03-30
 */

@SuppressWarnings("unchecked")
public final class L2Distance2 extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double[] _target;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  // TODO: replace Kahan sum with fully accurate?

  @Override
  public final double doubleValue (final double[] x) {
    final int n = _target.length;
    assert n == x.length;
    double s = 0.0;
    double c = 0.0;
    for (int i=0;i<n;i++) {
      // TODO: this isn't right, even as compensated l2 distance
      final double di = _target[i] - x[i];
      final double zi =  di*di - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  @Override
  public final Function derivativeAt (final double[] x) { 
    final int n = _target.length;
    assert n == x.length;
    final double[] dx = new double[n];
    for (int i=0;i<n;i++) { dx[i] = 2.0*(x[i] - _target[i]); } 
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
      (o instanceof L2Distance2)
      &&
      (super.equals(o))
      &&
      Arrays.equals(_target,((L2Distance2) o)._target); }

  // TODO: protect against large data sets!
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + Arrays.toString(_target); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private L2Distance2 (final double[] target) {
    super(Dn.get(target.length),Dn.get(1)); 
    _target = target; }

  public static final L2Distance2 make (final double[] target) {
    return new L2Distance2(Arrays.copyOf(target,target.length)); }

  public static final L2Distance2 make (final Object target) {
    if (target instanceof double[]) {
    return new L2Distance2((double[]) target); }
    if (target instanceof List) {
      final int n = ((List) target).size();
      final double[] t = new double[n];
      for (int i=0;i<n;i++) {
        t[i] = ((Number) ((List) target).get(i)).doubleValue(); } 
      return make(t); }
    throw new IllegalArgumentException(
      "Not a double[] or list of Number:" + target); }

  public static final L2Distance2 generate (final int dimension,
                                            final IFn.D g) {
    final double[] target = new double[dimension];
    for (int i=0;i<dimension;i++) { target[i] = g.invokePrim(); }
    return new L2Distance2(target); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------