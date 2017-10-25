package zana.java.functions;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
//----------------------------------------------------------------------------
/** Clojure loses return type hints when adding meta data to functions.
 * This is a base class for function wrappers that permit adding arbitrary meta
 * data while preserving the return type hint.
 *
 * @author palisades dot lakes at gmail dot com
 * @version 2017-10-24
 */

@SuppressWarnings("unchecked")
public final class IFnOLWithMeta extends AFnWithMeta implements IFn.OL {
  
  //----------------------------------------------------------------------------
  // IFn$OL interface
  //----------------------------------------------------------------------------
  
  public final IFn.OL functionOL () { return (IFn.OL) function(); }
  
  @Override
  public final long invokePrim (final Object x) { 
    return functionOL().invokePrim(x); }
  
  //----------------------------------------------------------------------------
  // IObj interface
  //----------------------------------------------------------------------------
  
  @Override
  public final IObj withMeta (final IPersistentMap m) {
    return wrap(functionOL(), meta()); }
 
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  private IFnOLWithMeta (final IFn.OL f, final IPersistentMap m) { 
    super((IFn) f,m); }

  public static final IFnOLWithMeta wrap (final IFn.OL f, 
                                          final IPersistentMap m) { 
    return new IFnOLWithMeta(f,m); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------