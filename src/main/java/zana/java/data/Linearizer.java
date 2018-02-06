package zana.java.data;

import java.util.List;

import com.google.common.collect.ImmutableList;

import clojure.lang.IFn;

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
 * @version 2018-02-05
 */

@SuppressWarnings("unchecked")
public final class Linearizer extends FlatEmbedding {
  
  private static final long serialVersionUID = 0L;
  
  //--------------------------------------------------------------
  // IFn interface
  //--------------------------------------------------------------
  
  @Override
  public final Object invoke (final Object record) {
    final int n = dimension();
    final double[] coords = new double[n];
    int start = 0;
    for (final Object pair : attributeLinearizers()) {
      final IFn x = (IFn) ((List) pair).get(0);
      final AttributeLinearizer al = 
        (AttributeLinearizer) ((List) pair).get(1);
      start = 
        al.updateCoordinates(
          x.invoke(record),
          coords,
          start); }
    return coords; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final String toString () { 
    return name() + "-linearizer"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  public Linearizer (final String name,
                      final ImmutableList attributeLinearizers) {
    super(name,attributeLinearizers); }
  //---------------------------------------------------------------
  /** Construct an embedding in a linear space 
   * (<b>R</b><sup>n</sup>) for selected attributes and attribute
   * values.
   * 
   * This method will construct a map from record objects to 
   * vectors in <b>R</b><sup>n</sup>, represented by instances
   * of <code>double[n]</code>.
   */
  public static final Linearizer
  make (final String name,
        final List attributeValues) {
    return new Linearizer(name,makeLinearizers(attributeValues)); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------