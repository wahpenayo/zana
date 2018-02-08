package zana.java.data;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableList.Builder;

import clojure.lang.IFn;
import clojure.lang.ISeq;

//----------------------------------------------------------------
/** AKA 'one-hot encoding'.
 *
 * Common code used in mapping records with general attribute
 * values to elements of linear or affine spaces.
 * 
 *  @author wahpenayo at gmail dot com
 * @version 2018-02-07
 */

@SuppressWarnings("unchecked")
public abstract class FlatEmbedding implements IFn, Serializable {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final String _name;
  /** A name, mostly for debugging.
   */
  public final String name () { return _name; }

  // list of [attribute, linearizer] pairs.
  // TODO: use Pair class rather than inner list?
  // TODO: use immutable lists

  private final ImmutableList _attributeEmbeddings;

  /** A list of pairs. Te first element is an EDN serializable
   * key (because attribute functions are not directly 
   * serializable as EDN)
   * and the second is the instance of {@link AttributeEmbedding}
   * to be used for values of that attribute.
   */

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

  public final void updateCoordinates (final Map bindings,
                                       final Object record,
                                       final double[] c) {
    int start = 0;
    for (final Object pair : attributeEmbeddings()) {
      final Object k = ((List) pair).get(0);
      assert (null != k);
      final IFn x = (IFn) bindings.get(k);
      assert (null != x);
      final AttributeEmbedding e = 
        (AttributeEmbedding) ((List) pair).get(1);
      start = 
        e.updateCoordinates(
          x.invoke(record),
          c,
          start); } }

  public abstract double[] embed (final Map bindings,
                                  final Object record);

  @Override
  public final Object invoke (final Object bindings,
                              final Object record) {
    return embed((Map) bindings, record); }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public final String toString () { 
    return name() + "-embedding"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

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
   * @param attributeEmbeddings a list of pairs, where the first
   * element of each pair is 
   */

  public FlatEmbedding (final String name,
                        final List attributeEmbeddings) {
    _name = name;
    _attributeEmbeddings = 
      ImmutableList.copyOf(attributeEmbeddings); }

  //---------------------------------------------------------------
  // TODO: what about Dates, etc.?

  private static final boolean isCategorical (final Object v) {
    return (v instanceof List); }

  private static final boolean isNumerical (final Object v) {
    return 
      (v instanceof Class)
      &&
      ( (v.equals(Byte.TYPE)) || 
        (v.equals(Double.TYPE)) || 
        (v.equals(Float.TYPE)) || 
        (v.equals(Integer.TYPE)) || 
        (v.equals(Long.TYPE)) || 
        (v.equals(Short.TYPE)) || 
        (v.equals(Byte.class)) || 
        (v.equals(Double.class)) ||
        (v.equals(Float.class)) || 
        (v.equals(Integer.class)) ||
        (v.equals(Long.class)) || 
        (v.equals(Short.class))); }

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

  protected static final ImmutableList
  makeAttributeEmbeddings (final List attributeValues) {
    final Builder b = ImmutableList.builder();
    for (final Object av : attributeValues) {
      final Object k = ((List) av).get(0);
      final Object v = ((List) av).get(1);
      final AttributeEmbedding e;
      if (isNumerical(v)) {
        e = NumericalEmbedding.make(k); }
      else if (isCategorical(v)){
        e = CategoricalEmbedding.make(k,(List) v); }
      else {
        throw new IllegalArgumentException(
          "can't construct an embedding for " 
        + k + " and " + v); }
      b.add(ImmutableList.of(k,e)); }
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
  public Object invoke (Object x0) {
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