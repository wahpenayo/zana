package zana.java.geometry.functions;

import zana.java.geometry.Dn;

//----------------------------------------------------------------
/** Map <code>double[]</code> to corresponding affine functional
 * on the domain.
 * 
 * @author wahpenayo at gmail dot com
 * @version 2018-04-04
 */

@SuppressWarnings("unchecked")
public final class AffineDual extends Function {

  private static final long serialVersionUID = 0L;

   //--------------------------------------------------------------
  // Functional methods
  //--------------------------------------------------------------
  
  @Override
  public final Object value (final Object x) {
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
    return AffineFunctional.make(xx); }

  @Override
  public final Function derivativeAt (final Object x) {
    final double[] xx = (double[]) x;
    assert ((Dn) domain()).dimension() == xx.length :
      this.toString() + "\n" + xx;
    return this; }

  //--------------------------------------------------------------
  // Object interface
  //--------------------------------------------------------------

  @Override
  public int hashCode () {
    int h = super.hashCode();
    return h; }

  @Override
  public boolean equals (final Object o) {
    return 
      super.equals(o)
      &&
      (o instanceof AffineDual); }
  
  @Override
  public String toString () { 
    return getClass().getSimpleName() + 
      "["  + domain() + "]"; }
  
  //--------------------------------------------------------------
  // construction
  //--------------------------------------------------------------

  private AffineDual (final Dn domain) {
     super(domain,Dn.get(1)); }

  public static final AffineDual make (final Dn domain) {
     return new AffineDual(domain);}

  //--------------------------------------------------------------
 } // end class
//----------------------------------------------------------------