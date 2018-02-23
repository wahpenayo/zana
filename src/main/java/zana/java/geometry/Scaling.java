package zana.java.geometry;

//----------------------------------------------------------------
/** Multiply arg by a double.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public final class Scaling extends ScalarFunctional  {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double _a;
  
  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) { 
    return _a*x; }
  
  @Override
  public final double dfdx (final double x) {
    return _a; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Scaling (final double a) { super(); _a = a; }
  
  public static final Scaling make (final double a) {
    return new Scaling(a); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------