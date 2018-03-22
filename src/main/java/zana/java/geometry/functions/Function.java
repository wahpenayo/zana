package zana.java.geometry.functions;

import java.io.Serializable;

import clojure.lang.IFn;
import clojure.lang.ISeq;

//----------------------------------------------------------------
/** Base class for functions from and to geometric spaces.
 *
 * @author wahpenayo at gmail dot com
 * @version 2018-03-14
 */

@SuppressWarnings("unchecked")
public abstract class Function
implements IFn, IFn.DD, IFn.OD, Serializable {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final Object _domain;
  public final Object domain () { return _domain; }

  private final Object _codomain;
  public final Object codomain () { return _codomain; }

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  /** If the {@link #codomain() codomain} of this function is
   * 1-dimensional, return the value as a <code>double</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double doubleValue (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** If the {@link #codomain() codomain} of this function is
   * 1-dimensional, and the {@link #domain() domain} contains
   * {@link Function functions}
   * return the value as a <code>double</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double doubleValue (final Function x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** If the {@link #codomain() codomain} of this function is
   * 1-dimensional, and the {@link #domain() domain} contains
   * <code>double[]</code> arrays,
   * return the value as a <code>double</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double doubleValue (final double[] x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** If the {@link #codomain() codomain} and
   * {@link #codomain() codomain} of this function are
   * 1-dimensional, take a <code>double</code> argument return the
   * value as a <code>double</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double doubleValue (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //--------------------------------------------------------------
  /** If the elements of the {@link #codomain() codomains} can be
   * represented by <code>double[]</code>,
   * return the value as a <code>double[]</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double[] value (final Object f) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** If the elements of the {@link #codomain() codomains} can be
   * represented by <code>double[]</code>
   * can, and the {@link #domain() domain} contains
   * {@link Function functions},
   * return the value as a <code>double[]</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double[] value (final Function f) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** If the elements of the {@link #codomain() codomain} can be
   * represented by <code>double[]</code>
   * can, and {@link #domain() domain} contains elements that can
   * be represented by <code>double[]</code>,
   * return the value as a <code>double[]</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double[] value (final double[] x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** If the elements of the {@link #codomain() codomain} can be
   * represented by <code>double[]</code>
   * can, and {@link #domain() domain} is 1-dimensional,
   * take a <code>double</code> as the arg and
   * return the value as a <code>double[]</code>.
   * <br>Otherwise throw an {@link UnsupportedOperationException}.
   */
  @SuppressWarnings("unused")
  public double[] value (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //--------------------------------------------------------------
  /** Return the derivative of this function at <code>x</code>.
   * Recall that the general definition of the derivative
   * of a function is the <em>linear</em> function that
   * approximates it in the limit as we approach x.
   * <br> Optional operation.
   */
  @SuppressWarnings("unused")
  public Function derivativeAt (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** Return the derivative of this function at <code>x</code>.
   * Recall that the general definition of the derivative
   * of a function is the <em>linear</em> function that
   * approximates it in the limit as we approach x.
   * <br> Optional operation.
   */
  @SuppressWarnings("unused")
  public Function derivativeAt (final double[] x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @SuppressWarnings("unused")
  public Function derivativeAt (final Function x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  @SuppressWarnings("unused")
  public Function derivativeAt (final Object x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //--------------------------------------------------------------
  /** Return the derivative of this function at <code>x</code>.
   * Recall that the general definition of the derivative
   * of a function is the <em>linear</em> function that
   * approximates it in the limit as we approach x.
   * <br> Optional operation.
   */
  @SuppressWarnings("unused")
  public double slopeAt (final double x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  /** Return the derivative of this function at <code>x</code>.
   * Recall that the general definition of the derivative
   * of a function is the <em>linear</em> function that
   * approximates it in the limit as we approach x.
   * <br> Optional operation.
   */
  @SuppressWarnings("unused")
  public double slopeAt (final double[] x) {
    throw new UnsupportedOperationException(
      getClass().getName()); }

  //--------------------------------------------------------------
  // IFn interfaces
  //--------------------------------------------------------------

  @Override
  public final Object invoke (final Object x) {
    return value((double[]) x); }

  @Override
  public final double invokePrim (final Object x) {
    return doubleValue((double[]) x); }

  @Override
  public final double invokePrim (final double x) {
    return doubleValue(x); }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = 17;
    h += 31*_domain.hashCode();
    h += 31*_codomain.hashCode();
    return h; }

  @Override
  public boolean equals (final Object o) {
    // necessary but not sufficient
    // TODO: should this be left to subclasses?
    return
      (o instanceof Function)
      &&
      _domain.equals(((Function) o)._domain)
      &&
      _codomain.equals(((Function) o)._codomain); }

  //  @Override
  //  public String toString () {
  //    return getClass().getSimpleName() + "[" + name() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public Function (final Object domain,
                   final Object codomain) {
    assert null != domain;
    assert null != codomain;
    _domain = domain;
    _codomain = codomain; }

  //--------------------------------------------------------------
  // unsupported IFn operations
  //--------------------------------------------------------------
  @Override
  public Object call ()
    throws Exception {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public void run () {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object applyTo (final ISeq x0) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke () {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17,
                        final Object x18) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17,
                        final Object x18, final Object x19) {
    throw new UnsupportedOperationException(getClass().getName());
  }

  @Override
  public Object invoke (final Object x0, final Object x1,
                        final Object x2, final Object x3,
                        final Object x4, final Object x5,
                        final Object x6, final Object x7,
                        final Object x8, final Object x9,
                        final Object x10, final Object x11,
                        final Object x12, final Object x13,
                        final Object x14, final Object x15,
                        final Object x16, final Object x17,
                        final Object x18, final Object x19,
                        final Object... x20) {
    throw new UnsupportedOperationException(getClass().getName());
  }
  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------