package zana.java.functions;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
import clojure.lang.ISeq;
//----------------------------------------------------------------------------
/** Clojure loses return type hints when adding meta data to functions.
 * This is a base class for function wrappers that permit adding arbitrary meta
 * data while preserving the return type hint.
 *
 * @author John Alan McDonald
 * @version 2016-02-02
 */

@SuppressWarnings("unchecked")
public abstract class AFnWithMeta extends Object implements IObj, IFn {
  
  //----------------------------------------------------------------------------
  // IMeta interface
  //----------------------------------------------------------------------------
  
  private final IPersistentMap _meta;
  public final IPersistentMap meta () { return _meta; }
  
  //----------------------------------------------------------------------------
  // IFn interface
  //----------------------------------------------------------------------------
  
  private final IFn _function;
  public final IFn function () { return _function; }
  
  @Override
  public final Object call () throws Exception { return function().call(); }
  @Override
  public final void run () { function().run(); }
  @Override
  public final Object applyTo (final ISeq x0) { return function().applyTo(x0);  }
  @Override
  public final Object invoke () { return function().invoke(); }
  @Override
  public final Object invoke (final Object x0) { return function().invoke(x0); }
  @Override
  public final Object invoke (final Object x0, final Object x1) {
    return function().invoke(x0,x1); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2) {
    return function().invoke(x0,x1,x2); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3) {
    return function().invoke(x0,x1,x2,x3); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4) {
    return function().invoke(x0,x1,x2,x3,x4); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5) {
    return function().invoke(x0,x1,x2,x3,x4,x5); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13, final Object x14) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13, final Object x14,
                              final Object x15) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,
                             x15); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13, final Object x14,
                              final Object x15, final Object x16) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,
                             x15,x16); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13, final Object x14,
                              final Object x15, final Object x16, 
                              final Object x17) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,
                             x15,x16,x17); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13, final Object x14,
                              final Object x15, final Object x16, 
                              final Object x17, final Object x18) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,
                             x15,x16,x17,x18); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13, final Object x14,
                              final Object x15, final Object x16, 
                              final Object x17, final Object x18, 
                              final Object x19) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,
                             x15,x16,x17,x18,x19); }
  @Override
  public final Object invoke (final Object x0, final Object x1, final Object x2, 
                              final Object x3, final Object x4, final Object x5, 
                              final Object x6, final Object x7, final Object x8, 
                              final Object x9, final Object x10, 
                              final Object x11, final Object x12, 
                              final Object x13, final Object x14,
                              final Object x15, final Object x16, 
                              final Object x17, final Object x18, 
                              final Object x19, final Object... x20) {
    return function().invoke(x0,x1,x2,x3,x4,x5,x6,x7,x8,x9,x10,x11,x12,x13,x14,
                             x15,x16,x17,x18,x19,x20); }
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public AFnWithMeta (final IFn f,
                      final IPersistentMap m) { 
    _function = f;
    _meta = m; }

  //----------------------------------------------------------------------------
  // unsupported IFn operations
  //----------------------------------------------------------------------------

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------