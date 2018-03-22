package zana.java.geometry.functions;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.math3.util.MathArrays;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Squared l2 norm on {@link zana.java.geometry.Dn}.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-03-21
 */

@SuppressWarnings("unchecked")
public final class L2Norm2 extends Function {

  private static final long serialVersionUID = 0L;

  //--------------------------------------------------------------
  // methods
  //--------------------------------------------------------------
  // TODO: replace Kahan sum with fully accurate?

  @Override
  public final double doubleValue (final double[] x) { 
    double s = 0.0;
    double c = 0.0;
    for (final double xi : x) {
      final double zi =  xi*xi - c;
      final double t = s + zi;
      c = (t - s) - zi;
      s = t; } 
    return s; }

  @Override
  public final Function derivativeAt (final double[] x) { 
    return LinearFunctional.make(MathArrays.scale(2.0,x)); }

  //--------------------------------------------------------------
  // Object methods
  //--------------------------------------------------------------

  @Override
  public final int hashCode () { 
    int h = super.hashCode();
    return h; }

  @Override
  public final boolean equals (final Object o) {
    return 
      (o instanceof L2Norm2)
      &&
      (super.equals(o)); }

  // TODO: protect against large data sets!
  @Override
  public final String toString () {
    return 
      getClass().getSimpleName() + "[" + domain() + "]"; }

  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private L2Norm2 (final Object domain) {
    super(domain,Dn.get(1)); }
  
  // TODO: premature optimization?
  private static final Map SINGLETONS = new HashMap(32);

  public static final L2Norm2 get (final Object domain) {
    L2Norm2 l2 = (L2Norm2) SINGLETONS.get(domain);
    if (null == l2) {
      l2= new L2Norm2(domain);
      SINGLETONS.put(domain,l2); }
    return l2; }

  //--------------------------------------------------------------
} // end class
//----------------------------------------------------------------