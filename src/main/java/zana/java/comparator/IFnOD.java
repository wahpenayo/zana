package zana.java.comparator;

import java.util.Comparator;

import clojure.lang.IFn;

//----------------------------------------------------------------------------
/** 
 * @author John Alan McDonald
 * @version 2016-11-30
 */

public final class IFnOD extends Object implements Comparator {
  
  private final IFn.OD _z;

  @Override
  public final int compare (final Object x0, final Object x1) {
    return Double.compare(_z.invokePrim(x0), _z.invokePrim(x1)); }
  
  public IFnOD (final clojure.lang.IFn.OD z) { super(); _z = z; }
  //----------------------------------------------------------------------------
  } // end class
  //----------------------------------------------------------------------------