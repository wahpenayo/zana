package zana.java.geometry.functions;

import java.util.Arrays;

import clojure.lang.IFn;
import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Affine functional on linear spaces.
 * (Affine spaces in the future?)
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class AffineFunctional extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final LinearFunctional _linearPart;
  public final LinearFunctional linearPart () {
    return _linearPart; }
  
  private final double _translation;
  public final double translation () { return _translation; }

  //--------------------------------------------------------------
  // Functional methods
  //--------------------------------------------------------------
  
  @Override
  public final double doubleValue (final Object x) {
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
    return _translation + _linearPart.doubleValue(xx); }

  @Override
  public final Function derivativeAt (final Object x) {
    // an affine functional's derivative is its linear part, 
    // independent of x
    return _linearPart; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = super.hashCode();
    h += 31*_linearPart.hashCode();
    h += 31*Double.hashCode(_translation);
    return h; }

  @Override
  public boolean equals (final Object o) {
    return 
      super.equals(o)
      &&
      (o instanceof AffineFunctional)
      &&
      _translation == ((AffineFunctional) o)._translation
      &&
     _linearPart.equals(((AffineFunctional) o)._linearPart); }
  
  @Override
  public String toString () { 
    return getClass().getSimpleName() + 
      "[" + _linearPart + ", " + _translation + "]"; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private AffineFunctional (final LinearFunctional linearPart,
                            final double translation) {
     super(linearPart.domain(),Dn.get(1));
     _linearPart = linearPart;
     _translation = translation; }

  public static final AffineFunctional make (final LinearFunctional linearPart,
                                              final double translation) {
     return new AffineFunctional(linearPart,translation);}

  public static final AffineFunctional  make (final double[] linearPart,
                                              final double translation) {
     return make(LinearFunctional.make(linearPart), translation); }

  public static final AffineFunctional  make (final double[] homogeneous) {
    final int n = homogeneous.length - 1;
     return 
       make(
         LinearFunctional.make(Arrays.copyOf(homogeneous,n)), 
         homogeneous[n]); }

  public static final AffineFunctional generate (final int dimension,
                                                 final IFn.D gl,
                                                 final IFn.D gt) {
    final LinearFunctional l = LinearFunctional.generate(dimension,gl);
    final double t = gt.invokePrim();
    return make(l,t); }

  public static final AffineFunctional generate (final int dimension,
                                                 final IFn.D g) {
    return generate(dimension,g,g); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------