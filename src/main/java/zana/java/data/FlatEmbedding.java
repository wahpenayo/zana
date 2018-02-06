package zana.java.data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import clojure.lang.IFn;
import clojure.lang.ISeq;

//----------------------------------------------------------------------------
/** AKA 'one-hot encoding'.
 *
 * Common code used in mapping records with general attribute 
 * values to elements of linear (<code>Linearizer</code>) or 
 * affine (<code>Homogenizer</code>) spaces.
 * 
 *  @author wahpenayo at gmail dot com
 * @version 2018-02-05
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
  private final ImmutableList _attributeLinearizers;
  public final ImmutableList attributeLinearizers () {
    return _attributeLinearizers; }

  //--------------------------------------------------------------
  // methods
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
  public final int dimension () {
    int n = 0;
    for (final Object pair : _attributeLinearizers) {
      final AttributeLinearizer al = 
        (AttributeLinearizer)  ((List) pair).get(1);
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
                        final List attributeLinearizers) {
    _name = name;
    _attributeLinearizers = 
      ImmutableList.copyOf(attributeLinearizers); }
  //---------------------------------------------------------------
  /** Construct an embedding in a linear space 
   * (<b>R</b><sup>n</sup>) for selected attributes and attribute
   * values.
   * 
   * This method will construct a map from record objects to 
   * vectors in <b>R</b><sup>n</sup>, represented by instances
   * of <code>double[n]</code>.
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
  makeLinearizers (final List attributeValues) {
    final int n = attributeValues.size();
    final Builder b = ImmutableList.builder();
    for (final Object av : attributeValues) {
      final IFn a = (IFn) ((List) av).get(0);
      final AttributeLinearizer al;
      if (isNumerical(a)) {
        al = NumericalAttributeLinearizer.make(a); }
      else {
        al = CategoricalAttributeLinearizer.make(
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