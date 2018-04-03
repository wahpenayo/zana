package zana.java.geometry.functions;

import java.util.Arrays;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** A diagonal linear function. 
 * Only handles domain == codomain for now.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class DiagonalScaling extends Function  {

  private static final long serialVersionUID = 0L;

  private final double[] _diagonal;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  @Override
  public final Object value (final Object x) { 
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
    final int n = _diagonal.length;
    assert xx.length == n;
    final double[] y = new double[n];
    for (int i=0;i<n;i++) { y[i] = _diagonal[i]*xx[i]; }
    return y; }

  @Override
  public final Function derivativeAt (final Object x) { 
    return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*Arrays.hashCode(_diagonal); 
    return h; }

  @Override
  public final boolean equals (final Object o) {
    if (! (o instanceof DiagonalScaling)) { return false; }
    final DiagonalScaling d = (DiagonalScaling) o;
    if (! super.equals(o)) { return false; }
    final int n = _diagonal.length;
    for (int i=0;i<n;i++) {
      if (_diagonal[i] != d._diagonal[i]) { return false; } }
    return true; }

  @Override
  public final String toString () {
    // need pretty printing to show all transforms
    return 
      getClass().getSimpleName() + "[" + domain() 
      //+ "," + _diagonal.toString() 
      + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private DiagonalScaling (final double[] diagonal) {
    super(Dn.get(diagonal.length),Dn.get(diagonal.length));
    _diagonal = diagonal; }

  public static final DiagonalScaling make (final double[] diagonal) {
    return 
      new DiagonalScaling(Arrays.copyOf(diagonal,diagonal.length)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------