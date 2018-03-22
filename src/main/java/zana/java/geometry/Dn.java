package zana.java.geometry;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

//----------------------------------------------------------------
/** 'Linear' spaces whose elements are instances of 
 * <code>double[n]</code>.
 * 
 * TODO: extend/implement 'Space' ?
 *
 * @author wahpenayo at gmail dot com
 * @version 2018-02-27
 */


@SuppressWarnings("unchecked")
public final class Dn implements Serializable {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final int _dimension;
  public final int dimension () { return _dimension; }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  
//  public final double[] makeElement () { 
//    return new double[_dimension]; }
  
  
  
  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return _dimension; }

  @Override
  public boolean equals (final Object o) {
    if (! (o instanceof Dn)) { return false; }
    return _dimension == ((Dn) o)._dimension; }

  @Override
  public final String toString () { 
    return "L" + _dimension; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  /** Public <code>get</code> tries to return a singleton.
   */
  private Dn (final int dimension) { _dimension = dimension; }

  // TODO: use a primitive int -> space map?
  
  private static final Map SINGLETONS = new HashMap(32);
  
  public static final Dn get (final int dimension) {
    final Integer n = Integer.valueOf(dimension);
    Dn ln = (Dn) SINGLETONS.get(n);
    if (null == ln) {
      ln = new Dn(dimension);
      SINGLETONS.put(n,ln); }
    return ln; }
  //--------------------------------------------------------------
}
//----------------------------------------------------------------