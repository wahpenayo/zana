package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Proxy for MSE that's more efficient at choosing a split.
 *
 * @author wahpenayo at gmail dot com
 * @version 2017-10-31
 */

public final class

VectorMSSN extends VectorSumBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  /** Default with no data is 0.0.
   */
  
  @Override
  public final double doubleValue () { 

    if (0.0 == netCount()) { // edge case
      assert 0 <= netCount();
      final int n = dimension();
      for (int i=0;i<n;i++) { assert 0.0 == sum()[i]; }
      return 0.0; }
    assert 0 < netCount();
    final int n = dimension();
    final double[] s = sum();
    double ss = 0.0;
    for (int i=0;i<n;i++) { final double si = s[i]; ss += si*si; }
    return - ss / netCount(); }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public VectorMSSN (final int dimension) { super(dimension); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
