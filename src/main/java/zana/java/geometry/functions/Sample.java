package zana.java.geometry.functions;

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
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class Sample extends Function  {

  private static final long serialVersionUID = 0L;

  private final List _data;
  
  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  
  @SuppressWarnings("unused")
  public final LinearRows compose (final AffineDual ad) {
    // TODO: check domains match
    return LinearRows.make(_data); }
  
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
  
  private Sample (final List data) {
    super(Function.class,Dn.get(data.size()));
    _data = ImmutableList.copyOf(data); }
  
  public static final Sample make (final List data) {
    return new Sample(data); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------