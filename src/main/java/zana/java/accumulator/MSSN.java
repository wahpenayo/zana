package zana.java.accumulator;

//------------------------------------------------------------------------------
/** Proxy for MSE that's more efficient at choosing a split.
 *
 * @author John Alan McDonald
 * @version 2016-12-16
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
    
    else { // normal case
      assert 0 < netCount();
      return - (sum() * sum()) / netCount(); } }
  
 //----------------------------------------------------------------------------
  // construction
  //----------------------------------------------------------------------------

  public MSSN () { super(); }

  //----------------------------------------------------------------------------
} // end class
//----------------------------------------------------------------------------
