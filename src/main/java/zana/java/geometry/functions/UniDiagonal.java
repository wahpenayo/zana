package zana.java.geometry.functions;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Apply a scalar function to each coordinate.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class UniDiagonal extends Function  {

  private static final long serialVersionUID = 0L;

  private final Function _coordinateTransform;
  
  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  
  @Override
  public final Object value (final Object x) { 
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
    final int n = xx.length;
    final double[] y = new double[n];
    for (int i=0;i<n;i++) {
      y[i] = _coordinateTransform.doubleValue(xx[i]); }
    return y; }
  
  @Override
  public final Function derivativeAt (final Object x) { 
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
       final int n = xx.length;
       final Function[] df = new Function[n];
       for (int i=0;i<n;i++) {
         df[i] = _coordinateTransform.derivativeAt(xx[i]); }
    return Diagonal.make(df); }
  
  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------
  
  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*_coordinateTransform.hashCode();
    return h; }
  
  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof UniDiagonal)
      &&
      (super.equals(o))
      && 
      _coordinateTransform.equals(
        ((UniDiagonal) o)._coordinateTransform); }
  
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + domain() + ","
      + _coordinateTransform.toString() + "]"; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private UniDiagonal (final Object domain,
                       final Function transform) {
    super(domain,domain);
    _coordinateTransform = transform; }
  
  public static final UniDiagonal make (final Object domain,
                                        final Function transform) {
    return new UniDiagonal(domain,transform); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------