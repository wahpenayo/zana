package zana.java.geometry.functions;

import java.util.Arrays;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Linear functionals (dual vectors) on linear spaces.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-03-31
 */

@SuppressWarnings("unchecked")
public final class LinearFunctional extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double[] _dual;
  public final double[] dual () { 
    return Arrays.copyOf(_dual,_dual.length); }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  
  public final LinearFunctional compose (final LinearRows lr) {
    final int m = ((Dn) lr.codomain()).dimension();
    final int n = ((Dn) lr.domain()).dimension();
    final double[] d = new double[n];
    for (int j=0;j<n;j++) {
      double s = 0.0;
      for (int i=0;i<m;i++) {
        s = Math.fma(_dual[i],lr.coordinate(i,j),s); }
      d[j] = s; }
    return new LinearFunctional(d); }
  
  //--------------------------------------------------------------
  // Functional methods
  //--------------------------------------------------------------
  
  @Override
  public final double doubleValue (final double[] x) {
    //assert domain().contains(x);
    return zana.java.arrays.Arrays.dot(_dual,x); }
  
  @Override
  public final Function derivativeAt (final double[] x) {
    // a linear functional is its own derivative, independent of x
    return this; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = super.hashCode();
    h += 31*Arrays.hashCode(_dual);
    return h; }

  @Override
  public boolean equals (final Object o) {
    return 
      super.equals(o)
      &&
      (o instanceof LinearFunctional)
      &&
     Arrays.equals(_dual,((LinearFunctional) o)._dual); }
  
  @Override
  public String toString () { 
    return getClass().getSimpleName() + Arrays.toString(_dual); }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private LinearFunctional (final double[] dual) {
    super(Dn.get(dual.length),Dn.get(1));
    _dual = dual; }

  public static final LinearFunctional make (final double[] dual) {
    return new LinearFunctional(Arrays.copyOf(dual,dual.length)); }

  public static final LinearFunctional generate (final int dimension,
                                                 final IFn.D g) {
    final double[] dual = new double[dimension];
    for (int i=0;i<dimension;i++) { dual[i] = g.invokePrim(); }
    return new LinearFunctional(dual); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------