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
public final class IFnODWithMeta extends AFnWithMeta implements IFn.OD {
  
  //----------------------------------------------------------------------------
  // IFn$OD interface
  //----------------------------------------------------------------------------
  
  public final IFn.OD functionOD () { return (IFn.OD) function(); }
  
  @Override
  public final double invokePrim (final Object x) { 
    return functionOD().invokePrim(x); }
  
  //----------------------------------------------------------------------------
  // IObj interface
  //----------------------------------------------------------------------------
  
  @Override
  public final IObj withMeta (final IPersistentMap m) {
    return wrap(functionOD(), meta()); }
 
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  private IFnODWithMeta (final IFn.OD f, final IPersistentMap m) { 
    super((IFn) f,m); }

  public static final IFnODWithMeta wrap (final IFn.OD f, 
                                          final IPersistentMap m) { 
    return new IFnODWithMeta(f,m); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------