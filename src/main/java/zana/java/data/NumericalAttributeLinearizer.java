package zana.java.data;

import clojure.lang.IFn;
import zana.java.functions.Functions;

//----------------------------------------------------------------
/** AKA one-hot encoding. Trivial for numerical attributes.
 *  
 * @author wahpenayo at gmail dot com
 * @version 2018-02-05
 */

@SuppressWarnings("unchecked")
public final class NumericalAttributeLinearizer 
implements AttributeLinearizer {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final String _name;
  public final String name () { return _name; }

  @Override
  public final int dimension () { return 1; }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
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
  public final String toString () { 
    return name() + "-linearizer"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  private NumericalAttributeLinearizer (final String name) {
    _name = name; }
  //---------------------------------------------------------------
  /** Construct an embedding in a linear space for values of a
   * numerical attribute.
   */
  public static final NumericalAttributeLinearizer
  make (final IFn attribute) {
    return new NumericalAttributeLinearizer(
      Functions.name(attribute)); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------