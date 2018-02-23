package zana.java.geometry;

//----------------------------------------------------------------
/** Squares its arg.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public final class Square extends ScalarFunctional  {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // ScalarFunctional methods
  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) { return x*x; }
  
  @Override
  public final double dfdx (final double x) { return 2*x; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Square () { super(); }
  
  private static final Square SINGLETON = new Square();
  
  public static final Square get () { return SINGLETON; }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------