package zana.java.functions;

import java.util.AbstractList;
import java.util.Iterator;

import com.google.common.collect.Iterators;

import clojure.lang.Fn;
import clojure.lang.IFn;
import clojure.lang.ISeq;
import clojure.lang.Named;

//----------------------------------------------------------------------------
/** A Function mapping int/long indexes to Object and vice versa.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2017-10-24
 */
@SuppressWarnings("unchecked")
public final class ArrayLookup extends AbstractList
implements Fn, IFn, IFn.LO, IFn.DO, Named {
  //----------------------------------------------------------------------------
  // fields
  //----------------------------------------------------------------------------
  private final String _name;
  private final String name () { return _name; }
  
  /** fallback value should be immutable.
   */
  private final Object _fallback;
  public final Object fallback () { return _fallback; }
  private final Object[] _values;
  private final Object[] values () { return _values; }
  public final Object[] getValues () { return _values.clone(); }
  public final Object value (final int i) {
    if (i < 0) { return fallback(); }
    if (i >= values().length) { return fallback(); }
    return values()[i]; }
  //----------------------------------------------------------------------------
  // IFn interface
  //----------------------------------------------------------------------------
  @Override
  public final Object invoke (final Object x) {
    if (x instanceof Double) {
      return value((int) Math.round(((Double) x).doubleValue())); }
    if (x instanceof Float) {
      return value((int) Math.round(((Float) x).doubleValue())); }
    return value(((Number) x).intValue()); }
  //----------------------------------------------------------------------------
  // IFn.LO interface
  //----------------------------------------------------------------------------
  @Override
  public final Object invokePrim (final long x) {
    return value((int) x); }
  //----------------------------------------------------------------------------
  // IFn.DO interface
  //----------------------------------------------------------------------------
  @Override
  public final Object invokePrim (final double x) {
    return value((int) Math.round(x)); }
  //----------------------------------------------------------------------------
  // List interface
  //----------------------------------------------------------------------------
  @Override
  public final int size () { return values().length; }
  @Override
  public final Object get (final int i) { return value(i); }
  @Override
  public final Iterator iterator () { return Iterators.forArray(values()); }
  //----------------------------------------------------------------------------
  // Named interface
  //----------------------------------------------------------------------------
  @Override
  public final String getName () { return name(); }
  // TODO: don't implement clojure.lang.Named, since that assumes a namespace?
  @Override
  public final String getNamespace () { 
    throw new UnsupportedOperationException(getClass().getName()); }
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------
  private ArrayLookup (final Object[] values,
                       final String name,
                       final Object fallback) {
    _fallback = fallback;
    _name = name; 
    _values = values;}
  //----------------------------------------------------------------------------
  public static final ArrayLookup make (final Object[] values,
                                        final String name,
                                        final Object fallback) {
    return new ArrayLookup(values.clone(),name,fallback); }
  //----------------------------------------------------------------------------
  // unsupported IFn operations
  //----------------------------------------------------------------------------
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
  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------