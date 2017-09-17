package zana.java.functions;
import clojure.lang.IFn;
import clojure.lang.IObj;
import clojure.lang.IPersistentMap;
//----------------------------------------------------------------------------
/** Clojure loses return type hints when adding meta data to functions.
 * This is a base class for function wrappers that permit adding arbitrary meta
 * data while preserving the return type hint.
 *
 * @author John Alan McDonald
 * @version 2016-02-02
 */

@SuppressWarnings("unchecked")
public final class IFnWithMeta extends AFnWithMeta {

  //----------------------------------------------------------------------------
  // IObj interface
  //----------------------------------------------------------------------------

  public final IObj withMeta (final IPersistentMap m) {
    return wrap(function(), meta()); }

  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  private IFnWithMeta (final IFn f, final IPersistentMap m) { 
    super(f,m); }

  public static final IFnWithMeta wrap (final IFn f, 
                                        final IPersistentMap m) { 
    return new IFnWithMeta(f,m); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------