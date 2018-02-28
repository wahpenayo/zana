package zana.java.geometry.functions;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Squares its double arg.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-27
 */

@SuppressWarnings("unchecked")
public final class Square extends Function  {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // ScalarFunctional methods
  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) { 
    return x*x; }
  
  @Override
  public final double slopeAt (final double x) { return 2*x; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Square () { super(Dn.get(1),Dn.get(1)); }
  
  private static final Square SINGLETON = new Square();
  
  public static final Square get () { return SINGLETON; }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------