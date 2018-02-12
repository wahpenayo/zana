package zana.java.data;

import java.util.List;

//----------------------------------------------------------------
/** AKA one-hot encoding. Trivial for numerical attributes.
 * 
 * NOTE: except for the name, could be a singleton...
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-11
 */

@SuppressWarnings("unchecked")
public final class NumericalEmbedding 
implements AttributeEmbedding {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final String _name;
  public final String name () { return _name; }

  //--------------------------------------------------------------
  // AttributeEmbedding methods
  //--------------------------------------------------------------

  @Override
  public final int dimension () { return 1; }

  // TODO: primitive value?
  
  @Override
  public final int updateCoordinates (final Object value,
                                      final double[] coords,
                                      final int start) {
    assert start < coords.length :
      "start: " + start + ", coords.length: " + coords.length;
    coords[start] = ((Number) value).doubleValue(); 
    return start+1; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { return _name.hashCode(); }

  @Override
  public boolean equals (final Object o) {
    if (! (o instanceof NumericalEmbedding)) { return false; }
    return _name.equals(((NumericalEmbedding) o)._name); }
  
  @Override
  public final String toString () { 
    return getClass().getSimpleName() + "[" + name() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public NumericalEmbedding (final String name) {
    _name = name; }
  
  //---------------------------------------------------------------
  /** Construct an embedding in a linear space for values of a
   * numerical attribute.
   */
  
  public static final NumericalEmbedding
  make (final Object key) {
    return new NumericalEmbedding(key.toString()); }
  
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------