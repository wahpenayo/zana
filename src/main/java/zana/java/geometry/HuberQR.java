package zana.java.geometry;

//----------------------------------------------------------------
/** A continuously differentiable function approximating quantile
 * regression cost, a tilted absolute value.
 * This version has an asymmetric quadratic interval around zero,
 * and continuous 2nd derivative at zero.
 * 
 * Quantile regression cost is p*x when x>=0 and
 * (1-p)*x otherwise.
 * 
 * This smoothed version agrees
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public final class HuberQR extends ScalarFunctional  {

  private static final long serialVersionUID = 0L;
  
  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double _p;
  private final double _q; // p-1
  private final double _lower;
  private final double _upper;
  private final double _a;
  private final double _b;

  //--------------------------------------------------------------
  // ScalarFunctional methods
  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) { 
    if (x <= _lower) { return _q*x; }
    if (_upper <= x) { return _p*x; }
    return (_a*x*x) + _b; }
  
  @Override
  public final double dfdx (final double x) { 
    if (x <= _lower) { return _q; }
    if (_upper <= x) { return _p; }
    return 2.0*_a*x; }
 
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private HuberQR (final double p,
                   final double epsilon) { 
    super(); 
    _p = p;
    _q = p - 1.0;
    _lower = (1.0 - p) * epsilon;
    _upper = p*epsilon;
    _a = p / epsilon;
    _b = p * epsilon; }
  
  public static final HuberQR get (final double p,
                                   final double epsilon) { 
    return new HuberQR(p,epsilon); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------