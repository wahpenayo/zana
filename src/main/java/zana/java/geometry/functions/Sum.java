package zana.java.geometry.functions;

import zana.java.geometry.Dn;
import zana.java.math.Statistics;

//----------------------------------------------------------------
/** Sum the coordinates.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-02-27
 */

@SuppressWarnings("unchecked")
public final class Sum extends Function  {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  // TODO: replace with fully accurate sum?
  
  @Override
  public final double doubleValue (final double[] x) { 
    return Statistics.kahanSum(x); }
  
  @Override
  public final Function derivativeAt (final double[] x) { 
    return this; }
  
  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------
  // use super class hashCode
  
//  @Override
//  public final int hashCode () { 
//    int h = super.hashCode();
//    h += 31*Double.hashCode(_epsilon);
//    return h; }
  
  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof Sum)
      &&
      (super.equals(o)); }
  
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + domain() + "]"; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private Sum (final Object domain) {
    super(domain,Dn.get(1));  }
  
  public static final Sum make (final Object domain) {
    return new Sum(domain); }

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------