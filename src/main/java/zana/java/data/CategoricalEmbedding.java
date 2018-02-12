package zana.java.data;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.common.collect.ImmutableMap;

//----------------------------------------------------------------
/** AKA one-hot encoding.
 *
 * Maps each of <code>n</code> distinct
 * values of a categorical <code>attribute</code> to the linear
 * (vector) coordinates of one of the <code>n+1</code> corners of
 * the unit <code>n</code> simplex (in <b>R</b><sup>n</sup>)
 * The coordinates are returned as <code>double[n]</code>.
 *
 * Any value not occurring in <code>categoryIndex</code> will be
 * mapped  
 * to the origin. Predictive models using this encoding will treat
 * previously unseen values as though they were the most common
 * value in the training data, a simple form of imputation.
 * 
 * Note: for this to be serializable as EDN, all  the categories 
 * must have EDN writers.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-11
 */

@SuppressWarnings("unchecked")
public final class CategoricalEmbedding 
implements AttributeEmbedding {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final String _name;
  public final String name () { return _name; }

  private final ImmutableMap _categoryIndex;

  /** The dimension of the linear space in which values of this
   * categorical attribute are embedded. <code>n+1</code> are
   * embedded as the corners of the unit <code>n</code> simplex
   * in <b>R</b><sup>n</sup>.
   * 
   * The category that maps to the origin is implied, and returns
   * <code>null</code> from <code>categoryIndex</code>.
   * New, unexpected categories will also be mapped to the origin,
   * providing a simple form of imputation for affine/linear
   * models. Thus it is usually best if the origin category is
   * the most frequent or in some other sense a reasonable 
   * default.
   */

  /** The dimension of the linear space in which values of this
   * categorical attribute are embedded. <code>n+1</code> are
   * embedded as the corners of the unit <code>n</code> simplex
   * in <b>R</b><sup>n</sup>.
   * 
   * The category that maps to the origin is implied, and returns
   * <code>null</code> from <code>categoryIndex</code>.
   * New, unexpected categories will also be mapped to the origin,
   * providing a simple form of imputation for affine/linear
   * models. Thus it is usually best if the origin category is
   * the most frequent or in some other sense a reasonable 
   * default.
   */
  public final ImmutableMap categoryIndex () {
    return _categoryIndex; }

  /** The dimension of the linear space in which values of this
   * categorical attribute are embedded. <code>n+1</code> are
   * embedded as the corners of the unit <code>n</code> simplex
   * in <b>R</b><sup>n</sup>.
   * 
   * The category that maps to the origin is implied, and returns
   * <code>null</code> from <code>categoryIndex</code>.
   * New, unexpected categories will also be mapped to the origin,
   * providing a simple form of imputation for affine/linear
   * models. Thus it is usually best if the origin category is
   * the most frequent or in some other sense a reasonable 
   * default.
   */
  private final Number categoryIndex (final Object category) {
    return (Number) _categoryIndex.get(category); }

  //--------------------------------------------------------------
  // AttributeEmbedding interface
  //--------------------------------------------------------------

  /** The dimension of the linear space in which values of this
   * categorical attribute are embedded. <code>n+1</code> are
   * embedded as the corners of the unit <code>n</code> simplex
   * in <b>R</b><sup>n</sup>.
   * 
   * The category that maps to the origin is implied, and returns
   * <code>null</code> from <code>categoryIndex</code>.
   * New, unexpected categories will also be mapped to the origin,
   * providing a simple form of imputation for affine/linear
   * models. Thus it is usually best if the origin category is
   * the most frequent or in some other sense a reasonable 
   * default.
   */
  @Override
  public final int dimension () {
    return _categoryIndex.size(); }

  @Override
  public final int updateCoordinates (final Object category,
                                      final double[] coords,
                                      final int start) {
    final int p = dimension();
    final int end = start + p;
    assert start < coords.length :
      "start: " + start + 
      ", end: " + end + 
      ", coords.length: " + coords.length;

    assert end < coords.length :
      "start: " + start + 
      ", end: " + end + 
      ", coords.length: " + coords.length;

    // might be able to skip this, but hard to do safely
    Arrays.fill(coords,start,end,0.0);
    final Number ii = categoryIndex(category);
    if (null != ii) {
      final int i = ii.intValue();
      assert i < p;
      coords[start+i] = 1.0; }
    return end; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final int hashCode () {
    int h = 17;
    h += 31*_name.hashCode();
    h += 31*_categoryIndex.hashCode();
    return h; }

  @Override
  public boolean equals (final Object o) {
    if (! (o instanceof CategoricalEmbedding)) { return false; }
    
    final CategoricalEmbedding that = (CategoricalEmbedding) o;
    
    if (! _name.equals(that._name)) { return false; }
    
     return _categoryIndex.equals(that._categoryIndex); }
  
 @Override
  public final String toString () { 
    return getClass().getSimpleName() + "[" + name() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public CategoricalEmbedding (final String name,
                               final Map categoryIndex) {
    _name = name;
    // reading from clojure gives Long values
    final ImmutableMap.Builder b = ImmutableMap.builder();
    for (final Object k : categoryIndex.keySet()) {
      final int i = ((Number) categoryIndex.get(k)).intValue();
      b.put(k,Integer.valueOf(i)); }
    _categoryIndex = b.build(); }

  //---------------------------------------------------------------
  /** Construct an embedding in a linear space for values of a
   * categorical attribute. <code>n+1</code> categories are
   * embedded as the corners of the unit <code>n</code> simplex
   * in <b>R</b><sup>n</sup>.
   * 
   * This method will construct a map from the categories to the
   * indexes of the canonical basis vectors for 
   * <b>R</b><sup>n</sup>.
   * 
   * The category that maps to the origin is implied and must be
   * left out of the <code>categories</code> list.
   * New, unexpected categories will also be mapped to the origin,
   * providing a simple form of imputation for affine/linear
   * models. Thus it is usually best if the origin category is
   * the most frequent or in some other sense a reasonable 
   * default.
   */

  public static final CategoricalEmbedding
  make (final Object key,
        final List categories) {
    final Map categoryIndex = new HashMap(categories.size());
    int i = 0;
    for (final Object cat : categories) {
      categoryIndex.put(cat,Integer.valueOf(i));
      i++; }
    return new CategoricalEmbedding(
      key.toString(),
      categoryIndex); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------