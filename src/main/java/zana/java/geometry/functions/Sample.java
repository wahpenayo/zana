package zana.java.geometry.functions;

import java.util.Arrays;
import java.util.List;

import com.google.common.collect.ImmutableList;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Map a functional (a real-valued function) valued function to 
 * <b>R</b><sup>n</sup> by applying the function to each element 
 * of a list.
 * 
 * Something like an inside-out version of {@link UniDiagonal};
 * this encapsulates the data, {@link UniDiagonal} encapsulates 
 * the function.
 * 
 * Note that this is linear in its double-valued function 
 * argument, so it is its own derivative.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-04
 */

@SuppressWarnings("unchecked")
public final class Sample extends Function  {

  private static final long serialVersionUID = 0L;

  private final List _data;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  /** {@link AffineDual} maps <b>R</b><sup>n+1</sup> 
   * (homogeneous coordinates for the dual vector) to affine 
   * functionals from <b>R</b><sup>n</sup> to <b>R</b>.
   * The composition maps <b>R</b><sup>n+1</sup> to 
   * <b>R</b><sup>m</sup>
   */
  @SuppressWarnings("unused")
  public final LinearRows compose (final AffineDual ad) {
    final int m  = ((Dn) codomain()).dimension();
    final int np1 = ((Dn) ad.domain()).dimension();
    final double[][] rows = new double[m][np1];
    for (int i=0;i<m;i++) {
      final double[] row = rows[i];
      // data must be vectors in R^n for this to work
      final double[] datum = (double[]) _data.get(i);
      final int n = datum.length;
      assert np1 == n+1;
      // represent the composition as a linear function from 
      // <b>R</b><sup>n+1</sup> to <b>R</b><sup>m</sup>
      // using homogeneous coordinates for 'points' in 
      // <b>R</b><sup>n</sup>
      try { 
        System.arraycopy(datum,0,row,0,n); 
        row[n] = 1.0; }
      catch (final Throwable t) {
        System.err.println(np1 + " " + n +
          "\nrow:" + row.length + "\n" + 
          Arrays.toString(row) + 
          "\ndatum:" + datum.length + "\n" + 
          Arrays.toString(datum)); 
        throw t;
      } }
    return new LinearRows(rows); }

  //--------------------------------------------------------------
  // Function methods
  //--------------------------------------------------------------

  @Override
  public final Object value (final Object f) { 
    final Function ff = (Function) f;
    final double[] y = new double[_data.size()];
    int i=0;
    for (final Object x : _data) { y[i++] = ff.doubleValue(x); }
    return y; }

  @Override
  public final Function derivativeAt (final Object f) { 
    return this; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*_data.hashCode();
    return h; }

  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof Sample)
      &&
      (super.equals(o))
      && 
      _data.equals(((Sample) o)._data); }

  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + 
      _data.toString() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  // TODO: restrict domain to double-valued functions?

  private Sample (final ImmutableList data) {
    super(Function.class,Dn.get(data.size()));
    _data = data; }

  public static final Sample make (final List data) {
    return new Sample(ImmutableList.copyOf(data)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------