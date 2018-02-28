package zana.java.geometry.functions;

import java.util.Arrays;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Apply a (possibly different) scalar function to each 
 * coordinate.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-27
 */

@SuppressWarnings("unchecked")
public final class Diagonal extends Function  {

  private static final long serialVersionUID = 0L;

  private final Function[] _coordinateTransforms;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  @Override
  public final double[] value (final double[] x) { 
    // assert domain().contains(x);
    final int n = _coordinateTransforms.length;
    assert x.length == n;
    final double[] y = new double[n];
    for (int i=0;i<n;i++) {
      y[i] = _coordinateTransforms[i].doubleValue(x[i]); }
    return y; }

  @Override
  public final Function derivativeAt (final double[] x) { 
    // assert domain().contains(x);
    final int n = _coordinateTransforms.length;
    assert x.length == n;
    final Function[] df = new Function[n];
    for (int i=0;i<n;i++) {
      df[i] = _coordinateTransforms[i].derivativeAt(x[i]); }
    return Diagonal.make(df); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    for (final Function f : _coordinateTransforms) {
      h += 31*f.hashCode(); }
    return h; }

  @Override
  public final boolean equals (final Object o) {
    if (! (o instanceof Diagonal)) { return false; }
    final Diagonal d = (Diagonal) o;
    if (! super.equals(o)) { return false; }
    final int n = _coordinateTransforms.length;
    for (int i=0;i<n;i++) {
      if (! _coordinateTransforms[i]
        .equals(d._coordinateTransforms[i])) {
        return false; } }
    return true; }

  @Override
  public final String toString () {
    // need pretty printing to show all transforms
    return 
      getClass().getSimpleName() + "[" + domain() 
      //+ "," + _coordinateTransforms.toString() 
      + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Diagonal (final Function[] transforms) {
    super(Dn.get(transforms.length),Dn.get(transforms.length));
    _coordinateTransforms = transforms; }

  public static final Diagonal make (final Function[] transforms) {
    return 
      new Diagonal(Arrays.copyOf(transforms,transforms.length)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------