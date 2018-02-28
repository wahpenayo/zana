package zana.java.geometry.functions;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Multiply arg by a double.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-27
 */

@SuppressWarnings("unchecked")
public final class Scaling extends Function  {

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
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Scaling (final double a) {
    super(Dn.get(1),Dn.get(1)); 
    _a = a; }
  
  public static final Scaling make (final double a) {
    return new Scaling(a); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------