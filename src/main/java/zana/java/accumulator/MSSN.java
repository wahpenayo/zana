package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Proxy for MSE that's more efficient at choosing a split.
 *
 * @author wahpenayo at gmail dot com
 * @version 2017-10-31
 */

public final class

MSSN

extends SumBase {

  //----------------------------------------------------------------------------
  // Accumulator interface
  //----------------------------------------------------------------------------

  /** Default with no data is 0.0.
   */
  
  @Override
  public final double doubleValue () { 

    if (0.0 == netCount()) { // edge case
      assert 0 <= netCount();
      assert 0.0 == sum();
      return 0.0; }
    assert 0 < netCount();
    return - (sum() * sum()) / netCount(); }
  
 //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public MSSN () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
