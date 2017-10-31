package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Proxy for MSE that's more efficient at choosing a split.
 *
 * @author wahpenayo at gmail dot com
 * @version 2017-10-31
 */

public final class

MSSN

extends WeightedSumBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  /** Default with no data is 0.0.
   */
  
  @Override
  public final double doubleValue () { 

    if (0.0 == netWeight()) { // edge case
      assert 0 <= netCount();
      assert 0.0 == sum();
      return 0.0; }
    
    // normal case
    assert 0 < netCount();
    assert 0.0 <= netWeight();
    return - (sum() * sum()) / netWeight(); }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public MSSN () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
