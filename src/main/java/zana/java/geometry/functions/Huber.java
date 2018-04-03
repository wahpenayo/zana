package zana.java.geometry.functions;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** A continuously differentiable function approximating absolute
 * value.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-02
 */

@SuppressWarnings("unchecked")
public final class Huber extends Function  {

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
    if (x >= _epsilon) { return x - _b; }
    if (-x >= _epsilon) { return -x - _b; }
    return _a*x*x; }
  
  @Override
  public final double slopeAt (final double x) { 
    if (x >= _epsilon) { return 1.0; }
    if (-x >= _epsilon) { return -1.0; }
    return 2.0*_a*x; }
 
  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------
  
  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    h += 31*Double.hashCode(_epsilon);
    return h; }
  
  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof Huber)
      &&
      (_epsilon == ((Huber) o)._epsilon); }
  
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + _epsilon + "]"; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Huber (final double epsilon) { 
    super(Dn.get(1),Dn.get(1)); 
    _epsilon = epsilon;
    _a = 0.5 / epsilon;
    _b = -0.5 * epsilon; }
  
  public static final Huber get (final double epsilon) { 
    return new Huber(epsilon); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------