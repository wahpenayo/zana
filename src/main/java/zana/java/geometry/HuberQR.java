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
 * @version 2018-02-26
 */

@SuppressWarnings("unchecked")
public final class HuberQR extends Function  {

  private static final long serialVersionUID = 0L;
  
  //--------------------------------------------------------------
  // fields
  //--------------------------------------------------------------

  private final double _epsilon;
  private final double _p;

  //--------------------------------------------------------------
  // ScalarFunctional methods
  //--------------------------------------------------------------

  @Override
  public final double doubleValue (final double x) { 
    if (x <= -_p*_epsilon) { 
      return ((_p-1)*x) + (-0.5*_p*(1.0-_p)*_epsilon); }
    if (x <= 0.0) { 
      return ((0.5*(1.0-_p)) / (_p*_epsilon))*x*x; }
    if (x <= (1.0-_p)*_epsilon) { 
      return ((0.5*_p) / ((1.0-_p)*_epsilon))*x*x; }
    return (_p*x) + (-0.5*_p*(1.0-_p)*_epsilon); }

  @Override
  public final double slopeAt (final double x) { 
    if (x <= -_p*_epsilon) { 
      return (_p-1.0); }
    if (x <= 0.0) { 
      return ((1.0-_p) / (_p*_epsilon))*x; }
    if (x <= (1.0-_p)*_epsilon) { 
      return (_p / ((1.0-_p)*_epsilon))*x; }
    return _p; }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------
  
  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*Double.hashCode(_epsilon);
    h += 31*Double.hashCode(_p);
    return h; }
  
  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof HuberQR)
      &&
      (_epsilon == ((HuberQR) o)._epsilon)
      &&
      (_p == ((HuberQR) o)._p); }
  
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() 
      + "[" + _epsilon + ", " + _p + "]"; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private HuberQR (final double epsilon,
                   final double p) { 
    super(Dn.get(1),Dn.get(1));  
    assert (0.0 < p) && (p < 1.0);
    assert (0.0 < epsilon);
    _epsilon = epsilon;
    _p = p;
    }
  
  public static final HuberQR get (final double epsilon,
                                   final double p) { 
    return new HuberQR(p,epsilon); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------