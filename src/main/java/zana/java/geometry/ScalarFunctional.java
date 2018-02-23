package zana.java.geometry;

import clojure.lang.IFn;

//----------------------------------------------------------------
/** Base class for 'real'-valued functions on geometric spaces.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public abstract class ScalarFunctional 
extends Functional 
implements IFn.DD {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  /** Scalar functionals (real-valued functions) offer a value method
   * that doesn't require allocating an array for input or output.
   */
  public abstract double doubleValue (final double x);
  
  /** The derivative of a scalar functional at x must be a linear
   * scalar functional.
   */
  public abstract double dfdx (final double x);
  
  //--------------------------------------------------------------
  // Functional interface
  //--------------------------------------------------------------
  
  @Override
  public final double doubleValue (final double[] x) {
    return doubleValue(x[0]); }
  
  @Override
  public final LinearFunctional derivativeFunctional (final double[] x) {
    return LinearFunctional.make(new double[] { dfdx(x[0]) }); }
  
  //--------------------------------------------------------------
  // IFn.DD interface
  //--------------------------------------------------------------

  @Override
  public final double invokePrim (final double x) {
    return doubleValue(x); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public ScalarFunctional () {
    super(Dn.get(1)); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------