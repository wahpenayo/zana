package zana.java.accumulator;

import java.util.Arrays;

//------------------------------------------------------------------------------
/** Vector mean.
 *
 * @author John Alan McDonald
 * @version 2017-01-04
 */

public final class

VectorMean

extends VectorSumBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  /** Default with no data is zero vector.
   */
  
  @Override
  public final Object value () { 

    final double[] mu = new double[dimension()];
    
    if (0.0 == netCount()) { // edge case
      assert 0 <= netCount();
      final int n = dimension();
      for (int i=0;i<n;i++) { assert 0.0 == sum()[i]; }
      Arrays.fill(mu, 0.0); } // paranoia 
    
    else { // normal case
      assert 0 < netCount();
      final long m = netCount();
      final int n = dimension();
      final double[] s = sum();
      for (int i=0;i<n;i++) { mu[i] = s[i] / m;  } }
    
    return mu; }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public VectorMean (final int dimension) { super(dimension); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
