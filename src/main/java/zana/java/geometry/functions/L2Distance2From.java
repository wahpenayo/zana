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
 * @version 2018-04-06
 */

@SuppressWarnings("unchecked")
public final class L2Distance2From extends Function {

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
  public final double doubleValue (final Object x) {
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
    final int n = _target.length;
    assert n == xx.length;
    double s = 0.0;
    double c = 0.0;
    for (int i=0;i<n;i++) {
      // TODO: this isn't right, even as compensated l2 distance
      final double di = _target[i] - xx[i];
      final double zi =  di*di - c;
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
    for (int i=0;i<n;i++) { dx[i] = 2.0*(xx[i] - _target[i]); } 
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
      (o instanceof L2Distance2From)
      &&
      (super.equals(o))
      &&
      Arrays.equals(_target,((L2Distance2From) o)._target); }

  // TODO: protect against large data sets!
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + domain() + "]"; } 
      //+ Arrays.toString(_target); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private L2Distance2From (final double[] target) {
    super(Dn.get(target.length),Dn.get(1)); 
    _target = target; }

  public static final L2Distance2From make (final double[] target) {
    return new L2Distance2From(Arrays.copyOf(target,target.length)); }

  public static final L2Distance2From make (final Object target) {
    if (target instanceof double[]) {
    return new L2Distance2From((double[]) target); }
    if (target instanceof List) {
      final int n = ((List) target).size();
      final double[] t = new double[n];
      for (int i=0;i<n;i++) {
        t[i] = ((Number) ((List) target).get(i)).doubleValue(); } 
      return make(t); }
    throw new IllegalArgumentException(
      "Not a double[] or list of Number:" + target); }

  public static final L2Distance2From generate (final int dimension,
                                            final IFn.D g) {
    final double[] target = new double[dimension];
    for (int i=0;i<dimension;i++) { target[i] = g.invokePrim(); }
    return new L2Distance2From(target); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------