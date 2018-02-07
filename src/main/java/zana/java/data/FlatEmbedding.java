package zana.java.data;

import java.io.Serializable;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import clojure.lang.IFn;
import clojure.lang.ISeq;

//----------------------------------------------------------------------------
/** AKA 'one-hot encoding'.
 *
 * Common code used in mapping records with general attribute 
 * values to elements of linear or affine spaces.
 * 
 *  @author wahpenayo at gmail dot com
 * @version 2018-02-06
 */

@SuppressWarnings("unchecked")
public abstract class FlatEmbedding implements IFn, Serializable {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final String _name;
  public final String name () { return _name; }

  // list of [attribute, linearizer] pairs.
  // TODO: use Pair class rather than inner list?
  // TODO: use immutable lists
  private final ImmutableList _attributeEmbeddings;
  public final ImmutableList attributeEmbeddings () {
    return _attributeEmbeddings; }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  /** The dimension of the flat space in which record objects
   * are embedded.
   * 
   * Each numerical attribute adds 1 dimension.
   * 
   * Categorical attributes with p+1 categories add p dimensions,
   * the p+1 values being mapped to the corners of the unit 
   * p-simplex in <b>R</b><sup>p</sup> or <b>E</b><sup>p</sup>.
   */
  
  public final int dimension () {
    int n = 0;
    for (final Object pair : _attributeEmbeddings) {
      final AttributeEmbedding al = 
        (AttributeEmbedding)  ((List) pair).get(1);
      n += al.dimension(); }
    return n; }

  //--------------------------------------------------------------
  // TODO: what about Dates, etc.?

  public static final boolean isNumerical (final IFn attribute) {
    return 
      (attribute instanceof IFn.OD)
      ||
      (attribute instanceof IFn.OL); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------
  public FlatEmbedding (final String name,
                        final List attributeEmbeddings) {
    _name = name;
    _attributeEmbeddings = 
      ImmutableList.copyOf(attributeEmbeddings); }
  //---------------------------------------------------------------
  /** Construct an embedding in a linear space 
   * (<b>R</b><sup>n</sup>) for selected attributes and attribute
   * values.
   * 
   * This method will construct a map from record objects to 
   * vectors/points in <b>R</b><sup>n</sup>
   * (or <b>E</b><sup>n</sup>),
   * represented by instances
   * of <code>double[n]</code> (or <code>double[n+1]</code>).
   * 
   * The category that maps to the origin is implied and must be
   * left out of the <code>categories</code> list.
   * New, unexpected categories will also be mapped to the origin,
   * providing a simple form of imputation for affine/linear
   * models. Thus it is usually best if the origin category is
   * the most frequent or in some other sense a reasonable 
   * default.
   */
  public static final ImmutableList
  makeAttributeEmbeddings (final List attributeValues) {
    final Builder b = ImmutableList.builder();
    for (final Object av : attributeValues) {
      final IFn a = (IFn) ((List) av).get(0);
      final AttributeEmbedding al;
      if (isNumerical(a)) {
        al = NumericalEmbedding.make(a); }
      else {
        al = CategoricalEmbedding.make(
          a,(List) ((List) av).get(1)); }
      b.add(ImmutableList.of(a,al)); }
    return b.build(); }

  //--------------------------------------------------------------
  // unsupported IFn operations
  //--------------------------------------------------------------
  @Override
  public Object call () throws Exception {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public void run () {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object applyTo (ISeq x0) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke () {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0, Object x1) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0, Object x1, Object x2) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0, Object x1, Object x2, Object x3) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13,Object x14) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13,Object x14,
                        Object x15) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13,Object x14,
                        Object x15,Object x16) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13,Object x14,
                        Object x15,Object x16,Object x17) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13,Object x14,
                        Object x15,Object x16,Object x17,Object x18) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13,Object x14,
                        Object x15,Object x16,Object x17,Object x18,Object x19) {
    throw new UnsupportedOperationException(getClass().getName()); }
  @Override
  public Object invoke (Object x0,Object x1,Object x2,Object x3,Object x4,
                        Object x5,Object x6,Object x7,Object x8,Object x9,
                        Object x10,Object x11,Object x12,Object x13,Object x14,
                        Object x15,Object x16,Object x17,Object x18,Object x19,
                        Object... x20) {
    throw new UnsupportedOperationException(getClass().getName()); }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------