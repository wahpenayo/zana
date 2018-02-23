package zana.java.geometry;

import java.util.Arrays;

import clojure.lang.IFn;

//----------------------------------------------------------------
/** Linear functionals (dual vectors) on linear spaces.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public final class LinearFunctional extends Functional {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double[] _dual;
  public final double[] dual () { 
    return Arrays.copyOf(_dual,_dual.length); }

  //--------------------------------------------------------------
  // Functional methods
  //--------------------------------------------------------------
  
  @Override
  public final double doubleValue (final double[] x) {
    //assert domain().contains(x);
    return zana.java.arrays.Arrays.dot(_dual,x); }
  
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
    super(Dn.get(dual.length));
    _dual = Arrays.copyOf(dual,dual.length);}

  public static final LinearFunctional make (final double[] dual) {
    return new LinearFunctional(dual); }

  public static final LinearFunctional generate (final int dimension,
                                                 final IFn.D g) {
    final double[] dual = new double[dimension];
    for (int i=0;i<dimension;i++) { dual[i] = g.invokePrim(); }
    return make(dual); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------