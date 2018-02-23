package zana.java.geometry;

//----------------------------------------------------------------
/** A continuously differentiable function approximating absolute
 * value.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public final class Huber extends ScalarFunctional  {

  private static final long serialVersionUID = 0L;
  
  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double _epsilon;
  private final double _a;
  private final double _b;

  //--------------------------------------------------------------
  // ScalarFunctional methods
  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) { 
    if (x >= _epsilon) { return x; }
    if (-x >= _epsilon) { return -x; }
    return (_a*x*x) + _b; }
  
  @Override
  public final double dfdx (final double x) { 
    if (x >= _epsilon) { return 1.0; }
    if (-x >= _epsilon) { return -1.0; }
    return 2.0*_a*x; }
 
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Huber (final double epsilon) { 
    super(); 
    _epsilon = epsilon;
    _a = 0.5 / epsilon;
    _b = 0.5 * epsilon; }
  
  public static final Huber get (final double epsilon) { 
    return new Huber(epsilon); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------