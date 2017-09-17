package zana.java.accumulator.weighted;

//------------------------------------------------------------------------------
/** Proxy for MSE that's more efficient at choosing a split.
 *
 * @author John Alan McDonald
 * @version 2016-12-16
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
    
    else { // normal case
      assert 0 < netCount();
      assert 0.0 <= netWeight();
      return - (sum() * sum()) / netWeight(); } }
  
  //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public MSSN () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
