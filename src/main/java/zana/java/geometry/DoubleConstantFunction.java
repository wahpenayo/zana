package zana.java.geometry;

//----------------------------------------------------------------
/** Constant double.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-26
 */

@SuppressWarnings("unchecked")
public final class DoubleConstantFunction extends Function  {

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
    return _a; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private DoubleConstantFunction (final double a) {
    super(Dn.get(1),Dn.get(1)); 
    _a = a; }
  
  public static final DoubleConstantFunction make (final double a) {
    return new DoubleConstantFunction(a); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------