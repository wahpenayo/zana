package zana.java.geometry;

import clojure.lang.IFn;

//----------------------------------------------------------------
/** Base class for 'real'-valued functions on geometric spaces.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-22
 */

@SuppressWarnings("unchecked")
public abstract class Functional 
extends Function 
implements IFn.OD {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------

  /** Only supporting domains and codomains whose elements can be
   * implemented as <code>double[]</code>.
   */
  public abstract double doubleValue (final double[] x);
  
  //--------------------------------------------------------------
  // Function interface
  //--------------------------------------------------------------
  
  @Override
  public final double[] value (final double[] x) {
    return new double[] { doubleValue(x) }; }
  
  //--------------------------------------------------------------
  // IFn.OD interface
  //--------------------------------------------------------------

  @Override
  public final double invokePrim (final Object x) {
    return doubleValue((double[]) x); }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  public Functional (final Object domain) {
    super(domain,Dn.get(1)); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------