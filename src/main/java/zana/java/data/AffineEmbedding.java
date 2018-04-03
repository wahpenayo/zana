package zana.java.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

//----------------------------------------------------------------
/** AKA 'one-hot encoding'.
 *
 * Convert records with general attribute values to elements
 * of affine spaces --- points in <b>E</b><sup>n</sup>
 * represented by instances of <code>double[n+1]</code>
 * holding homogeneous coordinates.
 * 
 * This is identical to a <code>LinearEmbedding</code>, except 
 * that it adds a constant 1.0 as the last element in the
 * returned arrays, supporting affine models (linear plus constant 
 * term).
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class AffineEmbedding extends FlatEmbedding {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // FlatEmbedding interface
  //--------------------------------------------------------------

  @Override
  public final double[] embed (final Map bindings,
                               final Object record) {
    final int n = dimension();
    final double[] c = new double[n+1];
    updateCoordinates(bindings,record,c);
    c[n] = 1.0;
    return c; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------
  
  @Override
  public final boolean equals (final Object o) {
    if (! (o instanceof AffineEmbedding)) { return false; }
    return super.equals(o); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public AffineEmbedding (final String name,
                          final List attributeEmbeddings) {
    super(name,ImmutableList.copyOf(attributeEmbeddings)); }

  //---------------------------------------------------------------
  /** Construct an embedding in an affine space 
   * (<b>E</b><sup>n</sup>) for selected attributes and attribute
   * values.
   * This method will construct a function from record objects to 
   * vectors in <b>R</b><sup>n</sup>, represented by instances
   * of <code>double[n]</code>.
   * 
   * @param attributeValues A list of pairs. The first element is
   * an EDN serializable key (because attribute functions are not
   * directly serializable as EDN)
   * and the second is either a list of values of the 
   * corresponding categorical attribute, or the type of values.
   * If the type is numerical, then a trivial NumericalEmbedding
   * is used. Other types throw an exception (for now).
   */

  public static final AffineEmbedding
  make (final String name,
        final List attributeValues) {
    return
      new AffineEmbedding(
        name,
        makeAttributeEmbeddings(attributeValues)); }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------