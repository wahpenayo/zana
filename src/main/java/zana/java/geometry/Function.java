package zana.java.geometry;

import java.io.Serializable;

import clojure.lang.IFn;
import clojure.lang.ISeq;

//----------------------------------------------------------------
/** Base class for functions from and to geometric spaces.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public abstract class Function implements IFn, Serializable {

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

  /** Only supporting domains and codomains whose elements can be
   * implemented as <code>double[]</code>.
   */
  public abstract double[] value (final double[] x);
  
  /** Return the derivative of this function at <code>x</code>.
   * Recall that the general definition of the derivative 
   * of a function is the <em>linear</em> function that 
   * approximates it in the limit as we approach x.
   */
  public abstract IFn derivative (final double[] x);
  
  //--------------------------------------------------------------
  // IFn interface
  //--------------------------------------------------------------

  @Override
  public final Object invoke (final Object x) {
    return value((double[]) x); }

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
  public Object invoke (Object x0, final Object x1) {
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