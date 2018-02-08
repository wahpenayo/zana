package zana.java.data;

import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;

//----------------------------------------------------------------------------
/** AKA 'one-hot encoding'.
 *
 * Convert records with general attribute values to elements
 * of linear spaces --- vectors in <b>R</b><sup>n</sup>
 * represented by instances of <code>double[n]</code>.
 * 
 * Each attribute is mapped to a canonical subspace of 
 * <b>R</b><sup>n</sup>.
 * 
 * Numerical attributes are mapped to a 1-dimensional subspace
 * the span of one of the canonical basis vectors; the attribute's
 * value is the value of that coordinate. (TODO: extend to handle
 * vector valued attributes in the obvious way.)
 * 
 * Categorical attributes are mapped to a 
 * <code>p</code>-dimensional subspace, using a 
 * <code>CategoricalAttributeLinearizer</code>,
 * which is constructed from a list of <code>p</code>
 * values (aka categories). The mapping takes each category in the
 * list to the corresponding canonical basis vector in
 * <b>R</b><sup>p</sup>. Any value for the attribute that is not
 * in the list will be mapped to the origin in 
 * <b>R</b><sup>p</sup>. The usual case is map some reasonable 
 * default category to the origin by dropping it from the list.
 * The reason for this is that categories that were unknown,
 * at the time the <code>CategoricalAttributeLinearizer</code>
 * was constructed, will also map to the origin. Using some
 * reasonable default, like the most frequent value in some 
 * training set, will provide a simple form of imputation for
 * new categories.
 * 
 * (TODO: provide a <code>StrictCategorialLinearizer</code> that
 * throws an exception with new categories?
 * 
 * One thing to think about is what happens when we know all the
 * possible categories ahead of time, but train an affine (linear)
 * model with a training set in which some categories are missing.
 * The corresponding coefficients in the affine model will then be 
 * undetermined. Most types of regularization will force those
 * coefficients to zero, giving the same predicted values as if
 * those categories were mapped to the origin.)
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-07
 */

@SuppressWarnings("unchecked")
public final class LinearEmbedding extends FlatEmbedding {
  
  private static final long serialVersionUID = 0L;
  
  //--------------------------------------------------------------
  // FlatEmbedding interface
  //--------------------------------------------------------------
  
  @Override
  public final double[] embed (final Map bindings,
                              final Object record) {
    final int n = dimension();
    final double[] c = new double[n];
    updateCoordinates(bindings,record,c);
    return c; }

   //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  
  public LinearEmbedding (final String name,
                          final List attributeEmbeddings) {
    super(name,ImmutableList.copyOf(attributeEmbeddings)); }
  
  //---------------------------------------------------------------
  /** Construct an embedding in a linear space 
   * (<b>R</b><sup>n</sup>) for selected attributes and attribute
   * values.
   * 
   * This method will construct a function from record objects to 
   * vectors in <b>R</b><sup>n</sup>, represented by instances
   * of <code>double[n]</code>.
   * 
   * @param attributeValues A list of pairs. The first element is
   * an EDN serializable key (because attribute functions are not
   * directly serializable as EDN)
   * and the second is either a list of values of the 
   * corresponding categorical attribute, or the type of the 
   * attribute's values.
   * If the type is numerical, then a trivial NumericalEmbedding
   * is used. Other types throw an exception (for now).
   */
  
  public static final LinearEmbedding
  make (final String name,
        final List attributeValues) {
    return new LinearEmbedding(
      name,
      makeAttributeEmbeddings(attributeValues)); }
  
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------